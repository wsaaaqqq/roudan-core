package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.util.Underline2CamelUtil;
import org.xht.xdb.vo.Row;
import org.xht.xdb.vo.SqlArgBatchVo;
import org.xht.xdb.vo.SqlArgVo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "rawtypes"})
@Slf4j
public class SqlTableFormatHelper {

    public static String insert(String tableName, MapUtil sqlArgs) {
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        @SuppressWarnings("unchecked") Map<String, Object> args = sqlArgs.value();
        Set<String> keySet = args.keySet();
        for (String key : keySet) {
            keys.append("\"").append(key).append("\"").append(" , ");
            values.append(":").append(key).append(" , ");
        }
        if (!keySet.isEmpty()) {
            keys = new StringBuilder(keys.substring(0, keys.length() - 3));
            values = new StringBuilder(values.substring(0, values.length() - 3));
        }
        return String.format("insert into %s ( %s ) values ( %s )", tableName, keys, values);
    }

    public static SqlArgVo insertNoNamedSql(String tableName, Row row) {
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Set<String> keySet = row.keySet();
        Object[] params = new Object[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            keys.append("\"").append(key).append("\"").append(" , ");
            values.append(" ? ").append(" , ");
            params[i] = row.get(key);
            i++;
        }
        if (!keySet.isEmpty()) {
            keys = new StringBuilder(keys.substring(0, keys.length() - 3));
            values = new StringBuilder(values.substring(0, values.length() - 3));
        }
        String sql = String.format("insert into %s ( %s ) values ( %s )", tableName, keys, values);
        return new SqlArgVo(sql, params);
    }

    public static Map<String, List<Object[]>> insertNoNamedSql(String tableName, List<Row> rows) {
        return insertNoNamedSql(tableName, rows, false);
    }

