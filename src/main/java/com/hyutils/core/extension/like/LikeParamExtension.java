package com.hyutils.core.extension.like;


import com.hyutils.core.utils.DatetimeUtil;
import org.javatuples.Triplet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface LikeParamExtension {
    // TODO: 2022/8/22 处理各种特殊需求：like 不等于 等等 需要做的事情
    String PARAM_IN_LIKE_DELIMITER = "++--++";
    String PARAM_LEFT_LIKE = "%++--++";
    String PARAM_RIGHT_LIKE = "++--++%";

    default Boolean checkMapContainsLikeInParam(Map<String, Object> condition) {
        for (Map.Entry<String, Object> a : condition.entrySet()) {
            if (a.getValue() instanceof String) {
                if (((String) a.getValue()).contains(PARAM_LEFT_LIKE)
                        // TODO: 2022/8/22 处理like
                        || ((String) a.getValue()).contains(PARAM_RIGHT_LIKE)) {
                    return true;
                }
                if (((String) a.getValue()).startsWith("-1*")) {
                    return true;
                }
                if (a.getKey().endsWith("_bigger_than") || a.getKey().endsWith("_lower_than")) {
                    return true;
                }
            }
            if (a.getValue() instanceof LocalDateTime){
                if (a.getKey().endsWith("_bigger_than") || a.getKey().endsWith("_lower_than")) {
                    return true;
                }
            }
            // TODO: 2022/8/22 正常情况下，id不可能为负数或者等于1 ，所以可以利用错误来做一些事情
            if (a.getValue() instanceof Long) {
                if (a.getKey().contains("id") && ((Long) a.getValue()) < -1) {
                    return true;
                }
            }
            if (a.getValue() instanceof Integer) {
                if (a.getKey().contains("id") && ((Integer) a.getValue()) == 1) {
                    return true;
                }
                if (a.getKey().startsWith("has_") && ((Integer) a.getValue()) < 0) {
                    return true;
                }
            }
            // TODO: 2022/9/14 处理ids的情况
            if (a.getValue() instanceof List) {
                if (a.getKey().endsWith("_list")) {
                    if (((List) a.getValue()).size() > 0) {
                        return true;
                    }
                }
            }

//            if (a.getValue() instanceof LocalDateTime) {

//            }
        }
        return false;
    }

    static String handleLikeStr(String likeStr) {
        if (likeStr.contains(PARAM_LEFT_LIKE)
                || likeStr.contains(PARAM_RIGHT_LIKE)) {
            return likeStr.replace(PARAM_IN_LIKE_DELIMITER, "");
        }
        return likeStr;
    }

    default List<Triplet<String, String, Object>> map2Triplet(Map<String, Object> condition) {
        List<Triplet<String, String, Object>> ans = new ArrayList<>();
        for (Map.Entry<String, Object> a : condition.entrySet()) {
            if (a.getValue() instanceof String) {
                if (((String) a.getValue()).contains(PARAM_LEFT_LIKE)
                        || ((String) a.getValue()).contains(PARAM_RIGHT_LIKE)) {
                    ans.add(new Triplet<>(a.getKey(), "like", ((String) a.getValue()).replace(PARAM_IN_LIKE_DELIMITER, "")));
                    continue;
                }
                if (((String) a.getValue()).startsWith("-1*")) {
                    ans.add(new Triplet<>(a.getKey(), "!=", ((String) a.getValue()).substring(3)));
                    continue;
                }
                if (a.getKey().endsWith("_bigger_than") || a.getKey().endsWith("_lower_than")) {
                    if (a.getKey().endsWith("_bigger_than")) {
                        ans.add(new Triplet<>(a.getKey().replace("_bigger_than", ""), ">", DatetimeUtil.getLocalDatetimeByStr(a.getValue().toString())));
                    } else if (a.getKey().endsWith("_lower_than")) {
                        ans.add(new Triplet<>(a.getKey().replace("_lower_than", ""), "<", DatetimeUtil.getLocalDatetimeByStr(a.getValue().toString())));
                    }
                    continue;
                }
            }
            if (a.getValue() instanceof LocalDateTime){
                if (a.getKey().endsWith("_bigger_than") || a.getKey().endsWith("_lower_than")) {
                    if (a.getKey().endsWith("_bigger_than")) {
                        ans.add(new Triplet<>(a.getKey().replace("_bigger_than", ""), ">", a.getValue()));
                    } else if (a.getKey().endsWith("_lower_than")) {
                        ans.add(new Triplet<>(a.getKey().replace("_lower_than", ""), "<", a.getValue()));
                    }
                    continue;
                }
            }
            if (a.getValue() instanceof Long) {
                if (a.getKey().contains("id") && ((Long) a.getValue()) < -1) {
                    ans.add(new Triplet<>(a.getKey(), "!=", -1 * ((Long) a.getValue())));
                    continue;
                }
            }
            if (a.getValue() instanceof Integer) {
                if (a.getKey().contains("id") && (((Integer) a.getValue()) == 1 || ((Integer) a.getValue()) < -1)) {
                    ans.add(new Triplet<>(a.getKey(), "!=", -1 * ((Integer) a.getValue())));
                    continue;
                }
                if (a.getKey().startsWith("has_") && ((Integer) a.getValue()) < 0) {
                    ans.add(new Triplet<>(a.getKey(), "!=", -1 * ((Integer) a.getValue())));
                    continue;
                }
            }
            if (a.getValue() instanceof List) {
                if (((List) a.getValue()).size() > 0) {
                    if (a.getKey().endsWith("_not_list")) {
                        ans.add(new Triplet<>(a.getKey().replace("_not_list", ""), "not in", a.getValue()));
                        continue;
                    }
                    if (a.getKey().endsWith("_left_like_list") || a.getKey().endsWith("_right_like_list")) {
                        List<Triplet<String, String, Object>> sumLike = new ArrayList<>();
                        ((List) a.getValue()).forEach(b -> {
                            sumLike.add(new Triplet<String, String, Object>(a.getKey().replace("_left_like_list", "").replace("_right_like_list", ""), "like", handleLikeStr((String) b)));
                        });
                        ans.add(new Triplet<>(a.getKey().replace("_left_like_list", "").replace("_right_like_list", ""), "like", sumLike));
                        continue;
                    }
                    if (a.getKey().endsWith("_list")) {
                        ans.add(new Triplet<>(a.getKey().replace("_list", ""), "in", a.getValue()));
                        continue;
                    }
                }
            }
            ans.add(new Triplet<>(a.getKey(), "=", a.getValue()));
        }
        return ans;
    }
}
