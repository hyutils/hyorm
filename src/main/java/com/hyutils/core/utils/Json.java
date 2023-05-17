
package com.hyutils.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class Json {

    private static final Logger LOGGER = LoggerFactory.getLogger(Json.class);
    private static final String EMPTY_JSON_OBJECT = "{}";
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final ObjectMapper NOT_CHANGE_MAPPER = new ObjectMapper();

    /**
     * 日期格式化参数 格式 yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDatetimeDeserializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDatetimeSerializer());
        simpleModule.addSerializer(Timestamp.class, new TimestampSerializer());
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        NOT_CHANGE_MAPPER.registerModule(simpleModule);
        NOT_CHANGE_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转换成Json
     *
     * @param object Object
     * @return String
     */
    public static String toJson(Object object) {
        StringWriter sw = new StringWriter();
        if (Objects.isNull(object)) {
            return EMPTY_JSON_OBJECT;
        }
        try {
            MAPPER.writeValue(MAPPER.getFactory().createGenerator(sw), object);
        } catch (Exception ex) {
            LOGGER.error("Object to json occur error, detail:", ex);
        }
        return sw.toString();
    }

    /**
     * Json转换成对象
     *
     * @param jsonString json
     * @param tClass     类型
     * @param <T>        泛型参数
     * @return T
     */
    public static <T> T toObject(String jsonString, Class<T> tClass) {
        if (Objects.isNull(jsonString) || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonString, tClass);
        } catch (Exception ex) {
            ex.printStackTrace();
//            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    /**
     * Json转换成对象
     *
     * @param fileName 文件名称
     * @param tClass   类型
     * @param <T>      泛型参数
     * @return T
     */
    public static <T> T fileToObject(String fileName, Class<T> tClass) {
        if (Objects.isNull(fileName) || fileName.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(new File(fileName), tClass);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    /**
     * Json转换成对象
     *
     * @param <T>        泛型
     * @param jsonString json
     * @param reference  类型引用
     * @return T
     */
    public static <T> T toObject(String jsonString, TypeReference<T> reference) {
        if (Objects.isNull(jsonString) || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonString, reference);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    /**
     * 输入流转换成对象
     *
     * @param <T>       泛型
     * @param stream    输入流
     * @param reference 类型引用
     * @return T
     */
    public static <T> T toObject(InputStream stream, TypeReference<T> reference) {
        if (Objects.isNull(stream)) {
            return null;
        }
        try {
            return MAPPER.readValue(stream, reference);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    /**
     * 输入流转换成对象
     *
     * @param <T>    泛型
     * @param stream 输入流
     * @param tClass 类型引用
     * @return T
     */
    public static <T> T toObject(InputStream stream, Class<T> tClass) {
        if (Objects.isNull(stream)) {
            return null;
        }
        try {
            return MAPPER.readValue(stream, tClass);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    /**
     * JSON字符串转Map
     *
     * @param json JSON字符串
     * @return map
     */
    public static Map<String, Object> toMap(String json) {
        if (Objects.nonNull(json) && !json.isEmpty()) {
            String tmp = json.trim();
            if (tmp.charAt(0) == '{' && tmp.charAt(tmp.length() - 1) == '}') {
                return toObject(json, new TypeReference<Map<String, Object>>() {
                });
            }
        }
        return null;
    }

    private final static Map<String, Map<String, Pair<Field, Method>>> dynamicTypeFieldPairsCache = new HashMap<>();

    /**
     * LocalDatetime类型序列化
     */
    public static class LocalDatetimeSerializer extends JsonSerializer<LocalDateTime> {
        /**
         * 序列化LocalDateTime
         *
         * @param localDateTime      LocalDateTime
         * @param jsonGenerator      JsonGenerator
         * @param serializerProvider SerializerProvider
         * @throws IOException IOException
         */
        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            jsonGenerator.writeString(DATE_TIME_FORMATTER.format(localDateTime));
        }

        /**
         * 获取LocalDateTime class
         *
         * @return LocalDateTime.class
         */
        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }
    }

    /**
     * LocalDatetime类型反序列化
     */
    public static class LocalDatetimeDeserializer extends JsonDeserializer<LocalDateTime> {


        /**
         * LocalDateTime 反序列化
         *
         * @param jsonParser             JsonParser
         * @param deserializationContext DeserializationContext
         * @return LocalDateTime
         * @throws IOException IOException
         */
        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            TreeNode tree = jsonParser.getCodec().readTree(jsonParser);
            if (tree instanceof TextNode) {
                TextNode node = (TextNode) tree;
                String value = node.textValue();
                return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            }
            return null;
        }
    }


    /**
     * LocalDatetime类型序列化
     */
    public static class TimestampSerializer extends JsonSerializer<Timestamp> {
        /**
         * 序列化 Timestamp
         *
         * @param timestamp          Timestamp
         * @param jsonGenerator      JsonGenerator
         * @param serializerProvider SerializerProvider
         * @throws IOException IOException
         */
        @Override
        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            LocalDateTime localDateTime = null;
            if (Objects.nonNull(timestamp)) {
                localDateTime = timestamp.toLocalDateTime();
            }
            jsonGenerator.writeString(DATE_TIME_FORMATTER.format(localDateTime));
        }

        /**
         * 获取 Timestamp class
         *
         * @return Timestamp.class
         */
        @Override
        public Class<Timestamp> handledType() {
            return Timestamp.class;
        }
    }

    /**
     * 去除空数据
     * @param tmp
     * @return
     */
    public static Map<String, Object> removeNull(Map<String, Object> tmp) {
        Map<String, Object> ans = new HashMap<>();
        for (Map.Entry<String, Object> x : tmp.entrySet()) {
            if (Objects.nonNull(x.getValue()) && !x.getValue().equals("")) {
                ans.put(x.getKey(), x.getValue());
            }
        }
        return ans;
    }

    public static <T> T toObjectNotChangeParams(String jsonString, Class<T> tClass) {
        if (Objects.isNull(jsonString) || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return NOT_CHANGE_MAPPER.readValue(jsonString, tClass);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }

    public static <T> T toObjectNoChangeParam(String jsonString, TypeReference<T> reference) {
        if (Objects.isNull(jsonString) || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return NOT_CHANGE_MAPPER.readValue(jsonString, reference);
        } catch (Exception ex) {
            LOGGER.error("Json to object occur error, detail:", ex);
        }
        return null;
    }
    public static Map<String, Object> toMapNoChangeParam(String json) {
        if (Objects.nonNull(json) && !json.isEmpty()) {
            String tmp = json.trim();
            if (tmp.charAt(0) == '{' && tmp.charAt(tmp.length() - 1) == '}') {
                return toObjectNoChangeParam(json, new TypeReference<Map<String, Object>>() {
                });
            }
        }
        return null;
    }

    public static String toJsonNotChangeParams(Object object){
        StringWriter sw = new StringWriter();
        if (Objects.isNull(object)) {
            return EMPTY_JSON_OBJECT;
        }
        try {
            NOT_CHANGE_MAPPER.writeValue(NOT_CHANGE_MAPPER.getFactory().createGenerator(sw), object);
        } catch (Exception ex) {
            LOGGER.error("Object to json occur error, detail:", ex);
        }
        String tmp = sw.toString();
        return sw.toString();
    }

}