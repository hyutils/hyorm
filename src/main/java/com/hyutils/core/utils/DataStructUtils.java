package com.hyutils.core.utils;

import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 数据结构基础设施
 */
public class DataStructUtils {

    private static char randomChar() {
        String codes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        return codes.charAt(random.nextInt(codes.length()));
    }

    /**
     * 合并两个Map
     *
     * @param a
     * @param b
     * @return
     */
    public static Map<String, Object> heBIngMap(Map<String, Object> a, Map<String, Object> b) {
        for (Map.Entry<String, Object> c : b.entrySet()) {
            if (!a.containsKey(c.getKey())) {
                a.put(c.getKey(), c.getValue());
            } else {
                a.computeIfAbsent(c.getKey(), k -> c.getValue());
                if (a.get(c.getKey()) instanceof String) {
                    if (!StringUtils.hasText((String) a.get(c.getKey()))) {
                        a.put(c.getKey(), c.getValue());
                    }
                }
            }
        }

        return a;
    }

    /**
     * 合并多个map
     *
     * @param a
     * @return
     */
    public static Map<String, Object> heBingListMap(List<Map<String, Object>> a) {
        Map<String, Object> ans = new HashMap<>();
        for (Map<String, Object> b : a) {
            if (Objects.nonNull(b)) {
                heBIngMap(ans, b);
            }
        }
        return ans;
    }


    /**
     * 对比两个Map的差异
     *
     * @param a
     * @param b
     * @return
     */
    public static Map<String, Object> duiBiMap(Map<String, Object> a, Map<String, Object> b) {
        Map<String, Object> ans = new HashMap<>(a);
        for (Map.Entry<String, Object> c : b.entrySet()) {
            if (ans.containsKey(c.getKey())) {
                ans.remove(c.getKey());
            } else {
                ans.put(c.getKey(), c.getValue());
            }
        }
        return ans;
    }


    /**
     * 对比一系列map的key的差异
     *
     * @param a
     * @return
     */
    public static Map<String, Object> duiBiListMap(List<Map<String, Object>> a) {
        Map<String, Object> ans = new HashMap<>();
        for (Map<String, Object> b : a) {
            ans = duiBiMap(ans, b);
        }
        return ans;
    }

    public static String randomStr(Integer length) {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < length; i++) {
            ans.append(randomChar()+"");
        }
        return ans.toString();
    }

    public static Boolean isBetween(String lr, Integer val) {
        if (Integer.parseInt(lr.split(",")[0]) <= val && Integer.parseInt(lr.split(",")[1]) >= val) {
            return true;
        }
        return false;
    }

    public static Integer getLegitimateVal(String lr, Integer val) {
        if (Integer.parseInt(lr.split(",")[0]) <= val && Integer.parseInt(lr.split(",")[1]) >= val) {
            return val;
        }
        if (Integer.parseInt(lr.split(",")[0]) > val) {
            return Integer.parseInt(lr.split(",")[0]);
        }
        if (Integer.parseInt(lr.split(",")[1]) < val) {
            return Integer.parseInt(lr.split(",")[1]);
        }
        return val;
    }
}
