package com.hyutils.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatetimeUtil {

    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static String getViewStrOfDatetime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getStrofTimestamp(long timestamp) {
        return getViewStrOfDatetime(getDateTimeOfTimestamp(timestamp));
    }

    public static Long getTimestampOfDatetime(LocalDateTime localDateTime) {
        // TODO: 2022/11/25 这里使用的东八区，没有使用系统默认的
        if (ZoneId.systemDefault().getId().contains("UTC")){
            return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        }else {
            return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        }
    }

    public static Long getTimeStampOfViewStr2(String value) {
        try {
            return getTimestampOfDatetime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            return getTimestampOfDatetime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return 0L;
    }

    public static Long getTimeStampOfViewStr(String value) {
        return getTimestampOfDatetime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public static Long getTimeStampOfViewStr(String value, String format) {
        return getTimestampOfDatetime(LocalDateTime.parse(value, DateTimeFormatter.ofPattern(format)));
    }


    public static LocalDateTime getLocalDatetimeByStr(String value, String format) {
        return DatetimeUtil.getDateTimeOfTimestamp(DatetimeUtil.getTimeStampOfViewStr(value, format));
    }

    public static LocalDateTime getLocalDatetimeByStr(String value) {
        List<String> formats = new ArrayList<String>() {
            {
                add("yyyy-MM-dd HH:mm:ss");
                add("yyyy/MM/dd HH:mm:ss");
                add("dd-MM-yyyy HH:mm:ss");
                add("yyyyMMddHHmmss");
            }
        };
        for (String format : formats) {
            try {
                return DatetimeUtil.getDateTimeOfTimestamp(DatetimeUtil.getTimeStampOfViewStr(value, format));
            } catch (Exception e) {
            }
        }
        return getLocalDatetimeByStr(value, formats.get(0));
    }

    public static String getYearAndMonthStrByLocalDateTime(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    public static String getYMDHMStrByLocalDateTime(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public static String getYMDStrByLocalDateTime(LocalDateTime time){
        if (Objects.isNull(time))return "--";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    public static String exchangeMonth(String source) {
        return source.replace("十一月", "11")
                .replace("十二月", "12")
                .replace("一月", "01")
                .replace("二月", "02")
                .replace("三月", "03")
                .replace("四月", "04")
                .replace("五月", "05")
                .replace("六月", "06")
                .replace("七月", "07")
                .replace("八月", "08")
                .replace("九月", "09")
                .replace("十月", "10")
                ;

    }

    public static String getExpireTimeStr(LocalDateTime nowTime, LocalDateTime expireTime,Integer fullMinutes) {
        if (expireTime.isAfter(nowTime)) {
            // TODO: 2022/7/26 如果没有超时
            int n = fullMinutes;
            int l = 1;
            int r = n;
            int mid = (l + r) >> 1;
            int ans = -1;
            while (l <= r) {
                if (DatetimeUtil.getYMDHMStrByLocalDateTime(nowTime.plusMinutes(mid)).equals(DatetimeUtil.getYMDHMStrByLocalDateTime(expireTime))) {
                    ans = mid;
                    break;
                }
                if (nowTime.plusMinutes(mid).isAfter(expireTime)) {
                    r = mid - 1;
                } else {
                    l = mid + 1;
                }
                mid = (r + l) >> 1;
            }
            return "剩余" + ((ans / (24 * 60)) + "天" + ((ans / 60) % 24) + "时" + (ans % 60) + "分");
        } else {
            int n = fullMinutes;
            int l = 1;
            int r = n;
            int mid = (l + r) >> 1;
            int ans = -1;
            while (l <= r) {
                if (DatetimeUtil.getYMDHMStrByLocalDateTime(nowTime.plusMinutes(mid)).equals(DatetimeUtil.getYMDHMStrByLocalDateTime(expireTime))) {
                    ans = mid;
                    break;
                }
                if (nowTime.plusMinutes(mid).isAfter(expireTime)) {
                    r = mid - 1;
                } else {
                    l = mid + 1;
                }
                mid = (r + l) >> 1;
            }
            return "超时" + ((ans / (24 * 60)) + "天" + ((ans / 60) % 24) + "时" + (ans % 60) + "分");
        }
    }

    public static Long getTimestampByMile(LocalDateTime localDateTime) {
        return getTimestampOfDatetime(localDateTime) / 1000;
    }

    public static LocalDateTime getLocalDatetimeByMileTimestamp(Long timestamp){
        return getDateTimeOfTimestamp(timestamp*1000L);
    }
}
