package com.hyutils.core.extension.tree;

import com.hyutils.core.utils.ArrayStrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * todo 按当前字段上递归 和 按当前字段下递归
 */
public interface TreeExtenseInterface {

    default String findTreeWithPrimaryKeySqlStr(List<String> allFields, String tableName, String primaryKeyName, Object primaryKeyValue, String recursionKeyName) {
        // 2021/9/1 参数：查询的字段，第一个条件和递归条件
        StringBuilder sql = new StringBuilder();

        List<String> aAllFields = new ArrayList<>();
        List<String> kAllFields = new ArrayList<>();
        for (String allField : allFields) {
            aAllFields.add("a." + allField);
            kAllFields.add("k." + allField);
        }
        sql.append("WITH RECURSIVE tree(").append(ArrayStrUtil.slist2Str(allFields, ",")).append(") AS\n")
                .append("(\n")
                .append("SELECT ").append(ArrayStrUtil.slist2Str(aAllFields, ",")).append(" FROM ").append(tableName)
                .append(" a WHERE ").append(primaryKeyName).append(" = ").append(primaryKeyValue).append(" and deleted_mark=false \n")
                .append("UNION ALL\n")
                .append("SELECT ").append(ArrayStrUtil.slist2Str(kAllFields, ",")).append(" FROM ").append(tableName)
                .append(" k, tree c WHERE c.").append(primaryKeyName).append(" = k.").append(recursionKeyName).append(" and k.deleted_mark=false\n")
                .append(")\n")
                .append("SELECT * FROM tree");
        /**
         * WITH RECURSIVE tree(id,org_name,org_type,father_org_id,father_org_name,social_code) AS
         * (
         *   SELECT a.id,a.org_name,a.org_type,a.father_org_id,a.father_org_name,a.social_code FROM sys_organization a WHERE id = ?
         *   UNION ALL
         *   SELECT k.id,k.org_name,k.org_type,k.father_org_id,k.father_org_name,k.social_code FROM sys_organization k, tree c WHERE c.id = k.father_org_id
         * )
         * SELECT * FROM tree;
         */
        return sql.toString();
    }

    /**
     * 上递归
     * @param allFields
     * @param tableName
     * @param primaryKeyName
     * @param primaryKeyValue
     * @param recursionKeyName
     * @return
     */
    default String findFatherTreeWithPrimaryKeySqlStr(List<String> allFields, String tableName, String primaryKeyName, Object primaryKeyValue, String recursionKeyName) {
        // 2021/9/1 参数：查询的字段，第一个条件和递归条件
        StringBuilder sql = new StringBuilder();

        List<String> aAllFields = new ArrayList<>();
        List<String> kAllFields = new ArrayList<>();
        for (String allField : allFields) {
            aAllFields.add("a." + allField);
            kAllFields.add("k." + allField);
        }
        sql.append("WITH RECURSIVE tree(").append(ArrayStrUtil.slist2Str(allFields, ",")).append(") AS\n")
                .append("(\n")
                .append("SELECT ").append(ArrayStrUtil.slist2Str(aAllFields, ",")).append(" FROM ").append(tableName)
                .append(" a WHERE ").append(primaryKeyName).append(" = ").append(primaryKeyValue).append(" and deleted_mark=false \n")
                .append("UNION ALL\n")
                .append("SELECT ").append(ArrayStrUtil.slist2Str(kAllFields, ",")).append(" FROM ").append(tableName)
                .append(" k, tree c WHERE k.").append(primaryKeyName).append(" = c.").append(recursionKeyName).append(" and k.deleted_mark=false\n")
                .append(")\n")
                .append("SELECT * FROM tree order by id asc;");
        /**
         * WITH RECURSIVE tree(id,org_name,org_type,father_org_id,father_org_name,social_code) AS
         * (
         *   SELECT a.id,a.org_name,a.org_type,a.father_org_id,a.father_org_name,a.social_code FROM sys_organization a WHERE id = ?
         *   UNION ALL
         *   SELECT k.id,k.org_name,k.org_type,k.father_org_id,k.father_org_name,k.social_code FROM sys_organization k, tree c WHERE k.id = c.father_org_id
         * )
         * SELECT * FROM tree;
         */
        return sql.toString();
    }

    List findTreeByPrimaryKey(Object primaryValue, String recursionName);

    List findFatherTreeByPrimaryKey(Object primaryValue, String recursionName);

}
