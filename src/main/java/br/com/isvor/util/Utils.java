package br.com.isvor.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static LocalDateTime convertStringToLocalDateTime(String data) {

        if (data == null) {
            return null;
        }

        return LocalDateTime.parse(data, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static Timestamp convertLocalDateTimeToTimeStamp(DateTimeFormatter formatter, LocalDateTime data) {

        if (data == null) {
            return null;
        }

        String dataStr = data.toString().substring(0, data.toString().lastIndexOf(".")).replaceAll("T", " ");
        return Timestamp.valueOf(LocalDateTime.parse(dataStr, formatter));
    }

    public static Integer getIntegerIfNotNull(Integer integer) {
        return integer != null ? integer : null;
    }

    public static Boolean objectIsNotNull(Object object) {
        return object != null;
    }

    public static String getStringIfNotNull(Object string) {
        return string != null ? string.toString() : null;
    }
}
