package org.side.mjm.common.converter;

import org.side.mjm.common.converter.enumeration.DateType;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;
import java.util.StringJoiner;

public class Converter {

    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }

    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public static String localDateToString(LocalDate localDate, DateType dateType) {
        if (ObjectUtils.isNotEmpty(localDate)) {
            return localDate.format(DateTimeFormatter.ofPattern(dateType.getPattern(), Locale.KOREA));
        }
        return null;
    }

    public static String localDateTimeToString(LocalDateTime localDateTime, DateType dateType) {
        if (ObjectUtils.isNotEmpty(localDateTime)) {
            return localDateTime.format(DateTimeFormatter.ofPattern(dateType.getPattern(), Locale.KOREA));
        }
        return null;
    }

    public static LocalDate stringToLocalDate(String dateString, DateType dateType) throws DateTimeParseException, IllegalArgumentException {
        if (ObjectUtils.isNotEmpty(dateString)) {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateType.getPattern(), Locale.KOREA));
        }
        return null;
    }

    public static LocalDateTime stringToLocalDateTime(String dateTimeString, DateType dateType) throws DateTimeParseException, IllegalArgumentException {
        if (ObjectUtils.isNotEmpty(dateTimeString)) {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(dateType.getPattern(), Locale.KOREA));
        }
        return null;
    }

    public static String dateStringToFormmatedDateString(String dateString, DateType fromDateType, DateType toDateType) {
        if (ObjectUtils.isNotEmpty(dateString)) {
            LocalDate localDate = stringToLocalDate(dateString, fromDateType);
            dateString = localDateToString(localDate, toDateType);
            return dateString;
        }
        return null;
    }

    public static String dateTimeStringToFormmatedDateTimeString(String dateTimeString, DateType fromDateType, DateType toDateType) {
        if (ObjectUtils.isNotEmpty(dateTimeString)) {
            LocalDateTime localDateTime = stringToLocalDateTime(dateTimeString, fromDateType);
            dateTimeString = localDateTimeToString(localDateTime, toDateType);
            return dateTimeString;
        }
        return null;
    }

    public static String secondToTimeFormatString(Long sec) {
        if (sec == null) {
            return null;
        }
        String diffTime = sec < 0 ? "-" : "";
        diffTime += String.format("%d:%02d:%02d", (Math.abs(sec) / 3600), (Math.abs(sec) % 3600) / 60, (Math.abs(sec) % 3600) % 60);
        return diffTime;
    }

    public static String yearMonthToYearMonthDay(String dateString) throws DateTimeParseException, IllegalArgumentException {
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

    public static String blobToBase64(byte[] blob) {
        if (ObjectUtils.isEmpty(blob)) {
            return null;
        }
        return Base64.getEncoder().encodeToString(blob);
    }
}
