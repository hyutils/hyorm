package com.hyutils.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

    public static LocalDate getDateTimeOfTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static String getViewStrOfDatetime(LocalDate localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getStrofTimestamp(long timestamp) {
        return getViewStrOfDatetime(getDateTimeOfTimestamp(timestamp));
    }

    public static Long getTimestampOfDatetime(LocalDate localDateTime) {
        return localDateTime.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Long getTimeStampOfViewStr(String value) {
        List<String> formats = new ArrayList<String>() {
            {
                add("yyyy-MM-dd");
                add("yyyy-MM-d");
                add("yyyy-M-dd");
                add("yyyy-M-d");
                add("yyyy/MM/dd");
                add("yyyy/MM/d");
                add("yyyy/M/dd");
                add("yyyy/M/d");
                add("yyyy年MM月dd日");
                add("yyyy年MM月d日");
                add("yyyy年M月dd日");
                add("yyyy年M月d日");
                add("yyyy.MM.dd");
                add("yyyy.MM.d");
                add("yyyy.M.dd");
                add("yyyy.M.d");
            }
        };
        for (String format : formats) {
            try {
                return getTimeStampOfViewStrWithFormat(value, format);
            } catch (Exception e) {
            }
        }
        try {
            if (value.length() == 4) {
                StringBuilder x = new StringBuilder(value).append("-12-30");
                return getTimeStampOfViewStrWithFormat(x.toString(), "yyyy-MM-dd");
            }
        } catch (Exception e) {

        }
        return getTimeStampOfViewStrWithFormat(value, "yyyy年");
    }

    public static Long getTimeStampOfViewStrWithFormat(String value, String format) {
        return getTimestampOfDatetime(LocalDate.parse(value, DateTimeFormatter.ofPattern(format)));
    }


    public static LocalDate getLocalDatetimeByStr(String value) {
        return DateUtil.getDateTimeOfTimestamp(DateUtil.getTimeStampOfViewStr(value));
    }

    public static Integer getWeekWithDate(LocalDate localDate) {
        return localDate.get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear());
    }

    public static LocalDateTime getDayOfMonthFirst(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, offset);
        return calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime getDayOfMonthLast(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.roll(Calendar.DAY_OF_MONTH, -1);
        calendar.add(Calendar.MONTH, offset);
        return calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


}
