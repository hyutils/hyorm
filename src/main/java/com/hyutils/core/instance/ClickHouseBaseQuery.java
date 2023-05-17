package com.hyutils.core.instance;


import com.hyutils.core.BaseQuery;
import com.hyutils.core.syntaxtree.AndWhereSyntaxTree;
import com.hyutils.core.syntaxtree.OrWhereSyntaxTree;
import com.hyutils.core.syntaxtree.WhereSyntaxTree;
import com.hyutils.core.utils.*;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javatuples.Triplet;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.*;

/**
 * clickhouse基础查询层
 * @param <T>
 */
public class ClickHouseBaseQuery<T> extends BaseQuery<T> {
    private Log logger = LogFactory.getLog(ClickHouseBaseQuery.class);

    private Converter<String, String> camelToUnderscoreConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    protected Class<T> clazz;

    public T model;

    public ClickHouseBaseQuery() {
        Class clazz = getClass();
        while (clazz != Object.class) {
            Type t = clazz.getGenericSuperclass();
            if (t instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                if (args[0] instanceof Class) {
                    this.clazz = (Class<T>) args[0];
                    break;
                }
            }
        }
        try {
            Constructor constructor = this.clazz.getDeclaredConstructor();
            model = (T) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        this.table = camelToUnderscoreConverter.convert(this.clazz.getSimpleName());
        logger.info(this.table);
    }

    private Map<String, Object> removeNull(Map<String, Object> tmp) {
        Map<String, Object> ans = new HashMap<>();
        for (Map.Entry<String, Object> x : tmp.entrySet()) {
            if (Objects.nonNull(x.getValue())) {
                ans.put(x.getKey(), x.getValue());
            }
        }
        return ans;
    }

    private Map<String, Object> convertParams(Map<String, Object> params) {
        Map<String, Object> newParams = new HashMap<>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            try {
                newParams.put(param.getKey(), DatetimeUtil.getLocalDatetimeByStr((String) param.getValue()));
            } catch (Exception e) {
                newParams.put(param.getKey(), param.getValue());
            }
        }
        return newParams;
    }

    private String generateUpdateSql(){
        String sql = "";
        if (updateSetMaps.size() == 0) {
            return "";
        }
        sql = "ALTER TABLE " + this.table + " UPDATE ";
        String setSql = "";
        for (Map.Entry<String, Object> set : this.updateSetMaps.entrySet()) {
            if (setSql.equals("")) {
                setSql = set.getKey() + "=:set" + set.getKey();
                this.params.put("set" + set.getKey(), set.getValue());
            } else {
                setSql = setSql + "," + set.getKey() + "=:set" + set.getKey();
                this.params.put("set" + set.getKey(), set.getValue());
            }
        }
        if (StringUtils.hasText(setSql)) {
            sql = sql + " " + setSql;
        }
        String whereSql = this.wheres.getSql(this.params);
        if (StringUtils.hasText(whereSql)) {
            if (whereSql.startsWith("(") && whereSql.endsWith(")")) {
                whereSql = whereSql.substring(1, whereSql.length() - 1);
            }
            sql = sql + " WHERE " + whereSql + " ";
        }
        sql = sql + ";";
        return sql;
    }

    @Override
    public BaseQuery<T> where(WhereSyntaxTree whereSyntaxTree) {
        this.wheres = whereSyntaxTree;
        return this;
    }

    @Override
    public BaseQuery<T> having() {
        return null;
    }

    @Override
    public BaseQuery<T> orHaving() {
        return null;
    }

    @Override
    public BaseQuery<T> whereExists() {
        return null;
    }

    @Override
    public BaseQuery<T> orWhereExists() {
        return null;
    }

