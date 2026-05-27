package org.xht.xdb.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public interface RowTransformer<T> {
    /**
     * 类型转换
     *
     * @param resultSet         结果集
     * @param colCount          每行列序（1 start）
     * @param resultSetMetaData 结果集元数据
     * @return {@code T}
     * @throws SQLException SQLException
     */
    T transform(ResultSet resultSet, int colCount, ResultSetMetaData resultSetMetaData) throws SQLException;

    RowTransformer<Map<String, Object>> CAMEL_KEYS_MAP =
            (resultSet, colCount, resultSetMetaData) -> ResultSetUtil.toMap(
                    resultSet,
                    resultSetMetaData,
                    colCount,
                    true,
                    false
            );
    RowTransformer<Map<String, Object>> ORIGINAL_KEYS_MAP =
            (resultSet, colCount, resultSetMetaData) -> ResultSetUtil.toMapOriginKey(
                    resultSet,
                    resultSetMetaData,
                    colCount
            );
    RowTransformer<Map<String, Object>> UPPER_CASE_KEYS_MAP =
            (resultSet, colCount, resultSetMetaData) -> ResultSetUtil.toMapUpperCase(
                    resultSet,
                    resultSetMetaData,
                    colCount
            );
    RowTransformer<Map<String, Object>> LOWER_CASE_KEYS_MAP =
            (resultSet, colCount, resultSetMetaData) -> ResultSetUtil.toMapLowerCase(
                    resultSet,
                    resultSetMetaData,
                    colCount
            );
}
