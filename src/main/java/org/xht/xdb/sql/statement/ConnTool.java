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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
                statement.setObject(k + 1, row[k]);
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
                    throw new SQLException(String.format(
                            "参数%s不能为空：sql语句应直接写 where (id is null or id=:id)",
                            key
                    ));
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
                    throw new SQLException(String.format(
                            "参数%s不能为空：sql语句应直接写 where (id is null or id=:id)",
                            key
                    ));
                }
            } else {
                setParamByKeyValue(statement, key, value);
            }
        }
        return statement;
    }

    private static void setParamByKeyValue(NamedParameterStatement statement, String key, Object value)
            throws SQLException {
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
            statement.setObject(key, value);
        }
    }

    private static void setParamByKeyValue(NamedParameterStatement statement, int key, Object value)
            throws SQLException {
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
            statement.setObject(key, value);
        }
    }

    public static void executeBatch(String sql, List<Object[]> rows, int batchSize, boolean autoCommit,
            boolean... autoCloseConnection
    ) {
        if (rows == null || rows.isEmpty())
            return;
        Connection conn = Xdb.getConnection();
        Object[] rowLast = null;
        try {
            SqlTool.debugListObjectArray(sql, rows);
            assert conn != null;
            @SuppressWarnings("SqlSourceToSinkFlow") PreparedStatement statement = conn.prepareStatement(sql);
            int j = 0;
            int l = rows.get(0).length;
            for (Object[] row : rows) {
                for (int k = 0; k < l; k++) {
                    statement.setObject(k + 1, row[k]);
                    rowLast = row;
                }
                statement.addBatch();
                j++;
                if (j == batchSize) {
                    j = 0;
                    statement.executeBatch();
                }
            }
            if (j > 0) {
                statement.executeBatch();
            }
            CommitUtil.commit(autoCommit, conn);
        } catch (Exception e) {
            errorMsgAndThrow(sql, rowLast, e);
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
    }

    public static void executeBatchRow(String sql, List<Map<String, Object>> rows, int batchSize, boolean autoCommit,
            boolean... autoCloseConnection
    ) {
        if (rows == null || rows.isEmpty())
            return;
        Connection conn = Xdb.getConnection();
        AtomicReference<Map<String, Object>> rowLast = new AtomicReference<>();
        try {
            SqlTool.debugRows(sql, rows);
            assert conn != null;
            NamedParameterStatement statement = new NamedParameterStatement(conn, sql);
            AtomicInteger j = new AtomicInteger(0);
            rows.forEach(row -> {
                row.forEach((k, v) -> {
                    rowLast.set(row);
                    try {
                        statement.setObject(k, v);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                try {
                    statement.addBatch();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (j.get() == batchSize) {
                    try {
                        statement.executeBatch();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                j.incrementAndGet();
            });
            if (j.get() > 0) {
                statement.executeBatch();
            }
            CommitUtil.commit(autoCommit, conn);
        } catch (Exception e) {
            errorMsgAndThrow(sql, rowLast.get(), e);
        } finally {
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
        }
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
