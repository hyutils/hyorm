package com.hyutils.core.extension.dataauth;


import com.hyutils.core.BaseQuery;
import com.hyutils.core.extension.log.BaseLog;
import com.hyutils.core.syntaxtree.AndWhereSyntaxTree;
import com.hyutils.core.syntaxtree.OrWhereSyntaxTree;
import com.hyutils.core.syntaxtree.WhereSyntaxTree;
import com.hyutils.core.utils.Json;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DataAuthExtensionInterface {

    default List<Triplet<String, String, Object>> mergeCondition(Map<String, Object> condition, List<Long> orgIds, String orgFieldName) {
        condition = Json.removeNull(condition);
        List<Triplet<String, String, Object>> ans = new ArrayList<>();
        for (Map.Entry<String, Object> a : condition.entrySet()) {
            ans.add(new Triplet<String, String, Object>(a.getKey(), "=", a.getValue()));
        }
        ans.add(new Triplet<String, String, Object>(orgFieldName, "in", orgIds));
        return ans;
    }

    default List<Triplet<String, String, Object>> map2List1(Map<String, Object> condition){
        condition = Json.removeNull(condition);
        List<Triplet<String, String, Object>> ans = new ArrayList<>();
        for (Map.Entry<String, Object> a : condition.entrySet()) {
            ans.add(new Triplet<String, String, Object>(a.getKey(), "=", a.getValue()));
        }
        return ans;
    }

    default OrWhereSyntaxTree getDataAuthConditionByOrgIdsAndUserId(List<Long> orgIds, Long userId, String userFieldName, String orgFieldName){
        // TODO: 2022/1/5 如果orgIds里面包含-1，则表明该查询条件中包含owner_id = 自己
        List<Triplet<String, String, Object>> orConditions = new ArrayList<>();
        List<Long> trueOrgIds = new ArrayList<>();
        if (orgIds.contains(-1L)) {
            orConditions.add(new Triplet<String, String, Object>(userFieldName, "=", userId));
        }
        for (Long orgId : orgIds) {
            if (orgId != -1L) {
                trueOrgIds.add(orgId);
            }
        }
        if (trueOrgIds.size() > 0) {
            orConditions.add(new Triplet<String, String, Object>(orgFieldName, "in", trueOrgIds));
        }
        if (orConditions.size() == 0) {
            // TODO: 2022/1/5 处理没有的情况
            orConditions.add(new Triplet<>("1", "=", 2));
        }
        return new WhereSyntaxTree().createOrTreeByOperate(orConditions);
    }

    default OrWhereSyntaxTree getDataAuthConditionByOrgIdsAndUserId(List<Long> orgIds, Long userId, String userFieldName, List<String> orgFieldNames){
        // TODO: 2022/1/5 如果orgIds里面包含-1，则表明该查询条件中包含owner_id = 自己
        List<Triplet<String, String, Object>> orConditions = new ArrayList<>();
        List<Long> trueOrgIds = new ArrayList<>();
        if (orgIds.contains(-1L)) {
            orConditions.add(new Triplet<String, String, Object>(userFieldName, "=", userId));
        }
        for (Long orgId : orgIds) {
            if (orgId != -1L) {
                trueOrgIds.add(orgId);
            }
        }
        if (trueOrgIds.size() > 0) {
            for (String orgFieldName : orgFieldNames){
                orConditions.add(new Triplet<String, String, Object>(orgFieldName, "in", trueOrgIds));
            }
        }
        if (orConditions.size() == 0) {
            // TODO: 2022/1/5 处理没有的情况
            orConditions.add(new Triplet<>("1", "=", 2));
        }
        return new WhereSyntaxTree().createOrTreeByOperate(orConditions);
    }


    AndWhereSyntaxTree andWheresWithOrgIds(Map<String, Object> andWheres, List<Long> orgIds);

    Integer updateWithOrgIdsAndUserId(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                      String userFieldName,
                                      String orgFieldName,
                                      Map<String, Object> values);

    Integer updateWithLogAndWithOrgIdsAndUserId(Map<String, Object> conditions, List<Long> orgIds, Long userId,
                                                String userFieldName,
                                                String orgFieldName,
                                                Map<String, Object> values,
                                                BaseQuery logQuery,
                                                BaseLog log);
}
