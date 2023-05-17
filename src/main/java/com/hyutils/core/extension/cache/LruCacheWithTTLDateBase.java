package com.hyutils.core.extension.cache;


import com.hyutils.core.utils.DatetimeUtil;
import com.fasterxml.jackson.databind.util.LRUMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 后期直接将Lru本地缓存的put和set方法直接改成跟redis交互的即可
 */
@Service
public class LruCacheWithTTLDateBase {


    @Value("${lru.cache.second}")
    private Integer second;

    /**
     * 缓存键规则：查询条件为键
     * 唯一缓存标记（用于区分不同的业务场景） + 条件（条件1：值（如果有）+ 条件2：值（如果有）+ 条件3：值（如果有））
     * 缓存值规则：返回的值的Json为值
     */
    private LRUMap<String, String> cache = new LRUMap<>(20, 10000);
    private ConcurrentHashMap<String, Long> ttls = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> seconds = new ConcurrentHashMap<>();

    public List<String> getPrefixKeys(String prefix) {
        return ttls.keySet().stream().filter(key -> key.startsWith(prefix))
                .collect(Collectors.toList());
    }

    public String getCacheJson(String className, String methodName, Map<String, Object> condition) {
        String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
        if (ttls.containsKey(key)) {
            if (DatetimeUtil.getDateTimeOfTimestamp(ttls.get(key)).plusSeconds(second).isBefore(LocalDateTime.now())) {
                // TODO: 2021/9/29 如果当前时间没有超时，直接返回结果
//                String result = "超时，当前时间为：" + DatetimeUtil.getViewStrOfDatetime(LocalDateTime.now()) + ",超时时间为："
//                        + DatetimeUtil.getViewStrOfDatetime(DatetimeUtil.getDateTimeOfTimestamp(ttls.get(key)).plusSeconds(second)) + "，其余配置为："
//                        + "超时的秒数为（" + second + "）, 入库的时间为:" + DatetimeUtil.getDateTimeOfTimestamp(ttls.get(key)) + "(" + ttls.get(key) + ")";
                ttls.remove(key);
//                return result;
            } else {
                // TODO: 2021/9/29 删除缓存
                return cache.get(key);
            }
        }
        return null;
    }

    public String getCacheJsonWithTTl(String className, String methodName, Map<String, Object> condition) {
        String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
        if (ttls.containsKey(key)) {
            if (DatetimeUtil.getDateTimeOfTimestamp(ttls.get(key)).plusSeconds(seconds.get(key)).isBefore(LocalDateTime.now())) {
                // TODO: 2021/9/29 删除缓存
                ttls.remove(key);
                seconds.remove(key);
            } else {
                // TODO: 2021/9/29 如果当前时间没有超时，直接返回结果
                return cache.get(key);
            }
        }
        return null;
    }

    public Boolean removeCache(String className, String methodName, Map<String, Object> condition) {
        String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
        ttls.remove(key);
        seconds.remove(key);
        return true;
    }

    public Boolean removeCache(String key) {
        ttls.remove(key);
        seconds.remove(key);
        return true;
    }

    public String getCacheJsonWithTTl(String key) {
        if (ttls.containsKey(key)) {
            if (DatetimeUtil.getDateTimeOfTimestamp(ttls.get(key)).plusSeconds(seconds.get(key)).isBefore(LocalDateTime.now())) {
                // TODO: 2021/9/29 删除缓存
                ttls.remove(key);
                seconds.remove(key);
            } else {
                // TODO: 2021/9/29 如果当前时间没有超时，直接返回结果
                return cache.get(key);
            }
        }
        return null;
    }


    public Boolean putCacheJson(String className, String methodName, Map<String, Object> condition, String value) {
        try {
            String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
            ttls.put(key, DatetimeUtil.getTimestampOfDatetime(LocalDateTime.now()));
            cache.put(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean putCacheJsonWithTTL(String className, String methodName, Map<String, Object> condition, String value, Integer ttl) {
        try {
            String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
            ttls.put(key, DatetimeUtil.getTimestampOfDatetime(LocalDateTime.now()));
            seconds.put(key, ttl);
            cache.put(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getKey(String className, String methodName, Map<String, Object> condition) {
        String key = className + "+" + methodName + "+" + getSortedConditionStr(condition);
        return key;
    }

    private String getSortedConditionStr(Map<String, Object> condition) {
        Set<String> keys = condition.keySet();
        List list = new ArrayList(keys);
        Object[] ary = list.toArray();
        Arrays.sort(ary);
        list = Arrays.asList(ary);
        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str += list.get(i) + "=" + condition.get(list.get(i) + "") + "&";
        }
        return str;
    }

    public Map<String, String> getConditionOfStr(String conditionStr) {
        Map<String, String> map = new HashMap<>();

        // 去掉末尾的"&"
        conditionStr = conditionStr.substring(0, conditionStr.length() - 1);

        // 使用 "&" 和 "=" 分割字符串，将结果存入 HashMap
        for (String s : conditionStr.split("&")) {
            String[] pair = s.split("=");
            map.put(pair[0], pair[1]);
        }
        return map;

    }

    public Map<String, Object> getCondition(String key) {
        Map<String, Object> ans = new HashMap<>();
        if (StringUtils.hasText(key)) {
            String[] a = key.split("\\+");
            if (a.length >= 3) {
                ans.put("class_name", a[0]);
                ans.put("method_name", a[1]);
                ans.put("condition", getConditionOfStr(a[2]));
            }
        }
        return ans;
    }


    public String devCache() {
        return "目前缓存的大小为：" + this.cache.size() + ",超时的大小为:" + this.seconds.size() + ",ttl的大小为:" + this.ttls.size();
    }

    public String getCacheConfig(String key) {
        String a = this.cache.get(key);
        if (StringUtils.hasText(a)) {
            Long time = this.ttls.get(key);
            if (Objects.nonNull(time)) {
                return "包含key:" + key + " 结果为：" + a + " 超时时间为：" + time;
            } else {
                return "包含key:" + key + " 结果为：" + a + " 已超时";
            }
        }
        return "不包含该KEY";
    }

    public List<String> getAllKeys() {
        return new ArrayList<>(ttls.keySet());
    }

}
