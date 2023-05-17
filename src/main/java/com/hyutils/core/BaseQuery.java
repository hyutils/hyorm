package com.hyutils.core;


import com.hyutils.core.syntaxtree.AndWhereSyntaxTree;
import com.hyutils.core.syntaxtree.OrWhereSyntaxTree;
import com.hyutils.core.syntaxtree.WhereSyntaxTree;
import org.javatuples.Triplet;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义基础框架
 * 还需要实现序列化和反序列化
 */
public abstract class BaseQuery<T> extends HasAttributes {

    protected String primaryKey = "id";
    public String versionFieldName = "lock_version";
    protected String table = "";
    protected List<String> with = new ArrayList<>();
    protected List<String> withCount = new ArrayList<>();
    protected Integer limit = -1;
    protected Integer offset = -1;
    protected List<String> traitInitializers = new ArrayList<>();
    protected List<String> globalScopes = new ArrayList<>();
    protected List<String> ignoreOnTouch = new ArrayList<>();
    protected List<String> columns = new ArrayList<>();
    protected WhereSyntaxTree wheres = new WhereSyntaxTree();
    protected Map<String, Object> params = new HashMap<>();
    protected String totalSql = "";
    protected List<String> canBeNullFields = new ArrayList<>();

    protected Map<String, Object> updateSetMaps = new HashMap<>();

    protected List<String> orders = new ArrayList<>();

    public BaseQuery<T> finds(List<String> names) {
        this.columns.addAll(names);
        return this;
    }

    public BaseQuery<T> size(Integer size) {
        this.limit = size;
        return this;
    }

    public BaseQuery<T> page(Integer page) {
        this.offset = page;
        return this;
    }

    public BaseQuery<T> orderBy(String key, String order) {
        this.orders.add(key + " " + order);
        return this;
    }

    public BaseQuery<T> find(String name) {
        this.columns.add(name);
        return this;
    }

    public BaseQuery<T> totalSql(String totalSql) {
        this.totalSql = totalSql;
        return this;
    }

    public BaseQuery<T> canBeNull(String field) {
        this.canBeNullFields.add(field);
        return this;
    }

    public BaseQuery<T> addParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public AndWhereSyntaxTree defaultAndWheres(Map<String, Object> andWheres) {
        return wheres.createAndTree(andWheres);
    }


    public AndWhereSyntaxTree defaultAndWheresWithOperate(List<Triplet<String, String, Object>> andWheres) {
        return wheres.createAndTreeByOperate(andWheres);
    }

    public OrWhereSyntaxTree defaultOrWheresWithOperate(List<Triplet<String, String, Object>> orWheres) {
        return wheres.createOrTreeByOperate(orWheres);
    }


    public OrWhereSyntaxTree defaultOrWheres(Map<String, Object> orWheres) {
        return wheres.createOrTree(orWheres);
    }

    public BaseQuery<T> andWheres(Map<String, Object> params) {
        WhereSyntaxTree whereSyntaxTree = new WhereSyntaxTree();
        this.wheres = whereSyntaxTree.createAndTree(params);
        return this;
    }

    public BaseQuery<T> groupBy(String name, String sort) {
        return this;
    }

    public BaseQuery<T> findById(Object id) {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put(this.primaryKey, id);
        tmp.put("deleted_mark", false);
        wheres = wheres.createAndTree(tmp);
        if (this.columns.size() == 0) {
            this.columns.add("*");
        }
        return this;
    }

    public BaseQuery<T> updateById(Object id) {
        wheres = wheres.createFinalAndTree(this.primaryKey, id);
        this.params.put(this.primaryKey, id);
        this.params.put("deleted_mark", false);
        return this;
    }

    public BaseQuery<T> updateById(Object id, Integer version) {
        // TODO: 2023/4/25 引入乐观锁，更新时版本控制
        wheres = wheres.createAndTree(new HashMap<String, Object>() {
            {
                put(primaryKey, id);
                put(versionFieldName, version);
            }
        });
        this.params.put(this.primaryKey, id);
        this.params.put("deleted_mark", false);
        this.params.put(versionFieldName, version);
        return this;
    }

    protected List defaultGet() {
        // TODO: 2021/8/16 整合数据
        return null;
    }

    protected Boolean defaultInsert() {
        return false;
    }

    protected Boolean defaultUpdate() {
        return false;
    }

    protected Boolean defaultDelete() {
        return false;
    }