    @Override
    public T simpleGet() {
        String sql = defaultGenerateSql();
        logger.info(sql);
        logger.info(this.params);
        try {
            T xx = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForObject(sql, this.params, new BeanPropertyRowMapper<>(this.clazz));
            return xx;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public Long count() {
        String findStr = "count(" + primaryKey + ")";
        this.find(findStr);
        String sql = defaultGenerateSql();
        logger.info(sql);
        logger.info(this.params);
        Long ans = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForObject(sql, this.params, Long.class);
        return ans;
    }

    @Override
    public List<Map<String, Object>> listMapGet() {
        String sql = defaultGenerateSql();
        logger.info(sql);
        logger.info(this.params);
        return SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForList(sql, this.params);
    }

    /**
     * clickhouse的insert语句不支持返回
     * @param values
     * @return
     */
    @Override
    public Long insert(Map<String, Object> values) {
        if (Objects.isNull(values)) values = new HashMap<>();
        values = removeNull(values);
        String names = "";
        String nameParams = "";
        for (Map.Entry<String, Object> tmp : values.entrySet()) {
            if (names.equals("")) {
                names = names + tmp.getKey();
                nameParams = nameParams + ":" + tmp.getKey();
            } else {
                names = names + "," + tmp.getKey();
                nameParams = nameParams + ",:" + tmp.getKey();
            }
        }
        this.params.putAll(values);
        params = convertParams(params);
        String sql = "INSERT INTO " + this.table + "(" + names + ") VALUES (" + nameParams + ")";
        logger.info(sql);
        logger.info(this.params);
        SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).execute(sql, this.params, PreparedStatement::execute);
        return 1L;
    }

    @Override
    public Integer batchInsert(List<Map<String, Object>> listValues) {
        if (Objects.isNull(listValues)) listValues = new ArrayList<>();
        int cnt = 0;
        String insertNames = "";
        List<String> insertNameParams = new ArrayList<>();
        int n = listValues.size();
        for (Map<String, Object> values : listValues) {
            values = removeNull(values);
            values = convertParams(values);
            String names = "";
            String nameParams = "";
            for (Map.Entry<String, Object> tmp : values.entrySet()) {
                if (names.equals("")) {
                    names = names + tmp.getKey();
                    nameParams = nameParams + ":" + tmp.getKey() + cnt;
                } else {
                    names = names + "," + tmp.getKey();
                    nameParams = nameParams + ",:" + tmp.getKey() + cnt;
                }
                this.params.put(tmp.getKey() + "" + cnt, tmp.getValue());
            }
            insertNames = names;
            nameParams = "(" + nameParams + ")";
            insertNameParams.add(nameParams);
            cnt++;
        }
        String sql = "INSERT INTO " + this.table + "(" + insertNames + ") VALUES " + ArrayStrUtil.slist2Str(insertNameParams, ",") + ";";
        logger.info(sql);
        logger.info(this.params);
        int x = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return x;
    }

    @Override
    public Integer update(Map<String, Object> conditions, Map<String, Object> values) {
        if (Objects.isNull(conditions)) conditions = new HashMap<>();
        if (Objects.isNull(values)) values = new HashMap<>();
        conditions = removeNull(conditions);
        values = removeNull(values);
        this.updateSetMaps.putAll(values);
        WhereSyntaxTree whereSyntaxTree = defaultAndWheres(conditions);
        this.where(whereSyntaxTree);
        String sql = generateUpdateSql();
        params = convertParams(params);
        logger.info(sql);
        logger.info(params);
        Integer influenceNumber = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return influenceNumber;
    }

    @Override
    public Integer update(Map<String, Object> conditions, Map<String, Object> values, Integer version) {
        return null;
    }

    @Override
    public Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values) {
        if (Objects.isNull(condition)) condition = new ArrayList<>();
        if (Objects.isNull(values)) values = new HashMap<>();
        values = removeNull(values);
        this.updateSetMaps.putAll(values);
        WhereSyntaxTree whereSyntaxTree = defaultAndWheresWithOperate(condition);
        this.where(whereSyntaxTree);
        String sql = generateUpdateSql();
        params = convertParams(params);
        logger.info(sql);
        logger.info(params);
        Integer influenceNumber = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return influenceNumber;
    }

