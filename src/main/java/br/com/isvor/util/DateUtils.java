package br.com.isvor.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static LocalDateTime convertStringToLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static Timestamp convertLocalDateTimeToTimeStamp(DateTimeFormatter formatter, LocalDateTime dataComentario) {
        return Timestamp.valueOf(LocalDateTime.parse(dataComentario.toString(), formatter));
    }
}
