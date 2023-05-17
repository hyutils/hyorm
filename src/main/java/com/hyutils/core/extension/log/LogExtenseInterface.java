package com.hyutils.core.extension.log;


import com.hyutils.core.BaseQuery;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Map;

public interface LogExtenseInterface {
    Integer updateByIdWithLog(Object primaryKey, Map<String, Object> value, BaseQuery logQuery, BaseLog log);

    Integer updateWithLog(Map<String, Object> conditions, Map<String, Object> values, BaseQuery logQuery, BaseLog log);

    Long saveWithLog(Map<String, Object> values, BaseQuery logQuery, BaseLog baseLog);

    Integer updateWithLogByOperate(List<Triplet<String, String, Object>> conditions, Map<String, Object> values, BaseQuery logQuery, BaseLog log);
}