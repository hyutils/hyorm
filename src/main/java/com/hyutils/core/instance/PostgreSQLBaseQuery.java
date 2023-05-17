package com.hyutils.core.instance;


import com.hyutils.core.BaseQuery;
import com.hyutils.core.extension.cache.LRUCacheExtensionInterface;
import com.hyutils.core.extension.dataauth.DataAuthExtensionInterface;
import com.hyutils.core.extension.like.LikeParamExtension;
import com.hyutils.core.extension.log.BaseLog;
import com.hyutils.core.extension.log.LogExtenseInterface;
import com.hyutils.core.extension.log.TreeLRUExtension;
import com.hyutils.core.extension.selectfield.SelectFields;
import com.hyutils.core.extension.selectorder.SelectOrder;
import com.hyutils.core.extension.selectorder.SelectOrders;
import com.hyutils.core.extension.tree.TreeExtenseInterface;
import com.hyutils.core.syntaxtree.AndWhereSyntaxTree;
import com.hyutils.core.syntaxtree.OrWhereSyntaxTree;
import com.hyutils.core.syntaxtree.WhereSyntaxTree;
import com.hyutils.core.utils.*;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javatuples.Triplet;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * pgsql
 *
 * @param
 */
public class PostgreSQLBaseQuery<T> extends BaseQuery<T>
        implements DataAuthExtensionInterface,
        LogExtenseInterface,
        TreeExtenseInterface,
        LRUCacheExtensionInterface, LikeParamExtension {

    private Log logger = LogFactory.getLog(PostgreSQLBaseQuery.class);

    protected Converter<String, String> camelToUnderscoreConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    protected Class<T> clazz;

    public T model;

    public String fieldOrgName = "created_org_id";
    public String fieldUserName = "created_id";
    public String fieldOrgName2 = "org_id";
    public Boolean useOrgName2 = false;


    public PostgreSQLBaseQuery() {
        Class clazz = getClass();
        while (clazz != Object.class) {
            Type t = clazz.getGenericSuperclass();
            if (t instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) t).getActualTypeArguments();
                if (args[0] instanceof Class) {
                    this.clazz = (Class<T>) args[0];
                    break;
                } else if (args[0] instanceof ParameterizedType) {
//                    this.clazz = (Class<T>) args[0];
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
        } catch (Exception e) {
//            e.printStackTrace();
        }
        if (Objects.nonNull(this.clazz)) {
            this.table = camelToUnderscoreConverter.convert(this.clazz.getSimpleName());
        }
//        logger.debug(this.table);
    }


    @Override
    public T simpleGet() {
        String sql = totalSql;
        if (!StringUtils.hasText(totalSql)) {
            sql = defaultGenerateSql();
        }
//        String sql = defaultGenerateSql();
        logger.debug(sql);
        logger.debug(this.params);
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
//        String sql = defaultGenerateSql();
        String sql = totalSql;
        if (!StringUtils.hasText(totalSql)) {
            sql = defaultGenerateSql();
        }
        logger.debug(sql);
        logger.debug(this.params);
        Long ans = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForObject(sql, this.params, Long.class);
        return ans;
    }

    @Override
    public Boolean run() {
        String sql = totalSql;
        if (!StringUtils.hasText(totalSql)) {
            sql = defaultGenerateSql();
        }
        logger.debug(sql);
        logger.debug(this.params);
        SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForObject(sql, this.params, Long.class);
        return true;
    }

    @Override
    public List<Map<String, Object>> listMapGet() {
        String sql = totalSql;
        if (!StringUtils.hasText(totalSql)) {
            sql = defaultGenerateSql();
        }
        logger.debug(sql);
        logger.debug(this.params);
        return SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForList(sql, this.params);
    }

    @Override
    public Long insert(Map<String, Object> values) {
        if (Objects.isNull(values)) values = new HashMap<>();
        values = removeNull(values);
        if (values.size() == 0) return null;
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
        String sql = "INSERT INTO " + this.table + "(" + names + ") VALUES (" + nameParams + ") RETURNING " + primaryKey + ";";
        logger.debug(sql);
        logger.debug(this.params);
        Long id = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForObject(sql, this.params, Long.class);
        return id;
    }

    @Override
    public Integer batchInsert(List<Map<String, Object>> listValues) {
        if (Objects.isNull(listValues)) listValues = new ArrayList<>();
        if (listValues.size() == 0) return 0;
        int cnt = 0;
        String insertNames = "";
        List<String> insertNameParams = new ArrayList<>();
        int n = listValues.size();
        Set<String> nameSet = new LinkedHashSet<>();
        for (Map<String, Object> values : listValues) {
            values = removeNull(values);
            values = convertParams(values);
//            String names = "";
//            String nameParams = "";
            Set<String> tmpNameSet = new LinkedHashSet<>();
            for (Map.Entry<String, Object> tmp : values.entrySet()) {
                tmpNameSet.add(tmp.getKey());

//                if (names.equals("")) {
//                    names = names + tmp.getKey();
//                    nameParams = nameParams + ":" + tmp.getKey() + cnt;
//                } else {
//                    names = names + "," + tmp.getKey();
//                    nameParams = nameParams + ",:" + tmp.getKey() + cnt;
//                }
//                this.params.put(tmp.getKey() + "" + cnt, tmp.getValue());
            }
//            insertNames = names;
//            nameParams = "(" + nameParams + ")";
//            insertNameParams.add(nameParams);
//            cnt++;
            if (nameSet.isEmpty()) {
                nameSet = tmpNameSet;
            } else {
                nameSet.retainAll(tmpNameSet);
            }
        }

        for (int i = 0; i < n; i++) {
            String nameParams = "";
            String names = "";
            for (String name : nameSet) {
                if (names.equals("")) {
                    names = names + name;
                    nameParams = nameParams + ":" + name + cnt;
                } else {
                    names = names + "," + name;
                    nameParams = nameParams + ",:" + name + cnt;
                }
                this.params.put(name + "" + cnt, listValues.get(i).get(name));
            }
            insertNames = names;
            nameParams = "(" + nameParams + ")";
            insertNameParams.add(nameParams);
            cnt++;
        }

        params = convertParams(params);
        String sql = "INSERT INTO " + this.table + "(" + insertNames + ") VALUES " + ArrayStrUtil.slist2Str(insertNameParams, ",") + ";";
        logger.debug(sql);
        logger.debug(this.params);
        int x = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return x;
    }


    @Override
    public Integer update(Map<String, Object> conditions, Map<String, Object> values) {
        if (Objects.isNull(conditions)) conditions = new HashMap<>();
        if (Objects.isNull(values)) values = new HashMap<>();
        conditions = removeNull(conditions);
        values = removeNull(values);
        if (!values.containsKey("modified_time")) {
            values.put("modified_time", LocalDateTime.now());
        }
        this.updateSetMaps.putAll(values);
        WhereSyntaxTree whereSyntaxTree = defaultAndWheres(conditions);
        this.where(whereSyntaxTree);
        String sql = defaultGenerateUpdateSql();
        if (!params.containsKey("modified_time")) {
            params.put("modified_time", LocalDateTime.now());
        }
        params = convertParams(params);
        logger.debug(sql);
        logger.debug(params);
        Integer influenceNumber = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return influenceNumber;
    }

    /**
     * 加入乐观锁
     *
     * @param conditions
     * @param values
     * @param version
     * @return
     */
    @Override
    public Integer update(Map<String, Object> conditions, Map<String, Object> values, Integer version) {
        if (Objects.isNull(conditions)) conditions = new HashMap<>();
        if (!conditions.containsKey(versionFieldName) || Objects.isNull(conditions.get(versionFieldName))) {
            conditions.put(versionFieldName, version);
            values.put(versionFieldName, version + 1);
        }
        return update(conditions, values);
    }

    @Override
    public Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values) {
        if (Objects.isNull(condition)) condition = new ArrayList<>();
        if (Objects.isNull(values)) values = new HashMap<>();
        values = removeNull(values);
        if (!values.containsKey("modified_time")) {
            values.put("modified_time", LocalDateTime.now());
        }
        this.updateSetMaps.putAll(values);
        WhereSyntaxTree whereSyntaxTree = defaultAndWheresWithOperate(condition);
        this.where(whereSyntaxTree);
        String sql = defaultGenerateUpdateSql();
        if (!params.containsKey("modified_time")) {
            params.put("modified_time", LocalDateTime.now());
        }
        params = convertParams(params);
        logger.debug(sql);
        logger.debug(params);
        Integer influenceNumber = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return influenceNumber;
    }

    @Override
    public Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values, Integer version) {
        if (!values.containsKey(versionFieldName) || Objects.isNull(values.get(versionFieldName))) {
            condition.add(new Triplet<>(versionFieldName, "=", version));
            values.put(versionFieldName, version + 1);
        }
        return update(condition, values);
    }

    /**
     * 类型转换：目前已知的是时间格式
     *
     * @param params
     * @return
     */
    private Map<String, Object> convertParams(Map<String, Object> params) {
        Map<String, Object> newParams = new LinkedHashMap<>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            try {
                newParams.put(param.getKey(), DatetimeUtil.getLocalDatetimeByStr((String) param.getValue()));
                continue;
            } catch (Exception e) {
            }
//            try {
//                newParams.put(param.getKey(), DateUtil.getLocalDatetimeByStr((String) param.getValue()));
//                continue;
//            }catch (Exception e){
//            }

            newParams.put(param.getKey(), param.getValue());
        }
        return newParams;
    }

    @Override
    public Integer updateById(Object primaryKey, Map<String, Object> values) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put(this.primaryKey, primaryKey);
        return this.update(conditions, values);
    }

    @Override
    public Integer updateById(Object primaryKey, Map<String, Object> value, Integer version) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put(this.primaryKey, primaryKey);
        conditions.put(versionFieldName, version);
        if (Objects.isNull(value)) value = new HashMap<>();
        value.put(versionFieldName, version + 1);
        return this.update(conditions, value);
    }

    /**
     * 更新 如果不一样，存log
     *
     * @param conditions
     * @param values
     * @param logQuery
     * @return
     */
    @Override
    public Integer updateWithLog(Map<String, Object> conditions, Map<String, Object> values, BaseQuery logQuery, BaseLog log) {
        // TODO: 2021/8/25 如果信息发生了更改，应该从
        this.update(conditions, values);
        List<T> olds = findListModelBySimpleAnd(conditions, 1, 10000);
        for (T old : olds) {
            String modelJson = Json.toJson(old);
            saveLog(modelJson, values, (PostgreSQLBaseQuery) logQuery, log);
        }
        return 0;
    }

    @Override
    public Long saveWithLog(Map<String, Object> values, BaseQuery logQuery, BaseLog baseLog) {
        Object id = this.insert(values);
        values.put(primaryKey, id);
        baseLog.setBusinessId(id);
        saveLog("", values, (PostgreSQLBaseQuery) logQuery, baseLog);
        return (Long) id;
    }

    @Override
    public Integer updateWithLogByOperate(List<Triplet<String, String, Object>> conditions, Map<String, Object> values, BaseQuery logQuery, BaseLog log) {
        Integer cnt = this.update(conditions, values);
        List<T> olds = findListModelByOperateSimpleAnd(conditions, 1, 10000);
        for (T old : olds) {
            String modelJson = Json.toJson(old);
            saveLog(modelJson, values, (PostgreSQLBaseQuery) logQuery, log);
        }
        return cnt;
    }

    @Override
    public Integer updateByIdWithLog(Object primaryKey1, Map<String, Object> value, BaseQuery query, BaseLog log) {
        log.setBusinessId(primaryKey1);
        System.out.println("+++++++++++++++" + value);
        checkOrSaveLog(primaryKey1, value, (PostgreSQLBaseQuery) query, log);
        return this.updateById(primaryKey1, value);
    }

    private Integer checkOrSaveLog(Object primaryKey1, Map<String, Object> values, PostgreSQLBaseQuery logQuery, BaseLog log) {
        TreeLRUExtension treeLruExtension = SpringContextUtil.getBean(TreeLRUExtension.class);
        if (treeLruExtension.tableModelCache.containsKey(this.table)) {
            String modelJsons = treeLruExtension.tableModelCache.get(this.table).get(primaryKey1);
            int cnt = 0;
            if (StringUtils.hasText(modelJsons)) {
                // TODO: 2021/8/26 如果缓存中存在值
                saveLog(modelJsons, values, logQuery, log);
            } else {
                // TODO: 2021/8/26 如果缓存中不存在，直接去数据库里面findbyid
                T now = this.findModelById(primaryKey1);
                if (Objects.nonNull(now)) {
                    modelJsons = Json.toJson(now);
                    saveLog(modelJsons, values, logQuery, log);
                }
            }
        } else {
            // TODO: 2021/9/7 没有在缓存中
            T model1 = findModelById(primaryKey1);
            if (Objects.nonNull(model1)) {
                String modelJsons = Json.toJson(model1);
                if (!treeLruExtension.tableModelCache.containsKey(this.table)) {
                    treeLruExtension.tableModelCache.put(this.table, new LRUMap<>(10, 100));
                }
                treeLruExtension.tableModelCache.get(this.table).put(primaryKey1, modelJsons);
                if (StringUtils.hasText(modelJsons)) {
                    // TODO: 2021/8/26 如果缓存中存在值
                    saveLog(modelJsons, values, logQuery, log);
                } else {
                    // TODO: 2021/8/26 如果缓存中不存在，直接去数据库里面findbyid
                    T now = this.findModelById(primaryKey1);
                    if (Objects.nonNull(now)) {
                        modelJsons = Json.toJson(now);
                        saveLog(modelJsons, values, logQuery, log);
                    }
                }
            }
        }
        return 1;
    }

    private void saveLog(String oldModelJsons, Map<String, Object> values, PostgreSQLBaseQuery logQuery, BaseLog log) {
        int cnt = 0;
        values = removeNull(values);
        if (StringUtils.hasText(oldModelJsons)) {
            // TODO: 2021/8/26 这里还需要区分delete的情况
            Map<String, Object> oldValues = Json.toMap(oldModelJsons);
            System.out.println(oldValues);
            if (Objects.nonNull(oldValues)) {
                log.setBusinessId(oldValues.get(primaryKey));
                // TODO: 2021/8/25 只匹配values中存在的字段，且值与原来不一样
                for (Map.Entry<String, Object> tmp : values.entrySet()) {
                    System.out.println("-------" + tmp.getKey());
                    if (oldValues.containsKey(tmp.getKey()) && tmp.getValue().equals(oldValues.get(tmp.getKey()))) {
                        // TODO: 2021/8/25 一致 不做处理
                    } else {
                        cnt = 1;
                        break;
                    }
                }
                if (cnt == 1) {
                    // TODO: 2021/8/25 有值不一样 补全为null的部分字段，然后转成json，写入log
                    for (Map.Entry<String, Object> tmp : oldValues.entrySet()) {
                        if (!values.containsKey(tmp.getKey())) {
                            values.put(tmp.getKey(), tmp.getValue());
                        }
                    }
                    System.out.println(values);
                    String newStr = Json.toJson(values);
                    // TODO: 2021/8/25 存储最新的变化
                    log.setSource(newStr);
                    log.setOperateType("MODIFY");
                    System.out.println(values.getOrDefault("deleted_mark", false));
                    if ((Boolean) values.getOrDefault("deleted_mark", false)) {
                        // TODO: 2021/8/26 如果是删除
                        log.setOperateType("DELETE");
                    }
                    log.setTableName(this.table);
                    logQuery.insert(Json.toMap(Json.toJson(log)));
                }
            } else {
                log.setTableName(this.table);
                log.setOperateType("ADD");
                log.setSource(Json.toJson(values));
                logQuery.insert(Json.toMap(Json.toJson(log)));
            }
        } else {
            // TODO: 2021/8/26 如果没有老的，说明是新增
            // TODO: 2021/8/26 新增的时候有一些字段是数据库自动生成的，我可能需要获取到这些字段才行
            // TODO: 2021/8/26 可能需要考虑需要在同一个数据库事务里面，暂时先不考虑，让调用方丢进来
            log.setTableName(this.table);
            log.setOperateType("ADD");
            log.setSource(Json.toJson(values));
            logQuery.insert(Json.toMap(Json.toJson(log)));
        }
    }

    @Override
    public Boolean delete(Object primaryKey) {
        wheres = wheres.createFinalAndTree(this.primaryKey, primaryKey);
        this.params.put(this.primaryKey, primaryKey);
        this.updateSetMaps.putAll(convertParams(new HashMap<String, Object>() {
            {
                put("deleted_mark", null);
                put("deleted_time", LocalDateTime.now());
            }
        }));
        String sql = defaultGenerateUpdateSql();
        Integer influenceNumber = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).update(sql, this.params);
        return influenceNumber == 1;
    }

    @Override
    public BaseQuery<T> where(WhereSyntaxTree whereSyntaxTree) {
        this.wheres = whereSyntaxTree;
        return this;
    }

    /**
     * 获取对象中的所有字段
     *
     * @return
     */
    private String[] allColumns() {
        String[] columns = null;
        Field[] fields = ReflectionBean.allFields(clazz);
        if (ArrayUtils.isNotEmpty(fields)) {
            columns = new String[fields.length];
            for (int index = 0; index < columns.length; ++index) {
                columns[index] = fields[index].getName();
            }
        }
        return columns;
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

    public List<Map<String, Object>> findAll() {
        return this.find("*").defaultWhere().orderBy(primaryKey, "desc").listMapGet();
    }

//    public T findWithId(Object id) {
//        return (T) this.findById(id).simpleGet();
//    }

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
        andCondition.put("deleted_mark", false);
//        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        AndWhereSyntaxTree andWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(andCondition)) {
            andWhereSyntaxTree = this.defaultAndWheresWithOperate(map2Triplet(andCondition));
        } else {
            andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        }
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
        andCondition.put("deleted_mark", true);
        AndWhereSyntaxTree andWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(andCondition)) {
            andWhereSyntaxTree = this.defaultAndWheresWithOperate(map2Triplet(andCondition));
        } else {
            andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        }
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

        OrWhereSyntaxTree orWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(orCondition)) {
            orWhereSyntaxTree = this.defaultOrWheresWithOperate(map2Triplet(orCondition));
        } else {
            orWhereSyntaxTree = this.defaultOrWheres(orCondition);
        }
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", false);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").size(1).simpleGet();
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
        andWhereCondition.put("deleted_mark", false);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").size(1).simpleGet();
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
        filter.add(new Triplet<>("deleted_mark", "=", false));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(filter);
        return this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").size(1).simpleGet();
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
        andCondition.put("deleted_mark", false);
        SelectOrders selectOrders = null;
        try {
            if (andCondition.containsKey("select_orders")) {
                selectOrders = Json.toObject(Json.toJson(andCondition.get("select_orders")), SelectOrders.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SelectFields selectFields = null;
        String findVal = "*";
        try {
            if (andCondition.containsKey("select_fields")) {
                selectFields = Json.toObject(Json.toJson(andCondition.get("select_fields")), SelectFields.class);
                List<String> finds = selectFields.getFields();
                findVal = ArrayStrUtil.slist2Str(finds, ",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        andCondition.remove("select_orders");
        andCondition.remove("select_fields");
        AndWhereSyntaxTree andWhereSyntaxTree = null;
        // TODO: 2022/7/20 加上有like的情况
        if (checkMapContainsLikeInParam(andCondition)) {
            andWhereSyntaxTree = this.defaultAndWheresWithOperate(map2Triplet(andCondition));
        } else {
            andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        }

        List<Map<String, Object>> tmp = new ArrayList<>();
        if (Objects.isNull(selectOrders) || selectOrders.getOrders().size() == 0) {
            tmp = this.find(findVal).where(andWhereSyntaxTree).page(page).size(size).orderBy(primaryKey, "desc").listMapGet();
        } else {
            this.find(findVal).where(andWhereSyntaxTree).page(page).size(size);
            List<SelectOrder> x = selectOrders.getOrders();
            for (SelectOrder y : x) {
                this.orderBy(y.getKey(), y.getOrder());
            }
            tmp = this.listMapGet();
        }
        List<T> ans = new ArrayList<>();
        if (Objects.isNull(clazz)) {
            for (Map<String, Object> x : tmp) {
                T y = (T) x;
                ans.add(y);
            }
        } else {
            for (Map<String, Object> x : tmp) {
                T y = Json.toObject(Json.toJson(x), clazz);
                ans.add(y);
            }
        }

        return ans;
    }

    public List<T> findListModelBySimpleAnd(Map<String, Object> andCondition) {
        if (Objects.isNull(andCondition)) andCondition = new HashMap<>();
        andCondition = removeNull(andCondition);
        andCondition.put("deleted_mark", false);
        AndWhereSyntaxTree andWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(andCondition)) {
            andWhereSyntaxTree = this.defaultAndWheresWithOperate(map2Triplet(andCondition));
        } else {
            andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        }
//        this.defaultAndWheres(andCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).orderBy(primaryKey, "desc").listMapGet();
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
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        List<Map<String, Object>> tmp = this.find("*").where(andWhereSyntaxTree).page(page).size(size).orderBy(primaryKey, "desc").listMapGet();
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
        andWhereCondition.put("deleted_mark", false);
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
     * 解决既具备数据组查询条件，又具备owner_id为查询条件的情况
     * 这里是查询
     *
     * @param conditions
     * @param orgIds
     * @param userId
     * @return
     */
    public List<T> findListModelByOperateSimpleAndWithOrgIds(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                                             String userFieldName,
                                                             String orgFieldName,
                                                             String finds,
                                                             Integer page,
                                                             Integer size) {
        OrWhereSyntaxTree orWhereSyntaxTree = this.getDataAuthConditionByOrgIdsAndUserId(orgIds, userId, userFieldName, orgFieldName);
        conditions = removeNull(conditions);
        List<Triplet<String, String, Object>> andConditions = this.map2Triplet(conditions);
        andConditions.add(new Triplet<String, String, Object>(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), "=", orWhereSyntaxTree));
        andConditions.add(new Triplet<>("deleted_mark", "=", false));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(andConditions);
        BaseQuery<T> query = this;
        query.find(finds);
        query.where(andWhereSyntaxTree).orderBy(primaryKey, "desc");
        if (Objects.nonNull(page) && Objects.nonNull(size)) {
            query.page(page).size(size);
        }
        List<Map<String, Object>> tmp = query.listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    public List<T> findListModelByOperateSimpleAndWithOrgIds(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                                             String userFieldName,
                                                             List<String> orgFieldNames,
                                                             String finds,
                                                             Integer page,
                                                             Integer size) {
        OrWhereSyntaxTree orWhereSyntaxTree = this.getDataAuthConditionByOrgIdsAndUserId(orgIds, userId, userFieldName, orgFieldNames);
        conditions = removeNull(conditions);
        List<Triplet<String, String, Object>> andConditions = this.map2Triplet(conditions);
        andConditions.add(new Triplet<String, String, Object>(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), "=", orWhereSyntaxTree));
        andConditions.add(new Triplet<>("deleted_mark", "=", false));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(andConditions);
        BaseQuery<T> query = this;
        query.find(finds);
        query.where(andWhereSyntaxTree).orderBy(primaryKey, "desc");
        if (Objects.nonNull(page) && Objects.nonNull(size)) {
            query.page(page).size(size);
        }
        List<Map<String, Object>> tmp = query.listMapGet();
        List<T> ans = new ArrayList<>();
        for (Map<String, Object> x : tmp) {
            T y = Json.toObject(Json.toJson(x), clazz);
            ans.add(y);
        }
        return ans;
    }

    public Long countByOperateSimpleAndWithOrgIds(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                                  String userFieldName,
                                                  List<String> orgFieldNames) {
        OrWhereSyntaxTree orWhereSyntaxTree = this.getDataAuthConditionByOrgIdsAndUserId(orgIds, userId, userFieldName, orgFieldNames);
        conditions = removeNull(conditions);
        conditions.remove("select_orders");
        conditions.remove("select_fields");
        List<Triplet<String, String, Object>> andConditions = this.map2Triplet(conditions);
        andConditions.add(new Triplet<String, String, Object>(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), "=", orWhereSyntaxTree));
        andConditions.add(new Triplet<>("deleted_mark", "=", false));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(andConditions);
        BaseQuery<T> query = this;
//        query.find("count(*)");
        query.where(andWhereSyntaxTree);
        return query.count();
    }

    public Long countByOperateSimpleAndWithOrgIds(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                                  String userFieldName,
                                                  String orgFieldName) {
        OrWhereSyntaxTree orWhereSyntaxTree = this.getDataAuthConditionByOrgIdsAndUserId(orgIds, userId, userFieldName, orgFieldName);
        conditions = removeNull(conditions);
        conditions.remove("select_orders");
        conditions.remove("select_fields");
        List<Triplet<String, String, Object>> andConditions = this.map2Triplet(conditions);
        andConditions.add(new Triplet<String, String, Object>(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), "=", orWhereSyntaxTree));
        andConditions.add(new Triplet<>("deleted_mark", "=", false));
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheresWithOperate(andConditions);
        BaseQuery<T> query = this;
//        query.find("count(*)");
        query.where(andWhereSyntaxTree);
        return query.count();
    }


    @Override
    public Integer updateWithOrgIdsAndUserId(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                             String userFieldName,
                                             String orgFieldName,
                                             Map<String, Object> values) {
        OrWhereSyntaxTree orWhereSyntaxTree = this.getDataAuthConditionByOrgIdsAndUserId(orgIds, userId, userFieldName, orgFieldName);
        conditions = removeNull(conditions);
        values = removeNull(values);
        List<Triplet<String, String, Object>> andConditions = this.map2Triplet(conditions);
        andConditions.add(new Triplet<String, String, Object>(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), "=", orWhereSyntaxTree));
        andConditions.add(new Triplet<>("deleted_mark", "=", false));
        return this.update(andConditions, values);
    }

    @Override
    public Integer updateWithLogAndWithOrgIdsAndUserId(Map<String, Object> conditions, List<Long> orgIds, Long userId, String userFieldName,
                                                       String orgFieldName, Map<String, Object> values,
                                                       BaseQuery logQuery, BaseLog log) {
        Integer cnt = this.updateWithOrgIdsAndUserId(conditions, orgIds, userId, userFieldName, orgFieldName, values);
        List<T> olds = findListModelByOperateSimpleAndWithOrgIds(conditions, orgIds, userId, userFieldName, orgFieldName, "*", null, null);
        for (T old : olds) {
            String modelJson = Json.toJson(old);
            saveLog(modelJson, values, (PostgreSQLBaseQuery) logQuery, log);
        }
        return cnt;
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
        andWhereCondition.put("deleted_mark", false);
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
        OrWhereSyntaxTree orWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(orCondition)) {
            orWhereSyntaxTree = this.defaultOrWheresWithOperate(map2Triplet(orCondition));
        } else {
            orWhereSyntaxTree = this.defaultOrWheres(orCondition);
        }
//        OrWhereSyntaxTree orWhereSyntaxTree = this.defaultOrWheres(orCondition);
        Map<String, Object> andWhereCondition = new HashMap<>();
        andWhereCondition.put("deleted_mark", false);
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
        andCondition.remove("select_orders");
        andCondition.remove("select_fields");
        andCondition.put("deleted_mark", false);
        AndWhereSyntaxTree andWhereSyntaxTree = null;
        if (checkMapContainsLikeInParam(andCondition)) {
            andWhereSyntaxTree = this.defaultAndWheresWithOperate(map2Triplet(andCondition));
        } else {
            andWhereSyntaxTree = this.defaultAndWheres(andCondition);
        }
//        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andCondition);
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
        andWhereCondition.put("deleted_mark", false);
        andWhereCondition.put(MD5Utils.compMd5(orWhereSyntaxTree.toString() + LocalDateTime.now().toString()), orWhereSyntaxTree);
        AndWhereSyntaxTree andWhereSyntaxTree = this.defaultAndWheres(andWhereCondition);
        return this.where(andWhereSyntaxTree).count();
    }


    private Map<String, Object> removeNull(Map<String, Object> tmp) {
        Map<String, Object> ans = new LinkedHashMap<>();
        for (Map.Entry<String, Object> x : tmp.entrySet()) {
            if (canBeNullFields.contains(x.getKey())) continue;
            if (Objects.nonNull(x.getValue())) {
                ans.put(x.getKey(), x.getValue());
            }
        }
        return ans;
    }


    @Override
    public List<T> findTreeByPrimaryKey(Object primaryValue, String recursionName) {
        List<String> columnList = new ArrayList<>();
        String[] alls = allColumns();
        if (Objects.nonNull(alls)) {
            for (int i = 0; i < alls.length; i++) {
                columnList.add(camelToSnake(alls[i]));
            }
            String sql = this.findTreeWithPrimaryKeySqlStr(columnList, this.table, this.primaryKey, primaryValue, recursionName);
            logger.debug(sql);
            List<Map<String, Object>> tmp = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForList(sql, new HashMap<>());
            List<T> ans = new ArrayList<>();
            for (Map<String, Object> x : tmp) {
                T y = Json.toObject(Json.toJson(x), clazz);
                ans.add(y);
            }
            return ans;
        }
        return null;
    }

    @Override
    public List<T> findFatherTreeByPrimaryKey(Object primaryValue, String recursionName) {
        List<String> columnList = new ArrayList<>();
        String[] alls = allColumns();
        if (Objects.nonNull(alls)) {
            for (int i = 0; i < alls.length; i++) {
                columnList.add(camelToSnake(alls[i]));
            }
            String sql = this.findFatherTreeWithPrimaryKeySqlStr(columnList, this.table, this.primaryKey, primaryValue, recursionName);
            logger.debug(sql);
            List<Map<String, Object>> tmp = SpringContextUtil.getBean(NamedParameterJdbcTemplate.class).queryForList(sql, new HashMap<>());
            List<T> ans = new ArrayList<>();
            for (Map<String, Object> x : tmp) {
                T y = Json.toObject(Json.toJson(x), clazz);
                ans.add(y);
            }
            return ans;
        }
        return null;
    }

    private String camelToSnake(String camal) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camal);
    }

    // TODO: 2021/12/29 与数据组org_ids列表有关的插件查询

    @Override
    public AndWhereSyntaxTree andWheresWithOrgIds(Map<String, Object> andWheres, List<Long> orgIds) {
        return wheres.createAndTreeByOperate(this.mergeCondition(andWheres, orgIds, this.fieldOrgName));
    }


}