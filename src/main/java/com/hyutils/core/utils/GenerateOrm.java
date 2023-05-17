package com.hyutils.core.utils;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成数据库层
 */
public class GenerateOrm {


    static class Field {
        String name;
        String type;
        String desc;
        Integer key;

        public Field(String name, String type, String desc) {
            this.name = name;
            this.type = type;
            this.desc = desc;
            this.key = Math.abs((name + MathUtils.randomGen(10)).hashCode());
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public static class Table {
        String name;
        List<Field> fields;
        String desc;
        Integer key;

        public Table(String name, List<Field> fields, String desc) {
            this.name = name;
            this.fields = new ArrayList<>();
            this.fields.addAll(fields);
            this.desc = desc;
            this.key = Math.abs((name + MathUtils.randomGen(10)).hashCode());
        }

        public Integer getKey() {
            return key;
        }

        public void setKey(Integer key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public static String importStr(List<String> types) {
        StringBuilder ans = new StringBuilder();
        if (types.contains("LocalDateTime")) {
            ans.append("import java.time.LocalDateTime;\n");
        }
        if (types.contains("LocalDate")) {
            ans.append("import java.time.LocalDate;\n");
        }
        ans.append("import com.hyutils.core.extension.like.LikeParamExtension;\n");
        ans.append("import com.hyutils.core.extension.selectfield.SelectFields;\n" +
                "import com.hyutils.core.extension.selectorder.SelectOrders;\n");
        ans.append("import java.util.Objects;\n");
        ans.append("import java.util.List;\n");
        return ans.toString();
    }


    public static String sqlTypeExchange2Java(String b) {
        if (b.contains("int8") || b.contains("Int64")) {
            return "Long";
        }
        // TODO: 2022/5/31 修改
        if (b.contains("int4") || b.contains("Int8") || b.contains("Int16") || b.contains("Int32")) {
            return "Integer";
        }
        if (b.contains("timestamp") || b.contains("DateTime64") || b.contains("DateTime32")) {
            return "LocalDateTime";
        }
        if (b.contains("boolean")) {
            return "Boolean";
        }
        if (b.contains("date")) {
            return "LocalDate";
        }
        if (b.contains("uuid") || b.contains("UUID")) {
            return "String";
        }
        if (b.contains("varchar") || b.contains("String")) {
            return "String";
        }
        if (b.contains("float4")) {
            return "Double";
        }
        return "";
    }


    public static String getModelStr(Table table) {
        String name = StringFormatUtils.snake(table.name, true);
        String desc = "//" + table.desc + "\n";
        StringBuilder ans = new StringBuilder().append(String.format("public class %s {\n", name));
        List<String> types = new ArrayList<>();
        for (Field field : table.fields) {
            String type = sqlTypeExchange2Java(field.type);
            String fieldName = StringFormatUtils.snake(field.name, false);
            ans.append(String.format("    private %s %s;         //%s \n", sqlTypeExchange2Java(field.type),
                    StringFormatUtils.snake(field.name, false),
                    field.desc));
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("LocalDateTime")) {
                ans.append(StringFormatUtils.formatByName("private {type} {fieldName}BiggerThan; // orm层需要，无实际意义\n" +
                        "    private {type} {fieldName}LowerThan; // orm层需要，无实际意义\n", new HashMap<String, Object>() {
                    {
                        put("type", type);
                        put("fieldName", fieldName);
                    }
                }));
            }
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("Long") || sqlTypeExchange2Java(field.type).equalsIgnoreCase("String") || sqlTypeExchange2Java(field.type).equalsIgnoreCase("Integer")) {
                if (StringFormatUtils.snake(field.name, false).equalsIgnoreCase("id")) {
                    ans.append(StringFormatUtils.formatByName(" private List<{type}> {sma_name}List; //orm层需要，无实际意义\n", new HashMap<String, Object>() {
                        {
                            put("sma_name", StringFormatUtils.snake(field.name, false));
                            put("big_name", StringFormatUtils.snake(field.name, true));
                            put("type", sqlTypeExchange2Java(field.type));
                        }
                    }));
                    ans.append(StringFormatUtils.formatByName(" private List<{type}> {sma_name}NotList; //orm层需要，无实际意义\n", new HashMap<String, Object>() {
                        {
                            put("sma_name", StringFormatUtils.snake(field.name, false));
                            put("big_name", StringFormatUtils.snake(field.name, true));
                            put("type", sqlTypeExchange2Java(field.type));
                        }
                    }));
                }
            }
            types.add(sqlTypeExchange2Java(field.type));
        }
        ans.append("private SelectOrders selectOrders; //排序字段，orm层需要\n");
        ans.append("private SelectFields selectFields; //待查询的字段\n");
        StringBuilder builderStr = new StringBuilder(String.format("\n\npublic static Builder builder(){\n" +
                "        return new Builder();\n" +
                "    }\n\npublic static class Builder {\n" +
                "private %s target;\n\npublic Builder() {\n" +
                "            this.target = new %s();\n" +
                "        }\n\npublic %s build() {\n" +
                "            return target;\n" +
                "        }", StringFormatUtils.snake(table.name, true), StringFormatUtils.snake(table.name, true), StringFormatUtils.snake(table.name, true)));
        for (Field field : table.fields) {
            ans.append(String.format("    public %s get%s() {\n" +
                            "        return %s;\n" +
                            "    }\n", sqlTypeExchange2Java(field.type),
                    StringFormatUtils.snake(field.name, true),
                    StringFormatUtils.snake(field.name, false)));
            ans.append(String.format("    public void set%s(%s %s) {\n" +
                            "        this.%s = %s;\n" +
                            "    }\n", StringFormatUtils.snake(field.name, true),
                    sqlTypeExchange2Java(field.type),
                    StringFormatUtils.snake(field.name, false),
                    StringFormatUtils.snake(field.name, false),
                    StringFormatUtils.snake(field.name, false)));
            String type = sqlTypeExchange2Java(field.type);
            if (type.equalsIgnoreCase("LocalDateTime")) {
                ans.append(StringFormatUtils.formatByName("public {type} get{big_name}BiggerThan() {\n" +
                        "        return {sma_name}BiggerThan;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void set{big_name}BiggerThan({type} {sma_name}BiggerThan) {\n" +
                        "        this.{sma_name}BiggerThan = {sma_name}BiggerThan;\n" +
                        "    }\n" +
                        "\n" +
                        "    public {type} get{big_name}LowerThan() {\n" +
                        "        return {sma_name}LowerThan;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void set{big_name}LowerThan({type} {sma_name}LowerThan) {\n" +
                        "        this.{sma_name}LowerThan = {sma_name}LowerThan;\n" +
                        "    }", new HashMap<String, Object>() {
                    {
                        put("sma_name", StringFormatUtils.snake(field.name, false));
                        put("big_name", StringFormatUtils.snake(field.name, true));
                        put("type", sqlTypeExchange2Java(field.type));
                    }
                }));
            }
            if (type.equalsIgnoreCase("Long") || type.equalsIgnoreCase("String") || type.equalsIgnoreCase("Integer")) {
                if (StringFormatUtils.snake(field.name, false).equalsIgnoreCase("id")) {
                    ans.append(StringFormatUtils.formatByName("public List<{type}> get{big_name}List() {\n" +
                            "        return {sma_name}List;\n" +
                            "    }\n" +
                            "\n" +
                            "    public void set{big_name}List(List<{type}> {sma_name}List) {\n" +
                            "        this.{sma_name}List = {sma_name}List;\n" +
                            "    }\n", new HashMap<String, Object>() {
                        {
                            put("sma_name", StringFormatUtils.snake(field.name, false));
                            put("big_name", StringFormatUtils.snake(field.name, true));
                            put("type", sqlTypeExchange2Java(field.type));
                        }
                    }));
                }
            }
            builderStr.append(StringFormatUtils.formatByName("public Builder {sma_field}({field_type} {sma_field}) {\n" +
                            "            this.target.set{big_field}({sma_field});\n" +
                            "            return this;\n" +
                            "        }\n"
                    , new HashMap<String, Object>() {
                        {
                            put("sma_field", StringFormatUtils.snake(field.name, false));
                            put("big_field", StringFormatUtils.snake(field.name, true));
                            put("field_type", sqlTypeExchange2Java(field.type));
                        }
                    }));
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("string")) {
                builderStr.append(StringFormatUtils.formatByName("public Builder {sma_field}Like(String {sma_field}) {\n" +
                        "if (Objects.isNull({sma_field})){\n" +
                        "                this.target.set{big_field}({sma_field});\n" +
                        "                return this;\n" +
                        "            }\n" +
                        "            this.target.set{big_field}(LikeParamExtension.PARAM_LEFT_LIKE + {sma_field} + LikeParamExtension.PARAM_RIGHT_LIKE);\n" +
                        "            return this;\n" +
                        "        }\n" +
                        "\n" +
                        "        public Builder {sma_field}LeftLike(String {sma_field}) {\n" +
                        "if (Objects.isNull({sma_field})){\n" +
                        "                this.target.set{big_field}({sma_field});\n" +
                        "                return this;\n" +
                        "            }\n" +
                        "            this.target.set{big_field}(LikeParamExtension.PARAM_LEFT_LIKE + {sma_field});\n" +
                        "            return this;\n" +
                        "        }\n" +
                        "\n" +
                        "        public Builder {sma_field}RightLike(String {sma_field}) {\n" +
                        "if (Objects.isNull({sma_field})){\n" +
                        "                this.target.set{big_field}({sma_field});\n" +
                        "                return this;\n" +
                        "            }\n" +
                        "            this.target.set{big_field}({sma_field} + LikeParamExtension.PARAM_RIGHT_LIKE);\n" +
                        "            return this;\n" +
                        "        }\n" +
                        "public Builder {sma_field}Not({field_type} {sma_field}) {\n" +
                        "                        if (Objects.isNull({sma_field})){\n" +
                        "                                        this.target.set{big_field}({sma_field});\n" +
                        "                                        return this;\n" +
                        "                                    }\n" +
                        "                                    this.target.set{big_field}(\"-1*\"+{sma_field});\n" +
                        "                                    return this;\n" +
                        "                                }\n", new HashMap<String, Object>() {
                    {
                        put("sma_field", StringFormatUtils.snake(field.name, false));
                        put("big_field", StringFormatUtils.snake(field.name, true));
                        put("field_type", sqlTypeExchange2Java(field.type));
                    }
                }));
            }
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("long")) {
                builderStr.append(StringFormatUtils.formatByName(
                        "public Builder {sma_field}Not({field_type} {sma_field}) {\n" +
                                "                        if (Objects.isNull({sma_field})){\n" +
                                "                                        this.target.set{big_field}({sma_field});\n" +
                                "                                        return this;\n" +
                                "                                    }\n" +
                                "                                    this.target.set{big_field}(-1L*{sma_field});\n" +
                                "                                    return this;\n" +
                                "                                }\n", new HashMap<String, Object>() {
                            {
                                put("sma_field", StringFormatUtils.snake(field.name, false));
                                put("big_field", StringFormatUtils.snake(field.name, true));
                                put("field_type", sqlTypeExchange2Java(field.type));
                            }
                        }));
            }
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("integer")) {
                builderStr.append(StringFormatUtils.formatByName(
                        "public Builder {sma_field}Not({field_type} {sma_field}) {\n" +
                                "                        if (Objects.isNull({sma_field})){\n" +
                                "                                        this.target.set{big_field}({sma_field});\n" +
                                "                                        return this;\n" +
                                "                                    }\n" +
                                "                                    this.target.set{big_field}(-1*{sma_field});\n" +
                                "                                    return this;\n" +
                                "                                }\n"
                        , new HashMap<String, Object>() {
                            {
                                put("sma_field", StringFormatUtils.snake(field.name, false));
                                put("big_field", StringFormatUtils.snake(field.name, true));
                                put("field_type", sqlTypeExchange2Java(field.type));
                            }
                        }));
            }
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("LocalDateTime")) {
                builderStr.append(StringFormatUtils.formatByName(
                        "public Builder {sma_field}BiggerThan({field_type} {sma_field}) {\n" +
                                "            this.target.set{big_field}BiggerThan({sma_field});\n" +
                                "            return this;\n" +
                                "        }\n" +
                                "\n" +
                                "public Builder {sma_field}LowerThan({field_type} {sma_field}) {\n" +
                                "            this.target.set{big_field}LowerThan({sma_field});\n" +
                                "            return this;\n" +
                                "        }\n" +
                                "\n", new HashMap<String, Object>() {
                            {
                                put("sma_field", StringFormatUtils.snake(field.name, false));
                                put("big_field", StringFormatUtils.snake(field.name, true));
                                put("field_type", sqlTypeExchange2Java(field.type));
                            }
                        }));
            }
            if (sqlTypeExchange2Java(field.type).equalsIgnoreCase("Long")
                    || sqlTypeExchange2Java(field.type).equalsIgnoreCase("Integer")
                    || sqlTypeExchange2Java(field.type).equalsIgnoreCase("String")) {
                if (StringFormatUtils.snake(field.name, false).equalsIgnoreCase("id")) {
                    builderStr.append(StringFormatUtils.formatByName("public Builder {sma_field}List(List<{field_type}> {sma_field}List) {\n" +
                            "            this.target.set{big_field}List({sma_field}List);\n" +
                            "            return this;\n" +
                            "        }\n", new HashMap<String, Object>() {
                        {
                            put("sma_field", StringFormatUtils.snake(field.name, false));
                            put("big_field", StringFormatUtils.snake(field.name, true));
                            put("field_type", sqlTypeExchange2Java(field.type));
                        }
                    }));
                }
            }

        }
        ans.append("public SelectOrders getSelectOrders() {\n" +
                "        return selectOrders;\n" +
                "    }\n" +
                "\n" +
                "    public void setSelectOrders(SelectOrders selectOrders) {\n" +
                "        this.selectOrders = selectOrders;\n" +
                "    }\n" +
                "\n" +
                "    public SelectFields getSelectFields() {\n" +
                "        return selectFields;\n" +
                "    }\n" +
                "\n" +
                "    public void setSelectFields(SelectFields selectFields) {\n" +
                "        this.selectFields = selectFields;\n" +
                "    }\n");
        String importStr = importStr(types);
        builderStr.append("public Builder selectOrders(SelectOrders selectOrders) {\n" +
                "            this.target.selectOrders = selectOrders;\n" +
                "            return this;\n" +
                "        }\n" +
                "\n");
        builderStr.append(StringFormatUtils.formatByName("public Builder selectFields({big_field} {sma_field}) {\n" +
                "            Map<String, Object> x = Json.toMap(Json.toJson({sma_field}));\n" +
                "            Map<String, Object> y = StringFormatUtils.removeNull(x);\n" +
                "            if (y.size() == 0) return this;\n" +
                "            SelectFields selectFields = new SelectFields();\n" +
                "            for (String z : y.keySet()) {\n" +
                "                selectFields.select(z);\n" +
                "            }\n" +
                "            this.target.selectFields = selectFields;\n" +
                "            return this;\n" +
                "        }\n", new HashMap<String, Object>() {
            {
                put("sma_field", StringFormatUtils.snake(table.name, false));
                put("big_field", StringFormatUtils.snake(table.name, true));
            }
        }));

        builderStr.append("\n\n}");

        return importStr + "\n" + desc + ans.toString() + builderStr.toString() + "\n" + "}";
    }


    public static String getRepositoryStr(Table table, String packageName, String baseJar) {
        Map<String, Object> params = new HashMap<>();

        String origin = "import {package}.model.{big};\n" +
                "import {package}.query.{big}Query;\n" +
                "import {base_jar}.utils.Json;\n" +
                "import org.javatuples.Triplet;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "import java.util.*;\n" +
                "import {base_jar}.syntaxtree.AndWhereSyntaxTree;\n" +
                "import {base_jar}.syntaxtree.WhereSyntaxTree;\n" +
                "import org.springframework.util.StringUtils;\n" +
                "import {base_jar}.extension.visible.VisibleUser;\n" +
                "\n" +
                "public interface {big}Repository {\n" +
                "\n" +
                "    default {big} find{big}ById(Long id) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findModelById(id);\n" +
                "    }\n" +
                "\n" +
                "    default {big} find{big}ByCondition({big} condition) {\n" +
                "        String json = Json.toJson(condition);\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findModelBySimpleAnd(andCondition);\n" +
                "    }\n" +
                "\n" +
                "    default Long save({big} {sma}) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.insert(Json.toMap(Json.toJson({sma})));\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    default Integer saveAll(List<{big}> {sma}s) {\n" +
                "        List<Map<String, Object>> {sma}Maps = new ArrayList<>();\n" +
                "        for ({big} {sma} : {sma}s) {\n" +
                "            {sma}Maps.add(Json.toMap(Json.toJson({sma})));\n" +
                "        }\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.batchInsert({sma}Maps);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> findAll() {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        List<Map<String, Object>> tmp = query.findAll();\n" +
                "        List<{big}> ans = new ArrayList<>();\n" +
                "        for (Map<String, Object> x : tmp) {\n" +
                "            ans.add(Json.toObject(Json.toJson(x), {big}.class));\n" +
                "        }\n" +
                "        return ans;\n" +
                "    }\n" +
                "\n" +
                "    default Integer update({big} condition, {big} value) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateByCondition(condition, value);\n" +
                "    }\n" +
                "    default Integer updateById(Long id, {big} value) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateById(id, Json.toMap(Json.toJson(value)));\n" +
                "    }\n" +
                "\n" +
                "default Integer update({big} condition, {big} value, Integer version) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateByCondition(condition, value, version);\n" +
                "    }\n" +
                "\n" +
                "    default Integer updateById(Long id, {big} value, Integer version) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateById(id, Json.toMap(Json.toJson(value)), version);\n" +
                "    }\n" +
                "    default Long count({big} {sma}) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.count({sma});\n" +
                "    }\n" +
                "\n" +
                "    default Long count(List<Triplet<String, String, Object>> {sma}) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.count({sma});\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> page({big} {sma}, Integer page, Integer size) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.page({sma}, page, size);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> page(List<Triplet<String, String, Object>> {sma}, Integer page, Integer size) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.findListModelByOperateSimpleAnd({sma}, page, size);\n" +
                "    }\n" +
//                "default Integer updateWithCheckLog({big} condition, {big} value, Long createdId) {\n" +
//                "        {big}Query query = new {big}Query();\n" +
//                "        return query.updateByConditionWithCheckLog(condition, value, createdId);\n" +
//                "    }\n" +
//                "\n" +
//                "    default Integer updateByIdWithCheckLog(Long id, {big} value, Long createdOrgId, Long createdId) {\n" +
//                "        {big}Query query = new {big}Query();\n" +
//                "        return query.updateByIdWithCheckLog(id, value, createdOrgId, createdId);\n" +
//                "    }\n" +
//                "\n" +
//                "    default Long saveWithCheckLog({big} value, Long createdOrgId, Long createdId) {\n" +
//                "        {big}Query query = new {big}Query();\n" +
//                "        return query.saveWithCheckLog(value, createdOrgId, createdId);\n" +
//                "    }\n" +
                "    default List<{big}> findAllByCondition({big} condition){\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(condition);\n" +
                "    }\n" +
                "    default List<{big}> findAllWithIn(Map<String, List<Triplet<String, String, Object>>> inMaps) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        Map<String, Object> andCondition = new HashMap<>();\n" +
                "        andCondition.put(\"tag\", 0);\n" +
                "        WhereSyntaxTree whereSyntaxTree = new WhereSyntaxTree();\n" +
                "        for (Map.Entry<String, List<Triplet<String, String, Object>>> map : inMaps.entrySet()) {\n" +
                "            List<Triplet<String, String, Object>> orCondition = map.getValue();\n" +
                "            andCondition.put(map.getKey(), whereSyntaxTree.createOrTreeByOperate(orCondition));\n" +
                "        }\n" +
                "        AndWhereSyntaxTree andWhereSyntaxTree = whereSyntaxTree.createAndTree(andCondition);\n" +
                "        List<Map<String, Object>> ans = query.find(\"*\").where(andWhereSyntaxTree).listMapGet();\n" +
                "        List<{big}> {sma}s = new ArrayList<>();\n" +
                "        for (Map<String, Object> x : ans) {\n" +
                "            {sma}s.add(Json.toObject(Json.toJson(x),{big}.class));\n" +
                "        }\n" +
                "        return {sma}s;\n" +
                "    }\n" +
                "    default {big} find{big}ByIdWithCache(Long id) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String tmp = query.getFromCache(\"{big}Repository\", \"find{big}ByIdWithCache\", new HashMap<String, Object>() {\n" +
                "            {\n" +
                "                put(\"id\", id);\n" +
                "            }\n" +
                "        });\n" +
                "        if (StringUtils.hasText(tmp)) {\n" +
                "            return Json.toObject(tmp, {big}.class);\n" +
                "        } else {\n" +
                "            {big} {sma} = query.findModelById(id);\n" +
                "            if (Objects.nonNull({sma})) {\n" +
                "                query.putToCache(\"{big}Repository\", \"find{big}ByIdWithCache\", new HashMap<String, Object>() {\n" +
                "                    {\n" +
                "                        put(\"id\", id);\n" +
                "                    }\n" +
                "                }, Json.toJson({sma}));\n" +
                "            }\n" +
                "            return {sma};\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    default {big} find{big}ByConditionWithCache({big} condition) {\n" +
                "        String json = Json.toJson(condition);\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String tmp = query.getFromCache(\"{big}Repository\", \"find{big}ByConditionWithCache\", andCondition);\n" +
                "        if (StringUtils.hasText(tmp)) {\n" +
                "            return Json.toObject(tmp, {big}.class);\n" +
                "        } else {\n" +
                "            {big} {sma} = query.findModelBySimpleAnd(andCondition);\n" +
                "            if (Objects.nonNull({sma})) {\n" +
                "                query.putToCache(\"{sma}Repository\",\"find{big}ByConditionWithCache\",andCondition,Json.toJson({sma}));\n" +
                "            }\n" +
                "            return {sma};\n" +
                "        }\n" +
                "    }\n" +
                "// TODO: 2021/12/29 数据组相关\n" +
                "    default List<{big}> findAllByCondition({big} condition, List<Long> orgIds) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(condition, orgIds);\n" +
                "    }\n" +
                "\n" +
//                "    default Integer updateWithCheckLog({big} condition, List<Long> orgIds, {big} value, Long createdId) {\n" +
//                "        {big}Query query = new {big}Query();\n" +
//                "        return query.updateByConditionWithCheckLog(condition, orgIds, value, createdId);\n" +
//                "    }\n" +
//                "\n" +
                "    default {big} findSysDataAuthByCondition({big} condition, List<Long> orgIds) {\n" +
                "        String json = Json.toJson(condition);\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findModelByOperateSimpleAnd(query.mergeCondition(andCondition, orgIds, query.fieldOrgName));\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> findAll(List<Long> orgIds) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(new {big}(), orgIds);\n" +
                "    }\n" +
                "\n" +
                "    default Integer update({big} condition, List<Long> orgIds, {big} value) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateByCondition(condition, orgIds, value);\n" +
                "    }\n" +
                "\n" +
                "    default Long count({big} {sma}, List<Long> orgIds) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.count({sma}, orgIds);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> page({big} {sma}, List<Long> orgIds, Integer page, Integer size) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.page({sma}, orgIds, page, size);\n" +
                "    }\n" +
                "    // TODO: 2022/1/6 数据组 + owner_id作为条件 \n" +
                "    default List<{big}> findAllByCondition({big} condition, List<Long> orgIds, Long ownerId) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(condition, orgIds, ownerId);\n" +
                "    }\n" +
                "\n" +
//                "    default Integer updateWithCheckLog({big} condition, List<Long> orgIds, Long ownerId, {big} value, Long createdId) {\n" +
//                "        {big}Query query = new {big}Query();\n" +
//                "        return query.updateByConditionWithCheckLog(condition, orgIds, ownerId, value, createdId);\n" +
//                "    }\n" +
//                "\n" +
                "    default {big} find{big}ByCondition({big} condition, List<Long> orgIds, Long ownerId) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        List<{big}> {sma}s = query.page(condition, orgIds, ownerId, 1, 1);\n" +
                "        if ({sma}s.size() != 0) {\n" +
                "            return {sma}s.get(0);\n" +
                "        }\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> findAll(List<Long> orgIds, Long ownerId) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(new {big}(), orgIds, ownerId);\n" +
                "    }\n" +
                "\n" +
                "    default Integer update({big} condition, List<Long> orgIds, Long ownerId, {big} value) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.updateByCondition(condition, orgIds, ownerId, value);\n" +
                "    }\n" +
                "\n" +
                "    default Long count({big} {sma}, List<Long> orgIds, Long ownerId) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.count({sma}, orgIds, ownerId);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> page({big} {sma}, List<Long> orgIds, Long ownerId, Integer page, Integer size) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.page({sma}, orgIds, ownerId, page, size);\n" +
                "    }\n" +
                "\n" +
                "// todo -- 可见范围相关，与数据组类似\n" +
                "    default List<{big}> page({big} {sma}, VisibleUser visibleUser, Integer page, Integer size) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.page({sma},visibleUser.getOrgIds(),visibleUser.getId(),page,size);\n" +
                "    }\n" +
                "\n" +
                "    default Long count({big} {sma}, VisibleUser visibleUser) {\n" +
                "        {big}Query {sma}Query = new {big}Query();\n" +
                "        return {sma}Query.count({sma}, visibleUser.getOrgIds(),visibleUser.getId());\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> findAll(VisibleUser visibleUser) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(new {big}(), visibleUser.getOrgIds(), visibleUser.getId());\n" +
                "    }\n" +
                "\n" +
                "    default {big} find{big}ByCondition({big} condition, VisibleUser visibleUser) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        List<{big}> {sma} = query.page(condition, visibleUser.getOrgIds(),visibleUser.getId(), 1, 1);\n" +
                "        if ({sma}.size() != 0) {\n" +
                "            return {sma}.get(0);\n" +
                "        }\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> findAllByCondition({big} condition, VisibleUser visibleUser) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        return query.findByCondition(condition, visibleUser.getOrgIds(),visibleUser.getId());\n" +
                "    }\n" +
                "// like 相关 \n" +
                "default {big} find{big}ByLikeCondition({big} condition) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson(condition);\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.findModelByOperateSimpleAnd(params);\n" +
                "    }\n" +
                "\n" +
                "    default {big} find{big}ByLikeConditionAndEqualCondition({big} likeCondition, {big} equalCondition) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson(likeCondition);\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        Map<String, Object> andCondition2 = Json.toMap(Json.toJson(equalCondition));\n" +
                "        for (Map.Entry<String, Object> x : andCondition2.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"=\", x.getValue()));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.findModelByOperateSimpleAnd(params);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> pageWithLike({big} {sma}, Integer page, Integer size) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson({sma});\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.findListModelByOperateSimpleAnd(params, page, size);\n" +
                "    }\n" +
                "\n" +
                "    default List<{big}> pageWithLikeAndEqual({big} like{big}, {big} equal{big}, Integer page, Integer size) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson(like{big});\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        Map<String, Object> andCondition2 = Json.toMap(Json.toJson(equal{big}));\n" +
                "        for (Map.Entry<String, Object> x : andCondition2.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"=\", x.getValue()));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.findListModelByOperateSimpleAnd(params, page, size);\n" +
                "    }\n" +
                "\n" +
                "    default Long countWithLike({big} {sma}) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson({sma});\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.count(params);\n" +
                "    }\n" +
                "\n" +
                "    default Long countWithLikeAndEqual({big} like{big},{big} equal{big}) {\n" +
                "        {big}Query query = new {big}Query();\n" +
                "        String json = Json.toJson(like{big});\n" +
                "        Map<String, Object> andCondition = Json.toMap(json);\n" +
                "        List<Triplet<String, String, Object>> params = new ArrayList<>();\n" +
                "        for (Map.Entry<String, Object> x : andCondition.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"like\", \"%\" + x.getValue() + \"%\"));\n" +
                "            }\n" +
                "        }\n" +
                "        Map<String, Object> andCondition2 = Json.toMap(Json.toJson(equal{big}));\n" +
                "        for (Map.Entry<String, Object> x : andCondition2.entrySet()) {\n" +
                "            if (Objects.nonNull(x.getValue())) {\n" +
                "                params.add(new Triplet<>(x.getKey(), \"=\", x.getValue()));\n" +
                "            }\n" +
                "        }\n" +
                "        return query.count(params);\n" +
                "    }" +
                "}";
        params.put("base_jar", baseJar);
        params.put("package", packageName);
        params.put("big", StringFormatUtils.snake(table.name, true));
        params.put("sma", StringFormatUtils.snake(table.name, false));
        return StringFormatUtils.formatByName(origin, params);
    }

    public static String getQueryStr(Table table, String packageName, String baseJar) {
        Map<String, Object> params = new HashMap<>();
        params.put("base_jar", baseJar);
        params.put("package", packageName);
        String origin = "import {package}.model.{bigTableName};\n" +
                "import {base_jar}.utils.Json;\n" +
                "import {base_jar}.instance.PostgreSQLBaseQuery;\n" +
                "import org.javatuples.Triplet;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import java.util.Objects;\n" +
                "\n" +
                "public class  {table} extends PostgreSQLBaseQuery<{bigTableName}> {\n" +
                "    public {bigTableName}Query() {\n" +
                "        super();\n" +
                "        this.primaryKey = \"id\";\n" +
                "        this.fieldOrgName = \"created_org_id\";\n" +
                "    }\n" +
                "\n" +
                "    public Long count({bigTableName} {smallTableName}) {\n" +
                "        return this.countModelBySimpleAnd(Json.toMap(Json.toJson({smallTableName})));\n" +
                "    }\n" +
                "\n" +
                "    public Long count(List<Triplet<String, String, Object>> {smallTableName}) {\n" +
                "        return this.countModelByOperateSimpleAnd({smallTableName});\n" +
                "    }\n" +
                "\n" +
                "    public List<{bigTableName}> page({bigTableName} {sma}, Integer page, Integer size) {\n" +
                "        return this.findListModelBySimpleAnd(Json.toMap(Json.toJson({smallTableName})), page, size);\n" +
                "    }\n" +
                "    \n" +
                "    public Integer updateByCondition({bigTableName} condition, {bigTableName} value) {\n" +
                "        return this.update(Json.toMap(Json.toJson(condition)), Json.toMap(Json.toJson(value)));\n" +
                "    }\n" +
                "    public Integer updateByCondition({bigTableName} condition, {bigTableName} value,Integer version) {\n" +
                "        return this.update(Json.toMap(Json.toJson(condition)), Json.toMap(Json.toJson(value)),version);\n" +
                "    }\n" +
                "    public List<{bigTableName}> findByCondition({bigTableName} condition) {\n" +
                "        return this.findListModelBySimpleAnd(Json.toMap(Json.toJson(condition)));\n" +
                "    }\n" +
//                "public Integer updateByConditionWithCheckLog({bigTableName} condition, {bigTableName} value,Long createdId) {\n" +
//                "        SysLog sysLog = new SysLog();\n" +
//                "        Map<String,Object> mapCondition = Json.toMap(Json.toJson(condition));\n" +
//                "        if (Objects.nonNull(mapCondition) && mapCondition.containsKey(\"created_org_id\")){\n" +
//                "            sysLog.setCreatedOrgId((Long) mapCondition.get(\"created_org_id\"));\n" +
//                "        }\n" +
//                "        sysLog.setCreatedId(createdId);\n" +
//                "        return this.updateWithLog(Json.toMap(Json.toJson(condition)), Json.toMap(Json.toJson(value)), new SysLogQuery(), sysLog);\n" +
//                "    }\n" +
//                "\n" +
//                "    public Long saveWithCheckLog({bigTableName} value,Long createdOrgId,Long createdId) {\n" +
//                "        SysLog sysLog = new SysLog();\n" +
//                "        sysLog.setCreatedOrgId(createdOrgId);\n" +
//                "        sysLog.setCreatedId(createdId);\n" +
//                "        return this.saveWithLog(Json.toMap(Json.toJson(value)), new SysLogQuery(), sysLog);\n" +
//                "    }\n" +
//                "\n" +
//                "    public Integer updateByIdWithCheckLog(Object primaryKey, {bigTableName} value,Long createdOrgId,Long createdId) {\n" +
//                "        SysLog sysLog = new SysLog();\n" +
//                "        sysLog.setCreatedOrgId(createdOrgId);\n" +
//                "        sysLog.setCreatedId(createdId);\n" +
//                "        return this.updateByIdWithLog(primaryKey, Json.toMap(Json.toJson(value)), new SysLogQuery(), sysLog);\n" +
//                "    }\n" +
                "// TODO: 2021/12/29 数据组相关的\n" +
                "    public List<{bigTableName}> page({bigTableName} {sma}, List<Long> orgIds, Integer page, Integer size) {\n" +
                "        return this.findListModelByOperateSimpleAnd(mergeCondition(Json.toMap(Json.toJson({sma})), orgIds, this.fieldOrgName), page, size);\n" +
                "    }\n" +
                "\n" +
                "    public Long count({bigTableName} {sma}, List<Long> orgIds) {\n" +
                "        return this.countModelByOperateSimpleAnd(mergeCondition(Json.toMap(Json.toJson({sma})), orgIds, this.fieldOrgName));\n" +
                "    }\n" +
                "\n" +
                "    public Integer updateByCondition({bigTableName} condition, List<Long> orgIds, {bigTableName} value) {\n" +
                "        return this.update(mergeCondition(Json.toMap(Json.toJson(condition)),orgIds,this.fieldOrgName), Json.toMap(Json.toJson(value)));\n" +
                "    }\n" +
                "\n" +
//                "    public Integer updateByConditionWithCheckLog({bigTableName} condition,List<Long> orgIds, {bigTableName} value, Long createdId) {\n" +
//                "        SysLog sysLog = new SysLog();\n" +
//                "        Map<String, Object> mapCondition = Json.toMap(Json.toJson(condition));\n" +
//                "        if (Objects.nonNull(mapCondition) && mapCondition.containsKey(\"created_org_id\")) {\n" +
//                "            sysLog.setCreatedOrgId((Long) mapCondition.get(\"created_org_id\"));\n" +
//                "        }\n" +
//                "        sysLog.setCreatedId(createdId);\n" +
//                "        return this.updateWithLogByOperate(mergeCondition(Json.toMap(Json.toJson(condition)),orgIds,this.fieldOrgName), Json.toMap(Json.toJson(value)), new SysLogQuery(), sysLog);\n" +
//                "    }\n" +
//                "\n" +
                "    public List<{bigTableName}> findByCondition({bigTableName} condition,List<Long> orgIds) {\n" +
                "        return this.findListModelByOperateSimpleAnd(mergeCondition(Json.toMap(Json.toJson(condition)),orgIds,this.fieldOrgName));\n" +
                "    }\n" +
                "// TODO: 2022/1/5 数据组 + owner_id 来实现\n" +
                "    public List<{bigTableName}> findByCondition({bigTableName} condition, List<Long> orgIds, Long userId) {\n" +
                "        return this.findListModelByOperateSimpleAndWithOrgIds(Json.toMap(Json.toJson(condition)), orgIds, userId, this.fieldUserName, this.fieldOrgName, \"*\", null, null);\n" +
                "    }\n" +
                "\n" +
                "    public List<{bigTableName}> page({bigTableName} condition, List<Long> orgIds, Long userId, Integer page, Integer size) {\n" +
                "        return this.findListModelByOperateSimpleAndWithOrgIds(Json.toMap(Json.toJson(condition)), orgIds, userId, this.fieldUserName, this.fieldOrgName, \"*\", page, size);\n" +
                "    }\n" +
                "\n" +
                "    public Long count({bigTableName} condition, List<Long> orgIds, Long userId) {\n" +
                "        return this.countByOperateSimpleAndWithOrgIds(Json.toMap(Json.toJson(condition)), orgIds, userId, this.fieldUserName, this.fieldOrgName);\n" +
                "    }\n" +
                "\n" +
                "    public Integer updateByCondition({bigTableName} condition, List<Long> orgIds, Long userId, {bigTableName} value) {\n" +
                "        return this.updateWithOrgIdsAndUserId(Json.toMap(Json.toJson(condition)), orgIds, userId, this.fieldUserName, this.fieldOrgName, Json.toMap(Json.toJson(value)));\n" +
                "    }\n" +
                "\n" +
//                "    public Integer updateByConditionWithCheckLog({bigTableName} condition, List<Long> orgIds, Long userId, {bigTableName} value, Long createdId) {\n" +
//                "        SysLog sysLog = new SysLog();\n" +
//                "        Map<String, Object> mapCondition = Json.toMap(Json.toJson(condition));\n" +
//                "        if (Objects.nonNull(mapCondition) && mapCondition.containsKey(\"created_org_id\")) {\n" +
//                "            sysLog.setCreatedOrgId((Long) mapCondition.get(\"created_org_id\"));\n" +
//                "        }\n" +
//                "        sysLog.setCreatedId(createdId);\n" +
//                "        return this.updateWithLogAndWithOrgIdsAndUserId(mapCondition, orgIds, userId, this.fieldUserName, this.fieldOrgName, Json.toMap(Json.toJson(value)), new SysLogQuery(), sysLog);\n" +
//                "    }\n" +
                "}";
        params.put("table", StringFormatUtils.snake(table.name, true) + "Query");
        params.put("bigTableName", StringFormatUtils.snake(table.name, true));
        params.put("smallTableName", StringFormatUtils.snake(table.name, false));
        params.put("sma", StringFormatUtils.snake(table.name, false));
        return StringFormatUtils.formatByName(origin, params);
    }

    public static String getImplStr(Table table, String packageName, String baseJar) {
        Map<String, Object> params = new HashMap<>();
        params.put("base_jar", baseJar);
        params.put("package", packageName);
        params.put("big", StringFormatUtils.snake(table.name, true));
        String origin = "import {package}.repository.{big}Repository;\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\n" +
                "import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;\n" +
                "import org.springframework.stereotype.Repository;\n" +
                "\n" +
                "\n" +
                "@Repository\n" +
                "public class {big}RepositoryImpl implements {big}Repository {\n" +
                "\n" +
                "    @Autowired\n" +
                "    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;\n" +
                "}";
        return StringFormatUtils.formatByName(origin, params);
    }

    public static List<String> generateAll(Table table, String packageName, String baseJar, String folderName) {
        String modelName = StringFormatUtils.snake(table.name, true);
        List<String> ans = new ArrayList<>();
        ans.add(getModelStr(table));
        ans.add(getQueryStr(table, packageName, baseJar));
        ans.add(getRepositoryStr(table, packageName, baseJar));
        ans.add(getImplStr(table, packageName, baseJar));
        return ans;
    }


    public static List<Table> getTableStrs(String fileName, Boolean isFile) {
        List<Table> tables = new ArrayList<>();
        List<String> lineContent = new ArrayList<>();
        if (isFile) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    lineContent.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lineContent = ArrayStrUtil.str2Array(fileName, "\n");
        }
        String line = "";
        String tableName = "";
        List<Field> fieldList = new ArrayList<>();
        String tableDesc = "";
        String tmpDesc = "";
        int n = lineContent.size();
        for (int i = 0; i < n; i++) {
            line = lineContent.get(i);
            if (line.toUpperCase().startsWith("CREATE TABLE IF NOT EXISTS")) {
                tableName = line.replace("CREATE TABLE IF NOT EXISTS", "").trim();
                tableDesc = tmpDesc;
                if (tableName.length() >= 32) {
                    System.out.println(tableName + "表长度大于32");
                    return new ArrayList<>();
                }
                fieldList = new ArrayList<>();
            }
            if (line.startsWith("* 表描述：")) {
                tmpDesc = line.replace("* 表描述：", "").trim();
            }
            if (line.startsWith("--")) {
                tmpDesc = line.replace("--", "").trim();
            }
            if (line.startsWith(" ") || line.startsWith("\t")) {
                line = line.trim();
                String[] a = line.split(" ");
                int cnt = 0;
                if (a.length >= 3) {
                    String name = "";
                    String type = "";
                    String desc = "";
                    for (String b : a) {
                        b = b.trim();
                        if (StringUtils.hasText(b)) {
                            String c = b.toLowerCase();
                            if (cnt == 0) {
                                name = c;
                                cnt += 1;
                            }
                            if (c.contains("bigint") || (c.contains("int8") && !b.substring(0, 1).equals("I")) || c.contains("int64")) {
                                type = "int8";
                            }
                            // TODO: 2022/5/31 修改
                            if (c.contains("int4") || c.contains("integer") || (c.contains("int8") && b.substring(0, 1).equals("I"))
                                    || c.contains("int16") || c.contains("int32") || c.equals("int")) {
                                type = "int4";
                            }
                            if (c.contains("varchar") || c.contains("text") || c.contains("string")) {
                                type = "varchar";
                            }
                            if (c.contains("timestamp") || c.contains("datetime")) {
                                type = "timestamp";
                            }
                            if (c.contains("date") && !c.contains("time")) {
                                type = "date";
                            }
                            if (c.contains("uuid")) {
                                type = "uuid";
                            }
                            if (c.contains("boolean") || c.contains("bool")) {
                                type = "boolean";
                            }
                            if (c.contains("float4") || c.contains("float32")) {
                                type = "float4";
                            }
                        }
                        if (b.contains("--")) {
                            b = b.trim();
                            desc = b.replace("--", "");
                        }
                    }
                    if (StringUtils.hasText(name) && StringUtils.hasText(type)) {
                        if (name.length() >= 32) {
                            System.out.println(name + "字段长度大于32");
                            return new ArrayList<>();
                        }
                        fieldList.add(new Field(name, type, desc));
                    }
                }
            }
            if (line.endsWith(";")) {
                if (!tableName.equals("sys_log")) {
                    tables.add(new Table(tableName, fieldList, tableDesc));
                    tableName = "";
                    fieldList = new ArrayList<>();
                    tableDesc = "";
                }
            }
        }

        return tables;
    }


    public static List<Table> getTableStrs(List<String> content) {
        List<Table> tables = new ArrayList<>();
        try {
            String line = "";
            String tableName = "";
            List<Field> fieldList = new ArrayList<>();
            String tableDesc = "";
            String tmpDesc = "";
            int n = content.size();
            for (int i = 0; i < n; i++) {
                line = content.get(i);
                if (line.toUpperCase().startsWith("CREATE TABLE IF NOT EXISTS")) {
                    tableName = line.replace("CREATE TABLE IF NOT EXISTS", "").trim();
                    tableDesc = tmpDesc;
                    if (tableName.length() >= 32) {
                        System.out.println(tableName + "表长度大于32");
                        return new ArrayList<>();
                    }
                    fieldList = new ArrayList<>();
                }
                if (line.startsWith("* 表描述：")) {
                    tmpDesc = line.replace("* 表描述：", "").trim();
                }
                if (line.startsWith("--")) {
                    tmpDesc = line.replace("--", "").trim();
                }
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    line = line.trim();
                    String[] a = line.split(" ");
                    int cnt = 0;
                    if (a.length >= 3) {
                        String name = "";
                        String type = "";
                        String desc = "";
                        for (String b : a) {
                            b = b.trim();
                            if (StringUtils.hasText(b)) {
                                String c = b.toLowerCase();
                                if (cnt == 0) {
                                    name = c;
                                    cnt += 1;
                                }
                                if (c.contains("bigint") || (c.contains("int8") && !b.substring(0, 1).equals("I")) || c.contains("int64")) {
                                    type = "int8";
                                }
                                // TODO: 2022/5/31 修改
                                if (c.contains("int4") || c.contains("integer") || (c.contains("int8") && b.substring(0, 1).equals("I"))
                                        || c.contains("int16") || c.contains("int32") || c.equals("int")) {
                                    type = "int4";
                                }
                                if (c.contains("varchar") || c.contains("text") || c.contains("string")) {
                                    type = "varchar";
                                }
                                if (c.contains("timestamp") || c.contains("datetime")) {
                                    type = "timestamp";
                                }
                                if (c.contains("date") && !c.contains("time")) {
                                    type = "date";
                                }
                                if (c.contains("uuid")) {
                                    type = "uuid";
                                }
                                if (c.contains("boolean") || c.contains("bool")) {
                                    type = "boolean";
                                }
                                if (c.contains("float4") || c.contains("float32")) {
                                    type = "float4";
                                }
                            }
                            if (b.contains("--")) {
                                b = b.trim();
                                desc = b.replace("--", "");
                            }
                        }
                        if (StringUtils.hasText(name) && StringUtils.hasText(type)) {
                            if (name.length() >= 32) {
                                System.out.println(name + "字段长度大于32");
                                return new ArrayList<>();
                            }
                            fieldList.add(new Field(name, type, desc));
                        }
                    }
                }
                if (line.endsWith(";")) {
                    if (!tableName.equals("sys_log")) {
                        tables.add(new Table(tableName, fieldList, tableDesc));
                        tableName = "";
                        fieldList = new ArrayList<>();
                        tableDesc = "";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }


    public static String getSimpleModel(List<String> content) {
        List<Table> tables = getTableStrs(content);
        List<String> ans = new ArrayList<>();
        for (Table table : tables) {
            ans.add(getModelStr(table));
        }
        return ArrayStrUtil.slist2Str(ans,"\n");
    }


}
