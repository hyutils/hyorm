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


    public static List<String> removeNull(List<String> a){
        List<String> ans = new ArrayList<>();
        for (String y : a){
            if (StringUtils.hasText(y)){
//                y = HtmlUtil.exchangeBackText(y);
                ans.add(y);
            }
        }
        return ans;
    }

    public static List<String> str2Array(String str, String b) {
        List<String> ans = new ArrayList<>();
        if (Objects.nonNull(str)) {
            String[] tmp = str.split(b);
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

/**
     * 计算字符串b在a中出现了多少次
     *
     * @param a
     * @param b
     * @return
     */
    public static int count(String a, String b) {
        int n = a.length();
        int m = b.length();
        int cnt = 0;
        for (int i = 0; i < n - m + 1; i++) {
            if (a.substring(i, i + m).equals(b)) {
                cnt++;
            }
        }
        return cnt;
    }

    public static String set2Str(Set<String> a) {
        return ArrayStrUtil.slist2Str(new ArrayList<>(a), ",");
    }


    public static List<String> handleLength(String a, Integer limit) {
        int n = a.length();
        int m = n / limit;
        List<String> ans = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            ans.add(a.substring(i * limit, (i + 1) * limit));
        }
        try {
            if (m * limit <= n) {
                ans.add(a.substring(m*limit,n));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ans;
    }}
