package org.xht.xdb.sql.statement;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.ResultQuery;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.CloseUtil;
import org.xht.xdb.util.CommitUtil;
import org.xht.xdb.util.MapUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.util.*;

@SuppressWarnings({"rawtypes", "SqlSourceToSinkFlow"})
@Slf4j
public class ConnTool {

    public static int execute(String sql, MapUtil sqlArgs, boolean... autoCloseConnection) {
        Connection conn = null;
        try {
            SqlTool.debugMapUtil(sql, sqlArgs);
            conn = Xdb.getConnection();
            return sqlArgs(conn, sql, sqlArgs).executeUpdate();
        } catch (Exception e) {
            errorMsgAndThrow(sql, sqlArgs, e);
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
        return 0;
    }

    public static int execute(String sql, Object[] row) {
        return execute(sql, row, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    public static int execute(String sql, Object[] sqlArgs, boolean... autoCloseConnection) {
        Connection conn = null;
        try {
            SqlTool.debugOjectArray(sql, sqlArgs);
            conn = Xdb.getConnection();
            return sqlArgs(conn, sql, sqlArgs).executeUpdate();
        } catch (Exception e) {
            errorMsgAndThrow(sql, sqlArgs, e);
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
        return 0;
    }

    public static int execute(String sql, MapUtil sqlArgs) {
        return execute(sql, sqlArgs, XdbConfig.isAutoClose());
    }

    public static int execute(String sql, Object[] row, boolean autoCommit, boolean autoCloseConnection) {
        Connection conn = Xdb.getConnection();
        try {
            SqlTool.debugOjectArray(sql, row);
            assert conn != null;
            PreparedStatement statement = conn.prepareStatement(sql);
            int l = row.length;
            for (int k = 0; k < l; k++) {
                NamedParameterStatement.setObject(statement, k + 1, row[k]);
            }
            int i = statement.executeUpdate();
            CommitUtil.commit(autoCommit, conn);
            return i;
        } catch (Exception e) {
            errorMsgAndThrow(sql, row, e);
        } finally {
            if (autoCloseConnection) {
                CloseUtil.close(conn);
            }
        }
        return 0;
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     * </pre>
     *
     * @return ResultQuery
     */
    public static ResultQuery executeQuery(String sql, MapUtil sqlArgs, boolean... autoCloseConnection) {
        ResultQuery r = null;
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            SqlTool.debugMapUtil(sql, sqlArgs);
            conn = Xdb.getConnection();
            resultSet = sqlArgs(conn, sql, sqlArgs).executeQuery();
            r = new ResultQuery(sql, resultSet, conn, autoCloseConnection);
        } catch (Exception e) {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
                CloseUtil.close(resultSet);
            }
            errorMsgAndThrow(sql, sqlArgs, e);

        }
        return r;
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     * </pre>
     *
     * @return ResultQuery
     */
    public static ResultQuery executeQuery(String sql, Object[] sqlArgs, boolean... autoCloseConnection) {
        ResultQuery r = null;
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            SqlTool.debugOjectArray(sql, sqlArgs);
            conn = Xdb.getConnection();
            resultSet = sqlArgs(conn, sql, sqlArgs).executeQuery();
            r = new ResultQuery(sql, resultSet, conn, autoCloseConnection);
        } catch (Exception e) {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
                CloseUtil.close(resultSet);
            }
            errorMsgAndThrow(sql, sqlArgs, e);

        }
        return r;
    }


    public static ResultQuery executeQuery(String sql, MapUtil sqlArgs) {
        return executeQuery(sql, sqlArgs, XdbConfig.isAutoClose());
    }

    public static ResultQuery executeQuery(String sql, Object[] sqlArgs) {
        return executeQuery(sql, sqlArgs, XdbConfig.isAutoClose());
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     * </pre>
     *
     * @return ResultSet
     */
    public static ResultSet executeQueryResultSet(String sql, MapUtil sqlArgs) {
        ResultSet result = null;
        Connection conn = null;
        try {
            SqlTool.debugMapUtil(sql, sqlArgs);
            conn = Xdb.getConnection();
            result = sqlArgs(conn, sql, sqlArgs).executeQuery();
        } catch (Exception e) {
            CloseUtil.close(conn);
            errorMsgAndThrow(sql, sqlArgs, e);
        }
        return result;
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     * </pre>
     *
     * @return ResultSet
     */
    public static ResultSet executeQueryResultSet(String sql, Object[] sqlArgs) {
        ResultSet result = null;
        Connection conn = null;
        try {
            SqlTool.debugOjectArray(sql, sqlArgs);
            conn = Xdb.getConnection();
            result = sqlArgs(conn, sql, sqlArgs).executeQuery();
        } catch (Exception e) {
            CloseUtil.close(conn);
            errorMsgAndThrow(sql, sqlArgs, e);
        }
        return result;
    }

    private static NamedParameterStatement sqlArgs(Connection conn, String sql, MapUtil sqlArgs) throws SQLException {
        NamedParameterStatement statement = new NamedParameterStatement(conn, sql);
        if (sqlArgs == null) {
            return statement;
        }
        @SuppressWarnings("unchecked") Set<String> keys = sqlArgs.keySet();
        for (String key : keys) {
            Object value = sqlArgs.get(key);
            if (value == null) {
                String _sql = sql.toLowerCase(Locale.ROOT);
                //先简单处理，后续需排除insert xxx select xxx from where id=:id, id为空的情况
                //insert value值可以为空，update的新值也可以为空（oracle已测试）
                if (sql.contains("insert") || _sql.contains("update")) {
                    statement.setNull(key, Types.NULL);
                } else {
                    //此处处理： sql = "... where id=:id"，当参数为null时执行报错的问题，但
                    throw new SQLException(String.format("参数%s不能为空：sql语句应直接写 where (id is null or id=:id)",
                            key));
                }
            } else {
                setParamByKeyValue(statement, key, value);
            }
        }
        return statement;
    }

    private static NamedParameterStatement sqlArgs(Connection conn, String sql, Object[] sqlArgs) throws SQLException {
        NamedParameterStatement statement = new NamedParameterStatement(conn, sql);
        if (sqlArgs == null) {
            return statement;
        }
        for (int i = 0; i < sqlArgs.length; i++) {
            Object value = sqlArgs[i];
            int key = i + 1;
            if (value == null) {
                String _sql = sql.toLowerCase(Locale.ROOT);
                //先简单处理，后续需排除insert xxx select xxx from where id=:id, id为空的情况
                //insert value值可以为空，update的新值也可以为空（oracle已测试）
                if (sql.contains("insert") || _sql.contains("update")) {
                    statement.setNull(key, Types.NULL);
                } else {
                    //此处处理： sql = "... where id=:id"，当参数为null时执行报错的问题，但
                    throw new SQLException(String.format("参数%s不能为空：sql语句应直接写 where (id is null or id=:id)",
                            key));
                }
            } else {
                setParamByKeyValue(statement, key, value);
            }
        }
        return statement;
    }

    private static void setParamByKeyValue(NamedParameterStatement statement, String key, Object value) throws SQLException {
        Class<?> valueClass = value.getClass();
        if (Long.class.isAssignableFrom(valueClass)) {
            long vLong = (Long) value;
            statement.setLong(key, vLong);
        } else if (BigDecimal.class.isAssignableFrom(valueClass)) {
            statement.setBigDecimal(key, (BigDecimal) value);
        } else if (BigInteger.class.isAssignableFrom(valueClass)) {
            BigInteger vBigInteger = (BigInteger) value;
            BigDecimal vBigDecimal = new BigDecimal(vBigInteger);
            statement.setBigDecimal(key, vBigDecimal);
        } else if (Character.class.isAssignableFrom(valueClass)) {
            String vStringInteger = String.valueOf(value);
            statement.setString(key, vStringInteger);
        } else if (Timestamp.class.isAssignableFrom(valueClass)) {
            Timestamp vDate = (Timestamp) value;
            statement.setTimestamp(key, vDate);
        } else if (Date.class.isAssignableFrom(valueClass)) {
            Date vDate = ((Date) value);
            statement.setDate(key, vDate);
        } else if (Time.class.isAssignableFrom(valueClass)) {
            Time vDate = (Time) value;
            statement.setTime(key, vDate);
        } else if (java.util.Date.class.isAssignableFrom(valueClass)) {
            Date vDate = new Date(((java.util.Date) value).getTime());
            statement.setDate(key, vDate);
        } else {
            NamedParameterStatement.setObject(statement, key, value);
        }
    }

    private static void setParamByKeyValue(NamedParameterStatement statement, int key, Object value) throws SQLException {
        Class<?> valueClass = value.getClass();
        if (Long.class.isAssignableFrom(valueClass)) {
            long vLong = (Long) value;
            statement.setLong(key, vLong);
        } else if (BigDecimal.class.isAssignableFrom(valueClass)) {
            statement.setBigDecimal(key, (BigDecimal) value);
        } else if (BigInteger.class.isAssignableFrom(valueClass)) {
            BigInteger vBigInteger = (BigInteger) value;
            BigDecimal vBigDecimal = new BigDecimal(vBigInteger);
            statement.setBigDecimal(key, vBigDecimal);
        } else if (Character.class.isAssignableFrom(valueClass)) {
            String vStringInteger = String.valueOf(value);
            statement.setString(key, vStringInteger);
        } else if (Timestamp.class.isAssignableFrom(valueClass)) {
            Timestamp vDate = (Timestamp) value;
            statement.setTimestamp(key, vDate);
        } else if (Date.class.isAssignableFrom(valueClass)) {
            Date vDate = ((Date) value);
            statement.setDate(key, vDate);
        } else if (Time.class.isAssignableFrom(valueClass)) {
            Time vDate = (Time) value;
            statement.setTime(key, vDate);
        } else if (java.util.Date.class.isAssignableFrom(valueClass)) {
            Date vDate = new Date(((java.util.Date) value).getTime());
            statement.setDate(key, vDate);
        } else {
            NamedParameterStatement.setObject(statement, key, value);
        }
    }

    public static void executeBatch(String sql, List<Object[]> rows, int batchSize, boolean autoCommit, boolean... autoCloseConnection) {
        if (rows == null || rows.isEmpty()) return;
        Connection conn = Xdb.getConnection();
        List<Object[]> currentBatch = new ArrayList<>(Math.max(batchSize, 1));
        int batchStart = 0;
        try {
            SqlTool.debugListObjectArray(sql, rows);
            assert conn != null;
            @SuppressWarnings("SqlSourceToSinkFlow") PreparedStatement statement = conn.prepareStatement(sql);
            int j = 0;
            int parameterCount = rows.get(0).length;
            for (Object[] row : rows) {
                currentBatch.add(row);
                for (int k = 0; k < parameterCount; k++) {
                    NamedParameterStatement.setObject(statement, k + 1, row[k]);
                }
                statement.addBatch();
                j++;
                if (j == batchSize) {
                    statement.executeBatch();
                    batchStart += currentBatch.size();
                    currentBatch.clear();
                    j = 0;
                }
            }
            if (j > 0) {
                statement.executeBatch();
                currentBatch.clear();
            }
            CommitUtil.commit(autoCommit, conn);
        } catch (Exception e) {
            if (XdbConfig.isShowBatchSqlErrorDetail()) {
                errorMsgAndThrowBatch(sql, currentBatch, batchStart, e);
            } else {
                errorMsgAndThrow(sql, currentBatch.isEmpty() ? null : currentBatch.get(currentBatch.size() - 1), e);
            }
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
    }

    public static void executeBatchRow(String sql, List<Map<String, Object>> rows, int batchSize, boolean autoCommit, boolean... autoCloseConnection) {
        if (rows == null || rows.isEmpty()) return;
        Connection conn = Xdb.getConnection();
        List<Map<String, Object>> currentBatch = new ArrayList<>(Math.max(batchSize, 1));
        int batchStart = 0;
        try {
            SqlTool.debugRows(sql, rows);
            assert conn != null;
            NamedParameterStatement statement = new NamedParameterStatement(conn, sql);
            int j = 0;
            for (Map<String, Object> row : rows) {
                currentBatch.add(row);
                row.forEach((k, v) -> {
                    try {
                        NamedParameterStatement.setObject(statement, k, v);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                statement.addBatch();
                j++;
                if (j == batchSize) {
                    statement.executeBatch();
                    batchStart += currentBatch.size();
                    currentBatch.clear();
                    j = 0;
                }
            }
            if (j > 0) {
                statement.executeBatch();
                currentBatch.clear();
            }
            CommitUtil.commit(autoCommit, conn);
        } catch (Exception e) {
            if (XdbConfig.isShowBatchSqlErrorDetail()) {
                errorMsgAndThrowBatch(sql, currentBatch, batchStart, e);
            } else {
                errorMsgAndThrow(sql, currentBatch.isEmpty() ? null : currentBatch.get(currentBatch.size() - 1), e);
            }
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
    }

    /** 报告 JDBC 批处理结果中明确失败的行，不重放已可能执行成功的写操作。 */
    private static <T> void errorMsgAndThrowBatch(String sql, List<T> batchRows, int batchStart, Exception e) {
        int[] updateCounts = findBatchUpdateCounts(e);
        List<Integer> failedIndexes = new ArrayList<>();
        if (updateCounts != null) {
            for (int i = 0; i < updateCounts.length && i < batchRows.size(); i++) {
                if (updateCounts[i] == Statement.EXECUTE_FAILED) failedIndexes.add(i);
            }
            // 部分驱动在首个失败处停止，只返回失败行之前的执行结果。
            if (failedIndexes.isEmpty() && updateCounts.length < batchRows.size()) {
                failedIndexes.add(updateCounts.length);
            }
        }

        StringBuilder details = new StringBuilder();
        if (failedIndexes.isEmpty()) {
            details.append("无法从 JDBC 批量执行结果中确定单一失败行，当前失败批次数据：");
            for (int i = 0; i < batchRows.size(); i++) {
                details.append("\n第").append(batchStart + i + 1).append("行: ")
                        .append(formatBatchRow(batchRows.get(i)));
            }
        } else {
            details.append("批量执行失败行：");
            for (Integer index : failedIndexes) {
                details.append("\n第").append(batchStart + index + 1).append("行: ")
                        .append(formatBatchRow(batchRows.get(index)));
            }
        }
        log.error("sql: {}", sql);
        log.error("{}", details);
        log.error("error: {}", e.getMessage());
        throw new RuntimeException(details + "\nSQL: " + sql + "\n原因: " + e.getMessage(), e);
    }

    private static int[] findBatchUpdateCounts(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof BatchUpdateException) {
                return ((BatchUpdateException) current).getUpdateCounts();
            }
        }
        return null;
    }

    private static String formatBatchRow(Object row) {
        if (row instanceof Object[]) return Arrays.deepToString((Object[]) row);
        return String.valueOf(row);
    }

    private static void errorMsgAndThrow(String sql, Object[] values, Exception e) {
        log.error("sql: {}", sql);
        if (values != null) {
            log.error("value: {}", values);
        }
        log.error("error: {}", e.getMessage());
        throw new RuntimeException(e);
    }

    private static void errorMsgAndThrow(String sql, Map<String, Object> values, Exception e) {
        log.error("sql: {}", sql);
        if (values != null) {
            log.error("value: {}", values);
        }
        log.error("error: {}", e.getMessage());
        throw new RuntimeException(e);
    }

    public static void executeBatch(String sql, List<Object[]> rows, int batchSize) {
        executeBatch(sql, rows, batchSize, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    private static void errorMsgAndThrow(String sql, MapUtil values, Exception e) {
        log.error("sql: {}", sql);
        if (values != null) {
            log.error("value: {}", values);
        }
        log.error("error: {}", e.getMessage());
        throw new RuntimeException(e);
    }

}
