package com.hyutils.core.simple;

import com.hyutils.core.instance.PostgreSQLBaseQuery;
import com.hyutils.core.utils.Json;
import com.hyutils.core.utils.StringFormatUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单查询
 */
public class SimpleQuery extends PostgreSQLBaseQuery<Map<String, Object>> {

    private Class thisClass;

    public <T> SimpleQuery(Class<T> tClass) {
        super();
        this.primaryKey = "id";
        this.fieldOrgName = "created_org_id";
        this.table = this.camelToUnderscoreConverter.convert(tClass.getSimpleName());
        thisClass = tClass;
    }

    private <T> T map2Object(Map<String, Object> map) {
        try {
            T instance = (T) thisClass.getDeclaredConstructor().newInstance();
            for (Field field : thisClass.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                String small = StringFormatUtils.camel(fieldName);
                Object value = map.get(small);
                if (value != null) {
                    if (value instanceof Timestamp && field.getType().equals(LocalDateTime.class)) {
                        LocalDateTime dateTime = ((Timestamp) value).toLocalDateTime();
                        field.set(instance, dateTime);
                    } else {
                        field.set(instance, value);
                    }
                }
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> List<T> listMap2Object(List<Map<String, Object>> source) {
        List<T> result = new ArrayList<>();
        for (Map<String, Object> map : source) {
            try {
                T instance = (T) thisClass.getDeclaredConstructor().newInstance();
                for (Field field : thisClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    String small = StringFormatUtils.camel(fieldName);
                    Object value = map.get(small);
                    if (value != null) {
                        if (value instanceof Timestamp && field.getType().equals(LocalDateTime.class)) {
                            LocalDateTime dateTime = ((Timestamp) value).toLocalDateTime();
                            field.set(instance, dateTime);
                        } else {
                            field.set(instance, value);
                        }
                    }
                }
                result.add(instance);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public Long count(Map<String, Object> condition) {
        return this.countModelBySimpleAnd(condition);
    }

    public <T> Long count(T condition) {
        return count(Json.toMap(Json.toJson(condition)));
    }

    public List<Map<String, Object>> pageReturnListMap(Map<String, Object> condition, Integer page, Integer size) {
        return this.findListModelBySimpleAnd(condition, page, size);
    }

    public <T> List<Map<String, Object>> pageReturnListMap(T condition, Integer page, Integer size) {
        return pageReturnListMap(Json.toMap(Json.toJson(condition)), page, size);
    }

    public <T> List<T> page(Map<String, Object> condition, Integer page, Integer size) {
        return listMap2Object(pageReturnListMap(condition, page, size));
    }

    public <T> List<T> page(T condition, Integer page, Integer size) {
        return page(Json.toMap(Json.toJson(condition)), page, size);
    }

    public Integer updateByCondition(Map<String, Object> condition, Map<String, Object> value) {
        return this.update(condition, value);
    }

    public <T> Integer updateByCondition(T condition, T value) {
        return updateByCondition(Json.toMap(Json.toJson(condition)), Json.toMap(Json.toJson(value)));
    }

    public Integer updateById(Object id, Map<String, Object> value) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("id", id);
        return this.updateByCondition(condition, value);
    }

    public <T> Integer updateById(Object id, T value) {
        return updateById(id, Json.toMap(Json.toJson(value)));
    }

    public List<Map<String, Object>> findByConditionReturnListMap(Map<String, Object> condition) {
        return this.findListModelBySimpleAnd(condition);
    }

    public <T> List<Map<String, Object>> findByConditionReturnListMap(T condition) {
        return findByConditionReturnListMap(Json.toMap(Json.toJson(condition)));
    }

    public <T> List<T> findByCondition(Map<String, Object> condition) {
        return listMap2Object(findByConditionReturnListMap(condition));
    }

    public <T> List<T> findByCondition(T condition) {
        return findByCondition(Json.toMap(Json.toJson(condition)));
    }

    public <T> T simpleGetById(Object id) {
        return map2Object(findModelById(id));
    }

    public List<Map<String, Object>> pageReturnListMap(Map<String, Object> crawlTaskHistory, List<Long> orgIds, Integer page, Integer size) {
        return this.findListModelByOperateSimpleAnd(mergeCondition(crawlTaskHistory, orgIds, this.fieldOrgName), page, size);
    }

    public <T> List<Map<String, Object>> pageReturnListMap(T condition, List<Long> orgIds, Integer page, Integer size) {
        return pageReturnListMap(Json.toMap(Json.toJson(condition)), orgIds, page, size);
    }


    public <T> List<T> page(Map<String, Object> condition, List<Long> orgIds, Integer page, Integer size) {
        return listMap2Object(pageReturnListMap(condition, orgIds, page, size));
    }

    public <T> List<T> page(T condition, List<Long> orgIds, Integer page, Integer size) {
        return page(Json.toMap(Json.toJson(condition)), orgIds, page, size);
    }

    public Long count(Map<String, Object> condition, List<Long> orgIds) {
        return this.countModelByOperateSimpleAnd(mergeCondition(condition, orgIds, this.fieldOrgName));
    }

    public <T> Long count(T condition, List<Long> orgIds) {
        return count(Json.toMap(Json.toJson(condition)), orgIds);
    }

    public Integer updateByCondition(Map<String, Object> condition, List<Long> orgIds, Map<String, Object> value) {
        return this.update(mergeCondition(condition, orgIds, this.fieldOrgName), value);
    }

    public <T> Integer updateByCondition(T condition,List<Long> orgIds,T value){
        return updateByCondition(Json.toMap(Json.toJson(condition)),orgIds,Json.toMap(Json.toJson(value)));
    }


    public List<Map<String, Object>> findByConditionReturnListMap(Map<String, Object> condition, List<Long> orgIds) {
        return this.findListModelByOperateSimpleAnd(mergeCondition(condition, orgIds, this.fieldOrgName));
    }

    public <T> List<Map<String,Object>> findByConditionReturnListMap(T condition,List<Long> orgIds){
        return findByConditionReturnListMap(Json.toMap(Json.toJson(condition)),orgIds);
    }

    public <T> List<T> findByCondition(Map<String, Object> condition, List<Long> orgIds) {
        return listMap2Object(findByConditionReturnListMap(condition, orgIds));
    }


    public List<Map<String, Object>> findByConditionReturnListMap(Map<String, Object> condition, List<Long> orgIds, Long userId) {
        return this.findListModelByOperateSimpleAndWithOrgIds(condition, orgIds, userId, this.fieldUserName, this.fieldOrgName, "*", null, null);
    }

    public <T> List<Map<String, Object>> findByConditionReturnListMap(T condition, List<Long> orgIds, Long userId) {
        return findByConditionReturnListMap(Json.toMap(Json.toJson(condition)),orgIds,userId);
    }

    public <T> List<T> findByCondition(Map<String, Object> condition, List<Long> orgIds, Long userId) {
        return listMap2Object(findByConditionReturnListMap(condition, orgIds, userId));
    }


    public <T> List<T> findByCondition(T condition, List<Long> orgIds, Long userId) {
        return findByCondition(Json.toMap(Json.toJson(condition)),orgIds,userId);
    }

    public List<Map<String, Object>> pageReturnListMap(Map<String, Object> condition, List<Long> orgIds, Long userId, Integer page, Integer size) {
        return this.findListModelByOperateSimpleAndWithOrgIds(condition, orgIds, userId, this.fieldUserName, this.fieldOrgName, "*", page, size);
    }

    public <T> List<Map<String, Object>> pageReturnListMap(T condition, List<Long> orgIds, Long userId, Integer page, Integer size) {
        return pageReturnListMap(Json.toMap(Json.toJson(condition)),orgIds,userId,page,size);
    }

    public <T> List<T> page(Map<String, Object> condition, List<Long> orgIds, Long userId, Integer page, Integer size) {
        return listMap2Object(pageReturnListMap(condition, orgIds, userId, page, size));
    }

    public <T> List<T> page(T condition, List<Long> orgIds, Long userId, Integer page, Integer size) {
        return page(Json.toMap(Json.toJson(condition)),orgIds,userId,page,size);
    }

    public Long count(Map<String, Object> condition, List<Long> orgIds, Long userId) {
        return this.countByOperateSimpleAndWithOrgIds(condition, orgIds, userId, this.fieldUserName, this.fieldOrgName);
    }

    public <T> Long count(T condition, List<Long> orgIds, Long userId) {
        return count(Json.toMap(Json.toJson(condition)),orgIds,userId);
    }

    public Integer updateByCondition(Map<String, Object> condition, List<Long> orgIds, Long userId, Map<String, Object> value) {
        return this.updateWithOrgIdsAndUserId(condition, orgIds, userId, this.fieldUserName, this.fieldOrgName, value);
    }

    public <T> Integer updateByCondition(T condition, List<Long> orgIds, Long userId, T value) {
        return updateByCondition(Json.toMap(Json.toJson(condition)),orgIds,userId,Json.toMap(Json.toJson(value)));
    }


}
