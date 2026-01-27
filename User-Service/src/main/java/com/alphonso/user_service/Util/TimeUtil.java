package com.alphonso.user_service.Util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss") // 24-hour format
            .withZone(ZoneId.of("Asia/Kolkata")); // your local time zone

    public static String formatInstant(Instant instant) {
        if (instant == null) return null;
        return FORMATTER.format(instant);
    }
}
