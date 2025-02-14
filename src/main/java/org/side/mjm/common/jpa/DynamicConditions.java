package org.side.mjm.common.jpa;


import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.jpa.baseEntity.BaseEntity;
import org.side.mjm.common.jpa.querydsl.annotation.SearchableField;
import org.side.mjm.common.jpa.querydsl.enumeration.Operator;
import org.side.mjm.common.request.DynamicFilter;
import org.side.mjm.common.request.DynamicSorter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public interface DynamicConditions {
    List<String> BASE_ENTITY_FIELDS = Arrays.stream(BaseEntity.class.getDeclaredFields()).map(Field::getName).toList();

    /**
     * 정렬 조건 생성
     */
    Object generateSort(Class<?> entity, List<DynamicSorter> dynamicSorters);

    /**
     * 키 값으로 기본 정렬 조건 생성
     */
    Object generateDefaultSort(Class<?> entity);

    /**
     * 조회 조건 생성
     */
    Object generateConditions(Class<?> entity, List<DynamicFilter> dynamicFilters);

    default String getSearchFieldPath(Class<?> entity, String searchFieldName) {
        String fieldPath = null;
        // Check Base entity field
        if (entity.getSuperclass() == BaseEntity.class && BASE_ENTITY_FIELDS.contains(searchFieldName)) {
            entity = BaseEntity.class;
        }
        Field[] fields = entity.getDeclaredFields();
        for (Field field : fields) {
            SearchableField searchableField = field.getAnnotation(SearchableField.class);
            // Check @SearchableField annotation
            if (searchableField == null) continue;

            // If the field names are the same, return the columnPath
            if (field.getName().equals(searchFieldName)) {
                fieldPath = field.getName();
                break;
            }

            // If the field name is different, determine it as a reference object and return the columnPath
            String[] paths = searchableField.columnPath();
            for (String searchableFieldPath : paths) {
                String lastFieldName = searchableFieldPath.substring(searchableFieldPath.lastIndexOf(".") + 1);
                if (lastFieldName.equals(searchFieldName)) {
                    fieldPath = searchableFieldPath;
                    break;
                }
            }
        }
        // If the field path is different, throw exception
        if (fieldPath == null) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "'" + searchFieldName + "' field that cannot be searched.");
        }
        return fieldPath;
    }

    default void checkAvailableFieldTypes(Operator operators, Class<?> fieldType) {
        switch (operators) {
            case EQUAL, NOT_EQUAL, IN -> {
                //Possible field type -> String, Integer, Double, LocalDate, Enum
                if (fieldType == LocalDateTime.class) {
                    throw new ServiceException(CommonErrorCode.INVALID_PARAMETER
                            , "For LocalDateTime type, use 'between' operator.");
                }
            }
            case LIKE -> {
                //Possible field type -> String
                if (fieldType != String.class) {
                    throw new ServiceException(CommonErrorCode.INVALID_PARAMETER
                            , "The 'like' operator can only use 'String' types.");
                }
            }
            case BETWEEN, LTE, GTE, LT, GT -> {
                //Possible field type -> LocalDate, LocalDateType, Number type
                if (fieldType == String.class || fieldType.isEnum()) {
                    throw new ServiceException(CommonErrorCode.INVALID_PARAMETER
                            , "The 'between' operator can only use 'LocalDate','LocalDateTime' or 'Number' types.");
                }
            }
        }
    }
}