    public String getPrimaryKey() {
        return primaryKey;
    }

    public BaseQuery<T> setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public BaseQuery<T> defaultWhere() {
        WhereSyntaxTree whereSyntaxTree = this.defaultAndWheres(new HashMap<String, Object>() {
            {
                put("deleted_mark", false);
            }
        });
        this.where(whereSyntaxTree);
        return this;
    }

//    public BaseQuery<T> params(Map<String, Object> params) {
//        this.params = params;
//        return this;
//    }

    public BaseQuery<T> set(String name, Object value) {
        this.updateSetMaps.put(name, value);
        return this;
    }

    public BaseQuery<T> sets(Map<String, Object> sets) {
        this.updateSetMaps.putAll(sets);
        return this;
    }

    /**
     * where 默认是 andWhere
     *
     * @return
     */
    public abstract BaseQuery<T> where(WhereSyntaxTree whereSyntaxTree);

    public abstract BaseQuery<T> having();

    public abstract BaseQuery<T> orHaving();

    public abstract BaseQuery<T> whereExists();

    public abstract BaseQuery<T> orWhereExists();

    public abstract T simpleGet();

    public abstract Long count();

    public abstract List<Map<String, Object>> listMapGet();

    public abstract Long insert(Map<String, Object> values);

    public abstract Integer batchInsert(List<Map<String, Object>> listValues);

    public abstract Integer update(Map<String, Object> conditions, Map<String, Object> values);

    public abstract Integer update(Map<String, Object> conditions, Map<String, Object> values, Integer version);

    public abstract Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values);

    public abstract Integer update(List<Triplet<String, String, Object>> condition, Map<String, Object> values,Integer version);

    public abstract Integer updateById(Object primaryKey, Map<String, Object> value);

    public abstract Integer updateById(Object primaryKey, Map<String, Object> value, Integer version);

    public abstract Boolean delete(Object primaryKey);

    public abstract Boolean run();

    protected String defaultGenerateUpdateSql() {
        String sql = "";
        if (updateSetMaps.size() == 0) {
            return "";
        }
        sql = "UPDATE " + this.table + " SET ";
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

    protected String defaultGenerateCountSql() {
        // TODO: 2021/8/16 需要构造整个查询语句
        String sql = "";
        for (String column : this.columns) {
            if (sql.equals("")) {
                sql = "SELECT " + column;
            } else {
                sql = sql + "," + column;
            }
        }
        if (!StringUtils.hasText(sql)) {
            return null;
        } else {
            sql = sql + " ";
        }
        sql = sql + "FROM " + table + " ";
        String whereSql = this.wheres.getSql(this.params);
        if (StringUtils.hasText(whereSql)) {
            if (whereSql.startsWith("(") && whereSql.endsWith(")")) {
                whereSql = whereSql.substring(1, whereSql.length() - 1);
            }
            sql = sql + "WHERE " + whereSql + " ";
        }
        sql = sql + ";";
        return sql;
    }


    protected String defaultGenerateSql() {
        // TODO: 2021/8/16 需要构造整个查询语句
        String sql = "";
        for (String column : this.columns) {
            if (sql.equals("")) {
                sql = "SELECT " + column;
            } else {
                sql = sql + "," + column;
            }
        }
        if (!StringUtils.hasText(sql)) {
            return null;
        } else {
            sql = sql + " ";
        }
        sql = sql + "FROM " + table + " ";
        String whereSql = this.wheres.getSql(this.params);
        if (StringUtils.hasText(whereSql)) {
            if (whereSql.startsWith("(") && whereSql.endsWith(")")) {
                whereSql = whereSql.substring(1, whereSql.length() - 1);
            }
            sql = sql + "WHERE " + whereSql + " ";
        }
        // TODO: 2021/8/17 order by
        if (this.orders.size() > 0) {
            String orderSql = "";
            for (String order : this.orders) {
                if (orderSql.equals("")) {
                    orderSql = "order by " + order;
                } else {
                    orderSql = orderSql + "," + order;
                }
            }
            sql = sql + orderSql + " ";
        }
        // TODO: 2021/8/17 offset
        if (this.offset != -1) {
            if (this.limit != -1) {
                sql = sql + " offset " + (this.offset - 1) * this.limit + " ";
            } else {
                sql = sql + " offset " + this.offset + " ";
            }
        }
        if (this.limit != -1) {
            sql = sql + " limit " + this.limit + " ";
        }
        sql = sql + ";";
        return sql;
    }
}
