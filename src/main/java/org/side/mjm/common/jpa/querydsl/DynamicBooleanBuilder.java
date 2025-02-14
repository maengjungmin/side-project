package org.side.mjm.common.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import jakarta.persistence.Enumerated;
import org.side.mjm.common.converter.enumeration.DateType;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.jpa.DynamicConditions;
import org.side.mjm.common.jpa.baseEntity.BaseEntity;
import org.side.mjm.common.jpa.querydsl.annotation.DefaultSort;
import org.side.mjm.common.jpa.querydsl.annotation.NumericOrder;
import org.side.mjm.common.jpa.querydsl.enumeration.Operator;
import org.side.mjm.common.jpa.querydsl.enumeration.SortDirection;
import org.side.mjm.common.request.DynamicFilter;
import org.side.mjm.common.request.DynamicSorter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class DynamicBooleanBuilder implements DynamicConditions {

    @Override
    public List<OrderSpecifier<?>> generateSort(Class<?> entity, List<DynamicSorter> dynamicSorters) {
        if (ObjectUtils.isEmpty(dynamicSorters)) {
            return generateDefaultSort(entity);
        }
        return parseSort(entity, dynamicSorters);
    }


    private List<OrderSpecifier<?>> parseSort(Class<?> entity, List<DynamicSorter> dynamicSorters) {
        List<OrderSpecifier<?>> orderSpecifierList = new ArrayList<>();
        PathBuilder<Object> root = new PathBuilder<>(entity, lowerCaseFirst(entity.getSimpleName()));
        for (DynamicSorter dynamicSorter : dynamicSorters) {
            String fieldPath = getSearchFieldPath(entity, dynamicSorter.field());
            PathBuilder<Object> rootPath = getParentPath(root, fieldPath);
            if (ObjectUtils.isEmpty(dynamicSorter.direction())) {
                throw new ServiceException(CommonErrorCode.INVALID_PARAMETER
                        , "Invalid sort direction. Possible sort directions -> " + SortDirection.getSorDirectionString());
            }
            StringPath stringPath = rootPath.getString(dynamicSorter.field());
            switch (dynamicSorter.direction()) {
                case ASC -> {
                    if (checkNumericOrder(entity, dynamicSorter.field())) {
                        orderSpecifierList.add(
                                Expressions.numberTemplate(Integer.class, "CAST({0} AS INTEGER)", stringPath)
                                        .asc().nullsLast());
                    } else {
                        orderSpecifierList.add(stringPath.asc().nullsLast());
                    }
                }
                case DESC -> {
                    if (checkNumericOrder(entity, dynamicSorter.field())) {
                        orderSpecifierList.add(
                                Expressions.numberTemplate(Integer.class, "CAST({0} AS INTEGER)", stringPath)
                                        .desc().nullsLast());
                    } else {
                        orderSpecifierList.add(stringPath.desc().nullsLast());
                    }
                }
            }
        }
        return orderSpecifierList;
    }

    @Override
    public List<OrderSpecifier<?>> generateDefaultSort(Class<?> entity) {
        List<OrderSpecifier<?>> orderSpecifierList = new ArrayList<>();
        DefaultSort defaultSort = entity.getAnnotation(DefaultSort.class);
        if (Objects.isNull(defaultSort) || ObjectUtils.isEmpty(defaultSort.columnName())) {
            return orderSpecifierList;
        }
        String[] columnNames = defaultSort.columnName();
        SortDirection[] sortDirections = defaultSort.direction();
        if (columnNames.length != sortDirections.length) {
            throw new ServiceException(CommonErrorCode.SERVICE_ERROR,
                    "Check '" + entity.getSimpleName() + "' entity @DefaultSort settings. different lengths. (columnName-direction)");
        }
        List<DynamicSorter> dynamicSorters = new ArrayList<>();
        for (int i = 0, n = columnNames.length; i < n; i++) {
            DynamicSorter dynamicSorter = new DynamicSorter(columnNames[i], sortDirections[i]);
            dynamicSorters.add(dynamicSorter);
        }
        return parseSort(entity, dynamicSorters);
    }

    @Override
    public BooleanBuilder generateConditions(Class<?> entity, List<DynamicFilter> dynamicFilters) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (dynamicFilters == null || dynamicFilters.size() == 0) {
            return booleanBuilder;
        }
        for (DynamicFilter dynamicFilter : dynamicFilters) {
            if (dynamicFilter.operator() == null) {
                throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "Invalid operator. Possible Operators -> " + Operator.getOperatorString());
            }
            PathBuilder<Object> root = new PathBuilder<>(entity, lowerCaseFirst(entity.getSimpleName()));
            String fieldPath = getSearchFieldPath(entity, dynamicFilter.field());
            PathBuilder<Object> rootPath = getParentPath(root, fieldPath);
            Class<?> fieldType = getType(entity, fieldPath);
            String value = dynamicFilter.value();
            // Possible search to null data
            if (value == null) {
                booleanBuilder.and(rootPath.get(dynamicFilter.field()).isNull());
                continue;
            }
            switch (dynamicFilter.operator()) {
                case EQUAL -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType.isEnum()) {
                        booleanBuilder.and(rootPath.get(dynamicFilter.field()).eq(stringToEnum(fieldType, value)));
                    } else if (fieldType == LocalDate.class) {
                        booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).eq(dateStringToLocalDate(value)));
                    } else {
                        booleanBuilder.and(rootPath.get(dynamicFilter.field()).eq(value));
                    }

                }
                case NOT_EQUAL -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    booleanBuilder.and(rootPath.get(dynamicFilter.field()).ne(value));
                }
                case LIKE -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    booleanBuilder.and(rootPath.getString(dynamicFilter.field()).likeIgnoreCase("%" + value + "%"));
                }
                case BETWEEN -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        if (value.length() == 13) {
                            value = yearMonthToYearMonthDay(value);
                        }
                        List<LocalDate> list = Arrays.stream(value.split(",")).map(this::dateStringToLocalDate).toList();
                        booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).between(list.get(0), list.get(1)));
                    } else if (fieldType == LocalDateTime.class) {
                        try {
                            List<LocalDateTime> list = Arrays.stream(value.split(","))
                                    .map(this::dateTimeStringToLocalDateTime).toList();
                            booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDateTime.class).between(list.get(0), list.get(1)));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else {
                        List<String> list = Arrays.asList(value.split(","));
                        booleanBuilder.and(rootPath.getNumber(dynamicFilter.field(), Double.class)
                                .between(Double.valueOf(list.get(0)), Double.valueOf(list.get(1))));
                    }
                }
                case IN -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        List<LocalDate> list = Arrays.stream(value.split(",")).map(this::dateStringToLocalDate).toList();
                        booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDate.class).in(list));
                    } else if (fieldType.isEnum()) {
                        List<? extends Enum<?>> list = Arrays.stream(value.split(",")).map(s -> stringToEnum(fieldType, s)).toList();
                        booleanBuilder.and(rootPath.get(dynamicFilter.field()).in(list));
                    } else {
                        List<String> list = Arrays.asList(value.split(","));
                        booleanBuilder.and(rootPath.get(dynamicFilter.field()).in(list));
                    }
                }
                case LT -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        try {
                            LocalDate localDate = dateStringToLocalDate(value);
                            booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).lt(localDate));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else if (fieldType == LocalDateTime.class) {
                        try {
                            LocalDateTime localDateTime = dateTimeStringToLocalDateTime(value);
                            booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDateTime.class).lt(localDateTime));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else {
                        booleanBuilder.and(rootPath.getNumber(dynamicFilter.field(), Double.class).lt(Double.valueOf(value)));
                    }
                }
                case LTE -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        try {
                            LocalDate localDate = dateStringToLocalDate(value);
                            booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).loe(localDate));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else if (fieldType == LocalDateTime.class) {
                        try {
                            LocalDateTime localDateTime = dateTimeStringToLocalDateTime(value);
                            booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDateTime.class).loe(localDateTime));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else {
                        booleanBuilder.and(rootPath.getNumber(dynamicFilter.field(), Double.class).loe(Double.valueOf(value)));
                    }
                }
                case GT -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        try {
                            LocalDate localDate = dateStringToLocalDate(value);
                            booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).gt(localDate));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else if (fieldType == LocalDateTime.class) {
                        try {
                            LocalDateTime localDateTime = dateTimeStringToLocalDateTime(value);
                            booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDateTime.class).gt(localDateTime));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else {
                        booleanBuilder.and(rootPath.getNumber(dynamicFilter.field(), Double.class).gt(Double.valueOf(value)));
                    }
                }
                case GTE -> {
                    checkAvailableFieldTypes(dynamicFilter.operator(), fieldType);
                    if (fieldType == LocalDate.class) {
                        try {
                            LocalDate localDate = dateStringToLocalDate(value);
                            booleanBuilder.and(rootPath.getDate(dynamicFilter.field(), LocalDate.class).goe(localDate));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else if (fieldType == LocalDateTime.class) {
                        try {
                            LocalDateTime localDateTime = dateTimeStringToLocalDateTime(value);
                            booleanBuilder.and(rootPath.getDateTime(dynamicFilter.field(), LocalDateTime.class).goe(localDateTime));
                        } catch (DateTimeParseException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
                        }
                    } else {
                        booleanBuilder.and(rootPath.getNumber(dynamicFilter.field(), Double.class).goe(Double.valueOf(value)));
                    }
                }
            }
        }
        return booleanBuilder;
    }

    private Class<?> getType(Class<?> entity, String fieldName) {
        try {
            if (entity.getSuperclass() == BaseEntity.class && BASE_ENTITY_FIELDS.contains(fieldName)) {
                entity = entity.getSuperclass();
            }
            if (fieldName.contains(".")) {
                String[] entityField = fieldName.split("\\.");
                return getType(entity.getDeclaredField(entityField[0]).getType(), fieldName.substring(fieldName.indexOf(".") + 1));
            }
            if (entity.getDeclaredField(fieldName).getDeclaredAnnotation(Enumerated.class) != null) {
                return entity.getDeclaredField(fieldName).getType();
            }
            return entity.getDeclaredField(fieldName).getType();
        } catch (PathElementException e) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private PathBuilder<Object> getParentPath(PathBuilder<Object> root, String fieldPath) {
        if (fieldPath.contains(".")) {
            String[] paths = fieldPath.substring(0, fieldPath.lastIndexOf(".")).split("\\.");
            PathBuilder<Object> pathBuilder = root;
            for (String path : paths) {
                pathBuilder = pathBuilder.get(path);
            }
            return pathBuilder;
        } else {
            return root;
        }
    }

    private Enum<?> stringToEnum(Class<?> fieldType, String value) {
        Method method;
        try {
            method = fieldType.getDeclaredMethod("valueOf", String.class);
            return (Enum<?>) method.invoke(null, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return null;
        }
    }

    private String getStringToNumbers(String str) {
        return str.replaceAll("[^0-9]+", "");
    }

    private LocalDate dateStringToLocalDate(String dateString) throws DateTimeParseException, IllegalArgumentException {
        if (!StringUtils.isEmpty(dateString)) {
            String replacedDateString = getStringToNumbers(dateString);
            return LocalDate.parse(replacedDateString, DateTimeFormatter.ofPattern("yyyyMMdd", Locale.KOREA));
        } else {
            return null;
        }
    }

    private LocalDateTime dateTimeStringToLocalDateTime(String dateTimeString) throws DateTimeParseException, IllegalArgumentException {
        if (!StringUtils.isEmpty(dateTimeString)) {
            String replacedDateString = getStringToNumbers(dateTimeString);
            DateTimeFormatter toTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.KOREA);
            return LocalDateTime.parse(replacedDateString, toTimeFormatter);
        } else {
            return null;
        }
    }

    private String localDateToString(LocalDate localDate, DateType dateType) {
        if (ObjectUtils.isNotEmpty(localDate)) {
            return localDate.format(DateTimeFormatter.ofPattern(dateType.getPattern(), Locale.KOREA));
        }
        return null;
    }

    private String yearMonthToYearMonthDay(String dateString) throws DateTimeParseException, IllegalArgumentException {
        if (dateString.contains(",") && dateString.length() == 13) {
            String[] yearMonths = dateString.split(",");
            String startDate = yearMonths[0] + "01";
            String endDate = localDateToString(LocalDate.parse(yearMonths[1] + "01", DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .withDayOfMonth(LocalDate.parse(yearMonths[1] + "01", DateTimeFormatter.ofPattern("yyyyMMdd"))
                            .lengthOfMonth()), DateType.YYYYMMDD);
            return new StringJoiner(",").add(startDate).add(endDate).toString();
        }
        return dateString;
    }

    private boolean checkNumericOrder(Class<?> entity, String fieldName) {
        try {
            return ObjectUtils.isNotEmpty(entity.getDeclaredField(fieldName).getAnnotation(NumericOrder.class));
        } catch (NoSuchFieldException e) {
            // getSearchFieldPath 에서 이미 체크하기 때문에 false return
            return false;
        }
    }

    private String lowerCaseFirst(String text) {
        char[] chars = text.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
