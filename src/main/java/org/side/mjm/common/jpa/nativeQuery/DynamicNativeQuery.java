package org.side.mjm.common.jpa.nativeQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.side.mjm.common.contextHolder.ApplicationContextHolder;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.request.DynamicSearchRequest;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NativeQuery 동적 변경을 위한 Class
 *
 * @author MJM
 * @since 2024-07-11<br />
 */
public class DynamicNativeQuery<T extends Record> {
    private final Logger LOGGER = LoggerFactory.getLogger(DynamicNativeQuery.class);
    private final Class<T> recordClass;
    private final DynamicSearchRequest dynamicSearchRequest;

    private final static String FIND_SELECT = "SELECT";
    private final static String FIND_FROM = "FROM";
    private final static String FIND_WITH_REGEX = "(WITH\\s+(.*?)\\)\\s*SELECT)";
    private final static String FIND_AS_REGEX = "\\bAS\\s+(\\w+)";
    private String baseQuery;

    public DynamicNativeQuery(Class<T> recordClass, DynamicSearchRequest dynamicSearchRequest) {
        this.recordClass = recordClass;
        this.dynamicSearchRequest = dynamicSearchRequest;
    }

    /**
     * Dynamic Native Query 생성하여 Record List 맵핑 후 리턴
     *
     * @param query  NativeQuery 문자열 람다
     * @author MJM
     * @since 2024-07-11<br />
     */
    public List<T> getDynamicQueryResult(NativeQueryInterface query) {
        this.baseQuery = query.query();

        String sql = makeNewQuery();
        List<T> queryResutList = new ArrayList<>();

//         JPA CONNECTION 얻어오기
        EntityManagerFactory entityManagerFactory = ApplicationContextHolder.getContext().getBean(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        entityManager.unwrap(Session.class).doWork(dbConnection -> {
            try {
                PreparedStatement preparedStatement = dbConnection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next()) {
                    queryResutList.add(resultSetToRecordDto(resultSet, this.recordClass));
                }
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                LOGGER.error("Failed Dynamic Native Query[SQL:{}]", sql);
            }
        });
        entityManager.getTransaction().commit();
        entityManager.close();
        LOGGER.info("Dynamic Native Query Select Success.[SQL:{}]", sql);
        return queryResutList;
    }

    /**
     *  resultSet to Record 공통함수
     *
     * @param resultSet  쿼리 결과 resultSet
     * @author MJM
     * @since 2024-07-11<br />
     */
    private T resultSetToRecordDto(ResultSet resultSet, Class<T> recordClass) throws NoSuchMethodException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?>[] classList = getParameterTypes(recordClass);
        Constructor<T> constructor = recordClass.getDeclaredConstructor(classList);

