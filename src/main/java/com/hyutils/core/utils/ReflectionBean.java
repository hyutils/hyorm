

package com.hyutils.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public abstract class ReflectionBean {

    private static final Log logger;
    private static final Field[]                           EMPTY_FIELD_ARRAY;
    private static final Map<Class<?>, Field[]> declaredFieldsCache;
    private static final Map<Class<?>, Map<String, Field>> declaredFieldsMap;
    private static final Map<Class<?>, Integer> javaTypeToSqlTypeMap;


    static {

        logger               = LogFactory.getLog(ReflectionBean.class);
        EMPTY_FIELD_ARRAY    = new Field[0];
        declaredFieldsCache  = new ConcurrentReferenceHashMap<>(512);
        declaredFieldsMap    = new ConcurrentReferenceHashMap<>(512);
        javaTypeToSqlTypeMap = new HashMap<>(32);

        javaTypeToSqlTypeMap.put(int.class, Types.INTEGER);
        javaTypeToSqlTypeMap.put(long.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(byte.class, Types.TINYINT);
        javaTypeToSqlTypeMap.put(boolean.class, Types.BOOLEAN);
        javaTypeToSqlTypeMap.put(short.class, Types.SMALLINT);
        javaTypeToSqlTypeMap.put(float.class, Types.FLOAT);
        javaTypeToSqlTypeMap.put(double.class, Types.DOUBLE);
        javaTypeToSqlTypeMap.put(Boolean.class, Types.BOOLEAN);
        javaTypeToSqlTypeMap.put(Byte.class, Types.TINYINT);
        javaTypeToSqlTypeMap.put(Short.class, Types.SMALLINT);
        javaTypeToSqlTypeMap.put(Integer.class, Types.INTEGER);
        javaTypeToSqlTypeMap.put(Long.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(BigInteger.class, Types.BIGINT);
        javaTypeToSqlTypeMap.put(Float.class, Types.FLOAT);
        javaTypeToSqlTypeMap.put(Double.class, Types.DOUBLE);
        javaTypeToSqlTypeMap.put(BigDecimal.class, Types.DECIMAL);
        javaTypeToSqlTypeMap.put(java.sql.Date.class, Types.DATE);
        javaTypeToSqlTypeMap.put(java.sql.Time.class, Types.TIME);
        javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, Types.TIMESTAMP);
        javaTypeToSqlTypeMap.put(Blob.class, Types.BLOB);
        javaTypeToSqlTypeMap.put(Clob.class, Types.CLOB);
    }

    /**
     * 获取类型Field清单
     *
     * @param clazz 类型
     * @return Field数组
     */
    public static Field[] allFields(Class<?> clazz) {
        Assert.notNull(clazz, "类型信息不能为: null.");
        Field[] result = declaredFieldsCache.get(clazz);
        if (result == null) {
            try {
                result = clazz.getDeclaredFields();
                declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
            } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {logger.error("获取类型Field列表失败,类型信息：" + clazz.getName());}
                throw new IllegalStateException("获取类型Field列表失败,类型信息：" + clazz.getName());
            }
        }
        return result;
    }

    /**
     * 获取类型Field字典
     *
     * @param clazz 类型
     * @return Field数组
     */
    public static Map<String, Field> allFieldMaps(Class<?> clazz) {

        Assert.notNull(clazz, "类型信息不能为: null.");
        Map<String, Field> fieldMap = declaredFieldsMap.get(clazz);

        if (fieldMap == null) {
            try {
                Field[] fields = allFields(clazz);
                fieldMap = new HashMap<>();

                for (Field field : fields) {
                    fieldMap.put(field.getName(), field);
                }
                declaredFieldsMap.put(clazz, fieldMap);
            } catch (Throwable ex) {
                if (logger.isDebugEnabled()) {logger.error("获取类型Field列表失败,类型信息：" + clazz.getName());}
                throw new IllegalStateException("获取类型Field列表失败,类型信息：" + clazz.getName());
            }
        }

        return fieldMap;
    }

    /**
     * 设置字段值
     *
     * @param field  字段实例
     * @param target 目标对象
     * @param value  值
     */
    public static void setField(Field field, Object target, Object value) {
        if (ObjectUtils.isEmpty(target)) {return;}
        try {
            if (!field.isAccessible()) {field.setAccessible(true);}
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            if (logger.isDebugEnabled()) {
                logger.error("设置字段值发生错误，类型：" + target.getClass().getName() + ",字段：" + field.getName());
            }
        }
    }


    /**
     * 获取指定实例的字段值
     *
     * @param field  字段名称
     * @param target 实例对象
     * @return 字段值
     */
    public static Object fieldValue(Field field, Object target) {
        if (!field.isAccessible()) {field.setAccessible(true);}
        try {
            Object value = field.get(target);
            if (value.getClass().equals(InetAddress.class)) {}
            return value;
        } catch (IllegalAccessException ex) { logger.error("获取值失败"); }
        return null;
    }

    /**
     * 获取指定实例的字段值
     *
     * @param field  字段名称
     * @param target 实例对象
     * @return 字段值
     */
    public static boolean notNull(Field field, Object target) {
        return fieldValue(field, target) != null;
    }

    /**
     * Java类型 转换 SQL类型
     *
     * @param javaType Java 类型
     * @return SQL类型
     */
    public static int javaTypeToSqlParameterType(@Nullable Class<?> javaType) {

        if (javaType == null) { return SqlTypeValue.TYPE_UNKNOWN; }

        Integer sqlType = javaTypeToSqlTypeMap.get(javaType);
        if (sqlType != null) { return sqlType; }

        if (Number.class.isAssignableFrom(javaType)) { return Types.NUMERIC; }
        if (isStringValue(javaType)) { return Types.VARCHAR; }
        if (isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType)) { return Types.TIMESTAMP; }
        return SqlTypeValue.TYPE_UNKNOWN;
    }

    /**
     * 判断类型是否是字符串类型
     *
     * @param inValueType 类型信息
     * @return 是否字符串类型
     */
    private static boolean isStringValue(Class<?> inValueType) {
        return (CharSequence.class.isAssignableFrom(inValueType) ||
                StringWriter.class.isAssignableFrom(inValueType));
    }

    /**
     * 判断是否日期、时间类型
     *
     * @param inValueType 输入类型
     * @return 是否日期类型
     */
    private static boolean isDateValue(Class<?> inValueType) {
        return (java.util.Date.class.isAssignableFrom(inValueType) &&
                !(java.sql.Date.class.isAssignableFrom(inValueType) ||
                  java.sql.Time.class.isAssignableFrom(inValueType) ||
                  java.sql.Timestamp.class.isAssignableFrom(inValueType)));
    }
}