    @Override
    public Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values, Integer version) {
        return null;
    }

    @Override
    public Integer updateById(Object primaryKey, Map<String, Object> values) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put(this.primaryKey, primaryKey);
        return this.update(conditions, values);
    }

    @Override
    public Integer updateById(Object primaryKey, Map<String, Object> value, Integer version) {
        return null;
    }

    @Override
    public Boolean delete(Object primaryKey) {
        return null;
    }

    @Override
    public Boolean run() {
        return null;
    }

    public List<Map<String, Object>> findAll() {
        return this.find("*").defaultWhere().listMapGet();
    }


    /**
     * select * from a where id = 2 ......
     *
     * @param id
     * @return
     */
    public T findModelById(Object id) {
        return this.find("*").findById(id).simpleGet();
    }

    /**
     * select aa,aaa,aaaa from a where id = 2 ....
     *
     * @param id
     * @param fields
     * @return
     */
    protected T findModelById(Object id, List<String> fields) {
        return this.finds(fields).findById(id).simpleGet();
    }

    /**
     * select * from a where (x=1 and y=2) and delete_mark = false ......
     *
     * @param andCondition
     * @return
     */
    public T findModelBySimpleAnd(Map<String, Object> andCondition) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", 0);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        return this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").size(1).simpleGet();
    }

    /**
     * select * from a where (x=1 and y=2) and delete_mark = true ......
     *
     * @param andCondition
     * @return
     */
    public T findModelBySimpleAndDeletedMarkTrue(Map<String, Object> andCondition) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", 1);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        return this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").size(1).simpleGet();
    }

    /**
     * select * from a where (x = 1 or y = 2) and delete_mark = false .....
     *
     * @param orCondition
     * @return
     */
    public T findModelBySimpleOr(Map<String, Object> orCondition) {
        if (Objects.isNull(orCondition)) orCondition = new HashMap<>();
        orCondition = removeNull(orCondition);
        OrWhereSyntaxTree orWhereSyntaxTree = this.defaultOrWheres(orCondition);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.find("*").where(andWhereSyntaxTree).size(1).simpleGet();
    }

    /**
     * select * from a where (x like '%,1,%' or y>2 or z < 3) and delete_mark = false limit 1;
     *
     * @param orCondition
     * @return
     */
    public T findModelByOperateSimpleOr(List<Triplet<String, String, Object>> orCondition) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : orCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        OrWhereSyntaxTree orWhereSyntaxTree = this.defaultOrWheresWithOperate(filter);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.find("*").where(andWhereSyntaxTree).size(1).simpleGet();
    }

    /**
     * select * from a where x like '%,1,%' and y > 2 and delete_mark = false limit 1;
     *
     * @param andCondition
     * @return
     */
    public T findModelByOperateSimpleAnd(List<Triplet<String, String, Object>> andCondition) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : andCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        filter.add(new Triplet<>("deleted_mark", "=", 0));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(filter);
        return this.find("*").where(andWhereSyntaxTree).size(1).simpleGet();
    }

    /**
     * select * from a where x like '%,1,%' and y > 2 and delete_mark = false offset page limit size;
     * 支持in操作，Map里面的值传入list即可
     *
     * @param andCondition
     * @param page
     * @param size
     * @return
     */
    public List<T> findListModelBySimpleAnd(Map<String, Object> andCondition, Integer page, Integer size) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", 0);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).page(page).size(size).listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    public List<T> findListModelBySimpleAnd(Map<String, Object> andCondition) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", 0);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }


    public List<T> findListModelByOperateSimpleAnd(List<Triplet<String, String, Object>> andCondition, Integer page, Integer size) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : andCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        AndWhereSyntaxTree orWhereSyntaxTree = this.defaultAndWheresWithOperate(filter);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", false);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        List<Map<String, Object>> tmp = this.find("*").where(orWhereSyntaxTree).page(page).size(size).orderBy(primaryKey, "desc").listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    public List<T> findListModelByOperateSimpleAnd(List<Triplet<String, String, Object>> andCondition) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : andCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        AndWhereSyntaxTree orWhereSyntaxTree = this.defaultAndWheresWithOperate(filter);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        List<Map<String, Object>> tmp = this.find("*").where(orWhereSyntaxTree).orderBy(primaryKey, "desc").listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    /**
     * select * from a where (x like '%,1,%' or y>2 or z < 3) and delete_mark = false offset (page-1)*size limit size;
     *
     * @param orCondition
     * @return
     */
    public List<T> findListModelByOperateSimpleOr(List<Triplet<String, String, Object>> orCondition, Integer page, Integer size) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : orCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        OrWhereSyntaxTree orWhereSyntaxTree = this.defaultOrWheresWithOperate(filter);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).page(page).size(size).listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    /**
     * select * from a where (x = 1 or y = 2) and delete_mark = false offset (page-1)*size limit size;
     *
     * @param orCondition
     * @return
     */
    public List<T> findListModelBySimpleOr(Map<String, Object> orCondition, Integer page, Integer size) {
        if (Objects.isNull(orCondition)) orCondition = new HashMap<>();
        orCondition = removeNull(orCondition);
        OrWhereSyntaxTree orWhereSyntaxTree = this.defaultOrWheres(orCondition);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).page(page).size(size).listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }


    public Long countModelBySimpleAnd(Map<String, Object> andCondition) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", 0);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        return this.where(andWhereSyntaxTree).count();
    }

    public Long countModelByOperateSimpleAnd(List<Triplet<String, String, Object>> andCondition) {
        List<Triplet<String, String, Object>> filter = new ArrayList<>();
        for (Triplet<String, String, Object> tmp : andCondition) {
            if (Objects.isNull(tmp.getValue2())) {
                continue;
            }
            filter.add(tmp);
        }
        AndWhereSyntaxTree orWhereSyntaxTree = this.defaultAndWheresWithOperate(filter);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", 0);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.where(andWhereSyntaxTree).count();
    }



}
