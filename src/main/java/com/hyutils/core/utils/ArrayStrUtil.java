package com.hyutils.core.utils;

import org.springframework.util.StringUtils;

import java.util.*;

public class ArrayStrUtil {

    public static List<String> str2Array(String str) {
        List<String> ans = new ArrayList<>();
        if (Objects.nonNull(str)) {
            String[] tmp = str.split(",");
            for (String x : tmp) {
                if (Objects.nonNull(x) && StringUtils.hasText(x)) {
                    ans.add(x);
                }
            }
        }
        return ans;
    }

    public static List<Long> str2LArray(String str) {
        List<Long> ans = new ArrayList<>();
        List<String> tmp = str2Array(str);
        for (String x : tmp) {
            x = x.trim();
            try {
                ans.add(Long.parseLong(x.trim()));
            }catch (Exception e){
            }
        }
        return ans;
    }

    public static List<Integer> str2IArray(String str) {
        List<Integer> ans = new ArrayList<>();
        List<String> tmp = str2Array(str);
        for (String x : tmp) {
            x = x.trim();
            try {
                ans.add(Integer.parseInt(x.trim()));
            }catch (Exception e){
            }
        }
        return ans;
    }

    public static Set<String> str2Set(String str) {
        Set<String> ans = new HashSet<>();
        if (Objects.nonNull(str)) {
            String[] tmp = str.split(",");
            for (String x : tmp) {
                if (StringUtils.hasText(x)) {
                    ans.add(x);
                }
            }
        }
        return ans;
    }

    public static Set<Long> str2LSet(String str) {
        Set<Long> ans = new HashSet<>();
        if (Objects.nonNull(str)) {
            String[] tmp = str.split(",");
            for (String x : tmp) {
                if (StringUtils.hasText(x)) {
                    ans.add(Long.parseLong(x));
                }
            }
        }
        return ans;
    }

    public static String slist2Str(List<String> a, String b) {
        if (Objects.isNull(a))return "";
        StringBuilder ans = new StringBuilder();
        int cnt = 0;
        for (String x : a) {
            if (cnt == 0) {
                ans.append(x);
            } else {
                ans.append(b).append(x);
            }
            cnt++;
        }
        return ans.toString();
    }

    public static String llist2Str(List<Long> a, String b) {
        StringBuilder ans = new StringBuilder();
        int cnt = 0;
        for (Long x : a) {
            if (cnt == 0) {
                ans.append(x);
            } else {
                ans.append(b).append(x);
            }
            cnt++;
        }
        return ans.toString();
    }

    public static String ilist2Str(List<Integer> a, String b) {
        StringBuilder ans = new StringBuilder();
        int cnt = 0;
        for (Integer x : a) {
            if (cnt == 0) {
                ans.append(x);
            } else {
                ans.append(b).append(x);
            }
            cnt++;
        }
        return ans.toString();
    }

}
