package com.happy_time.happy_time.constant;

import java.util.HashMap;
import java.util.Map;

public class DateTimeConstant {
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String WEEK = "week";
    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "second";
    public static final String MILLISECOND = "millisecond";

    public static Map<String, Long> SECOND_MAP;

    static {
        SECOND_MAP = new HashMap<>();
        SECOND_MAP.put(YEAR, 365 * 24 * 60 * 60L);
        SECOND_MAP.put(MONTH, 30 * 24 * 60 * 60L);
        SECOND_MAP.put(WEEK, 7 * 24 * 60 * 60L);
        SECOND_MAP.put(DAY, 24 * 60 * 60L);
        SECOND_MAP.put(HOUR, 60 * 60L);
        SECOND_MAP.put(MINUTE, 60L);
        SECOND_MAP.put(SECOND, 1L);
        SECOND_MAP.put(MILLISECOND, 0L);

    }

    public static Map<String, Long> MILLISECOND_MAP;

    static {
        MILLISECOND_MAP = new HashMap<>();
        MILLISECOND_MAP.put(YEAR, 365 * 24 * 60 * 60 * 1000L);
        MILLISECOND_MAP.put(MONTH, 30 * 24 * 60 * 60 * 1000L);
        MILLISECOND_MAP.put(WEEK, 7 * 24 * 60 * 60 * 1000L);
        MILLISECOND_MAP.put(DAY, 24 * 60 * 60 * 1000L);
        MILLISECOND_MAP.put(HOUR, 60 * 60 * 1000L);
        MILLISECOND_MAP.put(MINUTE, 60 * 1000L);
        MILLISECOND_MAP.put(SECOND, 1000L);
        MILLISECOND_MAP.put(MILLISECOND, 1L);

    }

    public static class Pattern {
        public static final String OMI_DEFAULT = "dd/MM/yyyy HH:mm:ss";
        public static final String YEAR_MONTH = "yyyy/MM";
        public static final String MONTH_YEAR = "MM/yyyy";
        public static final String HOUR_MINUTE_DAY_MONTH_YEAR = "HH:mm dd/MM/yyyy";
        public static final String HOUR_MINUTE_SECOND_DAY_MONTH_YEAR = "HH:mm:ss dd/MM/yyyy";
        public static final String DAY_MONTH_YEAR = "dd/MM/yyyy";
    }
}
