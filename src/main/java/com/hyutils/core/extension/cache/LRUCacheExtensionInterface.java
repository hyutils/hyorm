package com.hyutils.core.extension.cache;



import com.hyutils.core.utils.SpringContextUtil;

import java.util.Map;

/**
 * 缓存扩展
 * 本地缓存
 */
public interface LRUCacheExtensionInterface {

    default String getFromCache(String className, String methodName, Map<String, Object> condition) {
        return SpringContextUtil.getBean(LruCacheWithTTLDateBase.class).getCacheJson(className, methodName, condition);
    }

    default Boolean putToCache(String className, String methodName, Map<String, Object> condition, String value) {
        return SpringContextUtil.getBean(LruCacheWithTTLDateBase.class).putCacheJson(className, methodName, condition, value);
    }
}