    public static Map<String, List<Object[]>> insertNoNamedSql(String tableName, List<Row> rows,
            boolean rowsFromBeans
    ) {
        if (rows == null || rows.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, List<Object[]>> data = new HashMap<>();
        for (Row row : rows) {
            Set<String> keySet = row.keySet();
            StringBuilder keys = new StringBuilder();
            StringBuilder values = new StringBuilder();
            for (String key : keySet) {
                if (!rowsFromBeans) {
                    key = Underline2CamelUtil.camel2Underline(key, true);
                }
                keys.append("\"").append(key).append("\"").append(" , ");
                values.append(" ? ").append(" , ");
            }
            if (!keySet.isEmpty()) {
                keys = new StringBuilder(keys.substring(0, keys.length() - 3));
                values = new StringBuilder(values.substring(0, values.length() - 3));
            }
            String sql = String.format("insert into %s ( %s ) values ( %s )", tableName, keys, values);
            List<Object[]> paramList = data.get(sql);
            if (paramList == null) {
                paramList = new ArrayList<>();
            }
            Object[] params = new Object[keySet.size()];
            AtomicInteger idx = new AtomicInteger(0);
            row.forEach((k, v) -> params[idx.getAndIncrement()] = v);
            paramList.add(params);
            data.put(sql, paramList);
        }
        return data;
    }

    public static Map<String, List<Object[]>> joinSameSqlArgs(List<SqlArgBatchVo> list) {
        HashMap<String, List<Object[]>> map = new HashMap<>();
        if (list == null || list.isEmpty()) {
            return map;
        }
        for (SqlArgBatchVo vo : list) {
            String sql = vo.getSql();
            List<Object[]> parameters = vo.getParameters();
            List<Object[]> paramList = map.get(sql);
            if (paramList == null) {
                paramList = new ArrayList<>();
            }
            paramList.addAll(parameters);
            map.put(sql, paramList);
        }
        return map;
    }

    public static <T, ID> List<SqlArgBatchVo> updateNoNamedSql(String tableName, List<Row> rows,
            Boolean ignoreNullValues, String idColName, List<ID> idsForUpdate
    ) {
        return updateNoNamedSql(tableName, rows, ignoreNullValues, idColName, idsForUpdate, false);
    }

    public static <T, ID> List<SqlArgBatchVo> updateNoNamedSql(String tableName, List<Row> rows,
            Boolean ignoreNullValues, String idColName, List<ID> idsForUpdate, boolean rowsFromBeans
    ) {
        List<SqlArgBatchVo> list = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Row> datas = new ArrayList<>();
        if (ignoreNullValues) {
            for (Row row : rows) {
                Row row2 = new Row();
                row.forEach((k, v) -> {
                    if (v != null) {
                        row2.put(k, v);
                    }
                });
                if (!row2.isEmpty()) {
                    datas.add(row2);
                }
            }
        } else {
            datas = rows;
        }
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Map<String, List<Row>> setFieldsStrMap = getEqJoinString(datas, rowsFromBeans);
        Row row0 = datas.get(0);
        //默认兼容 id / ID
        idColName = getIdColName(row0, idColName);
        String finalId = idColName;
        if (idsForUpdate == null || idsForUpdate.isEmpty() || idsForUpdate.size() != setFieldsStrMap.size()) {
            setFieldsStrMap.forEach((setFieldsStr, _rows) -> {
                String sql = String.format("update %s set %s where %s = ?", tableName, setFieldsStr, finalId);
                List<Object[]> paramList = new ArrayList<>();
                for (Row row : _rows) {
                    Object[] params = new Object[row.size() + 1];
                    AtomicInteger idx = new AtomicInteger(0);
                    row.forEach((k, v) -> params[idx.getAndIncrement()] = v);
                    params[idx.getAndIncrement()] = row.get(finalId);
                    paramList.add(params);
                }
                list.add(new SqlArgBatchVo(sql, paramList));
            });
        } else {
            AtomicInteger rowIdx = new AtomicInteger();
            setFieldsStrMap.forEach((setFieldsStr, _rows) -> {
                String sql = String.format("update %s set %s where %s = ?", tableName, setFieldsStr, finalId);
                List<Object[]> paramList = new ArrayList<>();
                for (Row row : _rows) {
                    Object[] params = new Object[row.size() + 1];
                    AtomicInteger idx = new AtomicInteger(0);
                    row.forEach((k, v) -> params[idx.getAndIncrement()] = v);
                    params[idx.getAndIncrement()] = idsForUpdate.get(rowIdx.getAndIncrement());
                    paramList.add(params);
                }
                list.add(new SqlArgBatchVo(sql, paramList));
            });
        }
        return list;
    }

    public static String getIdColName(Row row0, String idColName) {
        if (row0.containsKey(idColName))
            return idColName;
        String upperCase = idColName.toUpperCase(Locale.ROOT);
        if (row0.containsKey(upperCase))
            return upperCase;
        String lowerCase = idColName.toLowerCase(Locale.ROOT);
        if (row0.containsKey(lowerCase))
            return lowerCase;
        return idColName;
    }

    /**
     * <pre>
     * key: column1 = ? ,  column2 = ?
     * value: List<Row>
     * </pre>
     */
    private static Map<String, List<Row>> getEqJoinString(List<Row> data, boolean rowsFromBeans) {
        Map<String, List<Row>> sqlArgs = new HashMap<>();
        for (Row row : data) {
            List<String> setFields = new ArrayList<>();
            Set<String> keySet = row.keySet();
            for (String key : keySet) {
                if (!rowsFromBeans) {
                    key = Underline2CamelUtil.camel2Underline(key, true);
                }
                setFields.add(key + " = ?");
            }
            String sql = String.join(" , ", setFields);
            List<Row> argsList = sqlArgs.getOrDefault(sql, new ArrayList<>());
            argsList.add(row);
            sqlArgs.put(sql, argsList);
        }
        return sqlArgs;
    }
}