        List<String> names = Arrays.stream(recordClass.getDeclaredFields()).map(field -> field.getName().toUpperCase()).toList();
        Object[] params = new Object[constructor.getParameterCount()];
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            int find = names.indexOf(resultSet.getMetaData().getColumnName(i + 1).toUpperCase());
            if(find != -1) {
                Object value = resultSet.getObject(i + 1);
                if(value == null) {
                    params[find] = null;
                }
                else if(classList[find].equals(value.getClass())) {
                    params[find] = value;
                }
                else if(classList[find].equals(String.class)) {
                    params[find] = String.valueOf(value);
                }
                else if (value instanceof BigDecimal) {
                    if(classList[find].equals(Long.class)) {
                        params[find] = ((BigDecimal) value).longValue();
                    }
                    else if(classList[find].equals(Double.class)) {
                        params[find] = ((BigDecimal)value).doubleValue();
                    }
                    else {
                        params[find] = ((BigDecimal)value).intValue();
                    }
                }
                else {
                    params[find] = (classList[find]).cast(value) ;
                }
            }
        }
        return constructor.newInstance(params);
    }

    /**
     *  Record class 필드의 타입 리스트를 추출
     *
     * @param recordClass  Record class
     * @author MJM
     * @since 2024-07-11<br />
     */
    private Class<?>[] getParameterTypes(Class<T> recordClass) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
    }

    /**
     *  dynamicRequest 에 따라 동적 조건절 및 정렬절을 추가하여 리턴
     *
     * @author MJM
     * @since 2024-07-11<br />
     */
    private String makeNewQuery() {
        // AS 뒤 네이밍된 필드들 리스트
        List<String> selectAbleFields = new ArrayList<>();
        // 동적 조건절이 들어갈 리스트
        List<String> predicateStringList = new ArrayList<>();
        // 동적 정렬절이 들어갈 리스트
        List<String> sortStringList = new ArrayList<>();

        // 쿼리 대소문자 구분 없이 동작
        String upperQuery = this.baseQuery.toUpperCase();

        // WITH 절을 사용하기 위한 부분
        String withQuery = "";
        Pattern withPattern = Pattern.compile(FIND_WITH_REGEX, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher withMatcher = withPattern.matcher(this.baseQuery);
        if(withMatcher.find()) {
            withQuery = withMatcher.group(1);
            int lastSelectIndex = withQuery.toUpperCase().lastIndexOf(FIND_SELECT);
            withQuery = withQuery.substring(0, lastSelectIndex);
            upperQuery = upperQuery.replace(withQuery.toUpperCase(), "");
            baseQuery = baseQuery.replace(withQuery, "");
        }

        // 제일 첫 SELECT와 FROM 위치로 필드들을 추출
        int startIndex = upperQuery.indexOf(FIND_SELECT) + FIND_SELECT.length();
        int endIndex = upperQuery.indexOf(FIND_FROM);
        String tmp = baseQuery.substring(startIndex, endIndex);
        Pattern pattern = Pattern.compile(FIND_AS_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tmp);
        while (matcher.find()) {
            selectAbleFields.add(matcher.group(1));
        }

        // dynamicSearchRequest를 이용한 동적 조건 및 정렬 생성
        if(this.dynamicSearchRequest != null) {
            if(CollectionUtils.isNotEmpty(this.dynamicSearchRequest.filter())) {
                this.dynamicSearchRequest.filter().forEach(dynamicFilter -> {
                    if (selectAbleFields.contains(dynamicFilter.field())) {
                        try {
                            predicateStringList.add(
                                    switch (dynamicFilter.operator()) {
                                        case EQUAL ->
                                                dynamicFilter.field().concat(" = ").concat("'" + dynamicFilter.value() + "'");
                                        case NOT_EQUAL ->
                                                dynamicFilter.field().concat(" <> ").concat("'" + dynamicFilter.value() + "'");
                                        case LIKE ->
                                                dynamicFilter.field().concat(" LIKE ").concat("'" + "%" + dynamicFilter.value() + "%" + "'");
                                        case BETWEEN -> dynamicFilter.field().concat(" BETWEEN ")
                                                .concat("'" + dynamicFilter.value().split(",")[0] + "'")
                                                .concat(" AND ").concat("'" + dynamicFilter.value().split(",")[1] + "'");
                                        case IN -> dynamicFilter.field().concat(" IN ")
                                                .concat("(" + String.join(", ", Arrays.stream(dynamicFilter.value().split(",")).map(s -> "'" + s + "'").toList()) + ")");
                                        case LTE ->
                                                dynamicFilter.field().concat(" <= ").concat("'" + dynamicFilter.value() + "'");
                                        case GTE ->
                                                dynamicFilter.field().concat(" >= ").concat("'" + dynamicFilter.value() + "'");
                                        case LT ->
                                                dynamicFilter.field().concat(" > ").concat("'" + dynamicFilter.value() + "'");
                                        case GT ->
                                                dynamicFilter.field().concat(" < ").concat("'" + dynamicFilter.value() + "'");
                                    }
                            );
                        } catch (IndexOutOfBoundsException e) {
                            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, dynamicFilter.value() + " : 잘못된 필터링 값 입니다.");
                        }
                    } else {
                        throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, dynamicFilter.field() + " : 잘못된 필터링 필드 입니다.");
                    }
                });
            }
            if(CollectionUtils.isNotEmpty(this.dynamicSearchRequest.sorter())) {
                this.dynamicSearchRequest.sorter().forEach(dynamicSorter -> {
                    if (selectAbleFields.contains(dynamicSorter.field())) {
                        sortStringList.add(
                                switch (dynamicSorter.direction()) {
                                    case ASC -> dynamicSorter.field().concat(" ASC ");
                                    case DESC -> dynamicSorter.field().concat(" DESC ");
                                }
                        );
                    } else {
                        throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, dynamicSorter.field() + " : 잘못된 정렬 필드 입니다.");
                    }
                });
            }
        }

        return (withQuery.isEmpty() ? "" : withQuery + "\n")
                .concat("SELECT ")
                .concat(String.join(", ", selectAbleFields))
                .concat(" FROM ( "
                        .concat(this.baseQuery)
                        .concat(" ) AS ").concat("dynamicSubQuery"))
                .concat(predicateStringList.isEmpty() ? "" : " WHERE ".concat(String.join(" AND ", predicateStringList)))
                .concat(sortStringList.isEmpty() ? "" : " ORDER BY ".concat(String.join(" , ", sortStringList)));
    }

    /**
     *  0시~23시 시간대를 seq 를 이용하여 WITH 절로 생성하여 return
     * @return 0~23을 포함한 임시 테이블 WITH 절
     * @author MJM
     * @since 2024-08-08<br />
     */
    public static String generateHoursTempTable() {
        return  "WITH CALENDER AS (\n" +
                "        SELECT LPAD(SEQ, 2, '0') AS CALC\n" +
                "         FROM seq_0_to_23\n" +
                ")\n";
    }

    /**
     *  0~6 요일 구분을 seq 를 이용하여 WITH 절로 생성하여 return
     *  사용시 CONCAT(SUBSTR(_UTF8'월화수목금토일', CALENDER.CALC+1, 1), '요일') 등을 이용하여 한글 요일로 표출
     *  WEEKDAY 는 0:월요일 ~ 6:일요일 까지 존재
     *  JOIN 사용시 DB 내장 WEEKDAY 함수를 이용하여 관계 설정 EX) RIGHT JOIN CALENDER ON CALENDER.CALC = WEEKDAY(TRAFFIC.CLCT_DT)
     * @return 0~6을 포함한 임시 테이블 WITH 절
     * @author MJM
     * @since 2024-08-08<br />
     */
    public static String generateWeekdaysTempTable() {
        return  "WITH CALENDER AS (\n" +
                "        SELECT SEQ AS CALC\n" +
                "          FROM seq_0_to_6\n" +
                ")\n";
    }

    /**
     *  시작/종료 일자를 받아 사이에 존재하는 Month 를 포함하는 WITH 절을 생성하여 return
     *  해당 열 표출시에는 TO_CHAR 등을 이용하여 형식을 지정하여 사용함이 권장됨. 기본적으로 DATE 형식 문자열이 RETURN
     * @return 시작/종료 일자 사이에 존재하는 Month 임시 테이블 WITH 절
     * @param startDate YYYYMMDD 형식의 시작 일자
     * @param endDate YYYYMMDD 형식의 종료 일자
     * @author MJM
     * @since 2024-08-08<br />
     */
    public static String generateMonthTempTable(String startDate, String endDate) {
        return  "WITH RECURSIVE CALENDER AS (\n" +
                "     SELECT DATE('" + startDate + "') + INTERVAL 0 MONTH AS CALC\n" +
                "     UNION ALL\n" +
                "     SELECT CALC + INTERVAL 1 MONTH FROM CALENDER WHERE CALC < LAST_DAY('" + endDate + "') - INTERVAL 1 MONTH\n" +
                ")\n";
    }

    /**
     *  시작/종료 일자를 받아 사이에 존재하는 Day 를 포함하는 WITH 절을 생성하여 return
     *  해당 열 표출시에는 TO_CHAR 등을 이용하여 형식을 지정하여 사용함이 권장됨. 기본적으로 DATE 형식 문자열이 RETURN
     * @return 시작/종료 일자 사이에 존재하는 Day 임시 테이블 WITH 절
     * @param startDate YYYYMMDD 형식의 시작 일자
     * @param endDate YYYYMMDD 형식의 종료 일자
     * @author MJM
     * @since 2024-08-08<br />
     */
    public static String generateDayTempTable(String startDate, String endDate) {
        return  "WITH RECURSIVE CALENDER AS (\n" +
                "     SELECT DATE('" + startDate + "') + INTERVAL 0 DAY AS CALC\n" +
                "     UNION ALL\n" +
                "     SELECT CALC + INTERVAL 1 DAY FROM CALENDER WHERE CALC < DATE('" + endDate + "')\n" +
                ")\n";
    }
}
