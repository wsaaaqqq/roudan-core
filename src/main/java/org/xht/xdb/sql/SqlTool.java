package org.xht.xdb.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.DbType;
import org.xht.xdb.function.ResultSetHandler;
import org.xht.xdb.function.ResultSetRowHandler;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.*;
import org.xht.xdb.vo.SqlArgVo;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

/**
 * sql执行工具
 */
@SuppressWarnings("ALL")
@Slf4j
public class SqlTool implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
    private String sql;
    private DbType dbType;
    private MapUtil sqlArgs;
    private String sqlFileName;
    private String sqlFileRelativePath;
    private Class sqlFileClass;
    private Long pageIndex;
    private Long pagePerSize;
    private Long limitFrom;
    private Long limitTo;
    private boolean notFormated = true;

    public Map<String, Object> getSqlArgs() {
        return sqlArgs.value();
    }

    public SqlTool() {
        ExecuteTimeHelp.executeStart();
        this.dbType = DbType.SQLITE;
        XDataSource xDataSource = Xdb.getXDataSource();
        if (xDataSource != null) {
            this.dbType = xDataSource.getDbType();
        }
        this.sqlArgs = MapUtil.init();
    }

    public SqlTool(DbType dbType) {
        ExecuteTimeHelp.executeStart();
        this.dbType = dbType;
        this.sqlArgs = MapUtil.init();
    }

    public SqlTool dbType(DbType dbType) {
        this.dbType = dbType;
        return this;
    }

    public <T> T resultSetHandler(ResultSetHandler<T> resultSetHandler) {
        try (ResultSet resultSet = executeQueryResultSet()) {
            return resultSetHandler.handle(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("resultSetHandler error", e);
        }
    }

    public long resultSetRowHandler(ResultSetRowHandler resultSetRowHandler, int batchSize) {
        return resultSetHandler((resultSet) -> {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount() + 1;
            long count = 0;
            List<Object[]> batchData = new ArrayList<>(batchSize);
            while (resultSet.next()) {
                Object[] row = ResultSetUtil.toArray(resultSet, colCount);
                batchData.add(row);
                if (++count % batchSize == 0) {
                    resultSetRowHandler.handle(batchData);
                    batchData.clear();
                }
            }
            if (count % batchSize != 0) {
                resultSetRowHandler.handle(batchData);
            }
            return count;
        });
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     * </pre>
     *
     * @param autoCloseConnection 自动关闭连接
     * @return ResultQuery
     */
    public ResultQuery executeQuery(boolean... autoCloseConnection) {
        format();
        return ConnTool.executeQuery(this.sql, this.sqlArgs, autoCloseConnection);
    }

    public ResultQuery executeQuery() {
        return executeQuery(XdbConfig.isAutoClose());
    }

    /**
     * <pre>
     *     执行查询语句，返回查询结果
     * </pre>
     *
     * @return ResultQuery
     */
    public ResultSet executeQueryResultSet() {
        format();
        ResultSet resultSet = ConnTool.executeQueryResultSet(this.sql, this.sqlArgs);
        ExecuteTimeHelp.debug();
        return resultSet;
    }

    /**
     * <pre>
     *     获取count
     * </pre>
     *
     * @param autoCloseConnection 自动关闭连接
     * @return ResultQuery
     */
    public long executeCount(boolean... autoCloseConnection) {
        format();
        if (!this.sql.replaceAll(" ", "").toLowerCase(Locale.ROOT).startsWith("selectcount")) {
            this.sql = String.format("select count(1) as count from ( %s )", this.sql);
        }
        ResultQuery resultQuery = ConnTool.executeQuery(this.sql, this.sqlArgs, autoCloseConnection);
        Long bean = resultQuery.firstBean(Long.class);
        ExecuteTimeHelp.debug();
        return bean;
    }

    public long executeCount() {
        return executeCount(XdbConfig.isAutoClose());
    }

    /**
     * <pre>
     * 执行非查询类sql语句(insert、delete、update和),返回结果有两种可能：
     * 1）dml类sql返回变更的记录个数
     * 2）返回0或者没有返回结果的sql执行
     * </pre>
     *
     * @param autoCloseConnection 自动关闭连接
     */
    public int executeUpdate(boolean... autoCloseConnection) {
        format();
        return executeUpdateWithoutFormat(autoCloseConnection);
    }

    public int executeUpdateWithoutFormat(boolean... autoCloseConnection) {
        int execute = ConnTool.execute(this.sql, this.sqlArgs, autoCloseConnection);
        ExecuteTimeHelp.debug();
        return execute;
    }

    public int executeUpdate() {
        return executeUpdate(XdbConfig.isAutoClose());
    }

    /**
     * 批量执行
     *
     * @param values              值
     * @param batchSize           批量大小
     * @param autoCommit          自动提交
     * @param autoCloseConnection 自动关闭连接
     */
    public void executeBatch(List<Object[]> values, int batchSize, boolean autoCommit, boolean... autoCloseConnection) {
        format();
        ConnTool.executeBatch(this.sql, values, batchSize, autoCommit, autoCloseConnection);
        ExecuteTimeHelp.debug();
    }

    public void executeBatch(List<Object[]> values, int batchSize) {
        executeBatch(values, batchSize, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    /**
     * 获取执行前格式化完毕的sql语句
     *
     * @return {@link SqlTool}
     */
    public SqlTool debug() {
        format();
        debug(XdbConfig.getLogLevel());
        return this;
    }

    public SqlTool debug(Level level) {
        XdbConfig.setLogLevel(level);
        debugMapUtil(this.sql, this.sqlArgs);
        return this;
    }

    public static void debugOjectArray(String sql, Object[] row) {
        debugObject(sql, ListUtil.joinToString(row));
    }

    public static void debugListObjectArray(String sql, List<Object[]> rows) {
        if (rows == null) {
            debugObject(sql, String.format("rows is null"));
        }
        if (rows.isEmpty()) {
            debugObject(sql, String.format("rows is empty"));
        }
        int size = rows.size();
        if (size > 1) {
            debugObject(sql, String.format("args cunt: %s , first row: ↓ \n%s", size, ListUtil.joinToString(rows.get(0))));
        } else {
            debugObject(sql, ListUtil.joinToString(rows.get(0)));
        }
    }

    public static void debugRows(String sql, List<Map<String, Object>> rows) {
        if (rows == null) {
            debugObject(sql, String.format("rows is null"));
        }
        if (rows.isEmpty()) {
            debugObject(sql, String.format("rows is empty"));
        }
        int size = rows.size();
        if (size > 1) {
            debugObject(sql, String.format("args cunt: %s , first row: ↓ \n%s", size, rows.get(0)));
        } else {
            debugObject(sql, rows.get(0));
        }
    }

    public static void debugMapUtil(String sql, MapUtil sqlArgs) {
        String _sql = sql;
        if (sqlArgs != null) {
            String part = "";
            if (XdbConfig.isShowSqlFlagOfArgsInComment()) {
                part = " /** %s **/ ";
            }
            Set<String> keySet = sqlArgs.keySet();
            for (String key : keySet) {
                Object value = sqlArgs.get(key);
                if (value instanceof String) {
                    _sql = _sql.replaceAll(
                            ":" + key + "[ >\n (\r\n)=\\|\\)\\(,]{1}",
                            String.format("'%s'" + part, value, key)
                    );
                } else {
                    _sql = _sql.replaceAll(
                            ":" + key + "[ >\n (\r\n)=\\|\\)\\(,]{1}",
                            String.format("%s" + part, value, key)
                    );
                }
            }
        }
        debugObject(_sql, sqlArgs);
    }

    private static void debugObject(String _sql, Object sqlArgs) {
        String msg;
        if (XdbConfig.isShowSql()) {
            // 去除sql中的换行
            _sql = _sql.replaceAll("\n", "");
            String caller = XdbConfig.isShowSqlCaller() ? getCaller() : "";
            if (XdbConfig.isShowSqlArgs()) {
                msg = String.format(
                        "\n---------------------- xdb log [config in XdbConfig] -----------------------%s\n%s\n%s",
                        caller,
                        _sql,
                        sqlArgs
                );
            } else {
                msg = String.format(
                        "\n---------------------- xdb log [config in XdbConfig] -----------------------%s\n%s",
                        caller,
                        _sql
                );
            }
            debug(msg);
        }
    }

    private static void debug(String msg) {
        Level level = XdbConfig.getLogLevel();
        if (XdbConfig.isShowSqlUseSystemOut()) {
            if (msg.startsWith("/n")) {
                msg = msg.substring(1);
            }
            System.out.println(msg);
        } else if (Level.DEBUG.equals(level)) {
            log.debug(msg);
        } else if (level.TRACE.equals(level)) {
            log.trace(msg);
        } else if (level.WARN.equals(level)) {
            log.warn(msg);
        } else if (level.ERROR.equals(level)) {
            log.error(msg);
        } else {
            log.info(msg);
        }
    }

    private static String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Set<String> ignorePackagesForDebug = XdbConfig.getIgnorePackagesForDebug();
        return Arrays.stream(stackTrace).filter(_packages -> {
            String name = _packages.toString();
            return ignorePackagesForDebug.stream().noneMatch(_package -> name.startsWith(_package));
        }).findFirst().map(e -> "\n" + e).orElse("");
    }

    /**
     * 格式sql,args,page
     *
     * @return {@link SqlTool}
     */
    public SqlTool format() {
        if (notFormated) {
            getSqlFromFile();
            setArraySqlArgs();
            limitSql();
            notFormated = false;
        }
        return this;
    }

    public SqlArgVo formatSqlArgVo() {
        format();
        return SQLConverter.formatArgsWithoutNamedParam(this.sql, this.getSqlArgs());
    }

    /**
     * 结果集限制：开始位置(结果集包含此位置)
     *
     * @param limitFrom 开始位置(结果集包含此位置)
     * @return SqlTool
     */
    public SqlTool limitFrom(Object limitFrom) {
        this.limitFrom = limitFrom == null ? null : Long.parseLong(String.valueOf(limitFrom));
        ;
        return this;
    }

    /**
     * 结果集限制：结束位置(结果集包含此位置)
     *
     * @param limitTo 结束位置(结果集包含此位置)
     * @return SqlTool
     */
    public SqlTool limitTo(Object limitTo) {
        this.limitTo = limitTo == null ? null : Long.parseLong(String.valueOf(limitTo));
        return this;
    }

    /**
     * 分页：当前第几页数据 ( 序号从1起始 )
     *
     * @param pageIndex 当前第几页 ( 序号从1起始 )
     * @return SqlTool
     */
    public SqlTool pageIndex(Object pageIndex) {
        if (pageIndex == null) {
            return this;
        }
        try {
            this.pageIndex = Long.parseLong(String.valueOf(pageIndex));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("pageIndex start from 1 error: " + e.getMessage());
        }
        if (this.pageIndex == 0L) {
            throw new IllegalArgumentException("pageIndex start from 1");
        }
        return this;
    }

    /**
     * 分页：每页多少条数据
     *
     * @param pagePerSize 每页多少条数据
     * @return SqlTool
     */
    public SqlTool pagePerSize(Object pagePerSize) {
        this.pagePerSize = pagePerSize == null ? null : Long.parseLong(String.valueOf(pagePerSize));
        return this;
    }

    /**
     * <pre>
     * 1、sql 中参数以":"开始,如“:id”,":name"
     * 2、参数前后必须留有空格
     * </pre>
     *
     * @param sql sql
     * @return SqlTool
     */
    public SqlTool sql(String sql) {
        this.sql = sql;
        return this;
    }

    /**
     * sql执行参数
     *
     * @param sqlArgs sql执行参数
     * @return SqlTool
     */
    @SuppressWarnings("unused")
    public SqlTool sqlArgs(MapUtil sqlArgs) {
        this.sqlArgs = sqlArgs;
        return this;
    }

    /**
     * sql执行参数
     *
     * @param key    关键
     * @param sqlArg sql参数
     * @return SqlTool
     */
    @SuppressWarnings("unused")
    public SqlTool sqlArg(String key, Object sqlArg) {
        this.sqlArgs.add(key, sqlArg);
        return this;
    }

    public SqlTool sqlArgIf(String key, Object sqlArg) {
        this.sqlArgs.addIf(key, sqlArg, () -> sqlArg != null);
        return this;
    }

    public SqlTool sqlArgIf(String key, Object sqlArg, boolean condition) {
        this.sqlArgs.addIf(key, sqlArg, condition);
        return this;
    }

    public SqlTool sqlArgIf(String key, Object sqlArg, Supplier<Boolean> condition) {
        this.sqlArgs.addIf(key, sqlArg, condition);
        return this;
    }

    /**
     * <pre>
     * 功能：动态组装sql
     *    1、sqlFileClass和sqlFileName需在同一目录层级
     *    2、sql文件中的参数使用冒号占位符，如" :id "," :name "
     *    3、冒号占位符前后必须留有空格
     *    4、注释语句以1行为单位，1行内的所有参数占位符如果都在sqlArgs中传入，则自动放开此行语句
     * </pre>
     *
     * @param sqlFileClass sqlFileClass和sqlFileName需在同一目录层级
     * @param sqlFileName  sqlFileName
     * @return SqlTool
     */
    public SqlTool sqlFile(Class sqlFileClass, String sqlFileName) {
        this.sqlFileClass = sqlFileClass;
        this.sqlFileName = sqlFileName;
        return this;
    }

    private SqlTool limitSql() {
        if (limitTo == null) {
            if (pageIndex != null && pagePerSize != null && pagePerSize > 0) {
                limitFrom = pagePerSize * (pageIndex - 1) + 1;
                limitTo = limitFrom + pagePerSize - 1;
                this.dbType = this.dbType == null ? DbType.SQLITE : this.dbType;
                this.sql = DbType.getLimitSql(this.sql, this.limitFrom, this.limitTo, this.dbType);
            }
        } else {
            limitFrom = limitFrom == null ? 1 : limitFrom;
            this.dbType = this.dbType == null ? DbType.SQLITE : this.dbType;
            this.sql = DbType.getLimitSql(this.sql, this.limitFrom, this.limitTo, this.dbType);
        }
        return this;
    }

    private void getSqlFromFile() {
        if (sql == null) {
            if (this.sqlFileRelativePath != null) {
                this.sql = SqlFileUtil.getSql(sqlFileRelativePath, sqlArgs);
            } else if (sqlFileClass != null && sqlFileName != null) {
                this.sql = SqlFileUtil.getSql(sqlFileClass, sqlFileName, sqlArgs);
            }
        } else {
            String[] split = this.sql.split("\n");
            List<String> lines = new ArrayList<>(split.length);
            for (String line : split) {
                lines.add(line);
            }
            this.sql = SqlFileUtil.dynamicFormatSql(lines, sqlArgs);
        }
    }

    /**
     * 由于各jdbc驱动对Connection.createArrayOf的支持不同，这里使用替换的方法实现数组类型参数的处理
     */
    private void setArraySqlArgs() {
        if (sqlArgs == null) {
            return;
        }
        Set<String> keys = sqlArgs.keySet();
        int size = keys.size();
        String[] _keys = new String[size];
        _keys = keys.toArray(_keys);
        for (int i = 0; i < size; i++) {
            String key = _keys[i];
            Object value = sqlArgs.get(key);
            if (value != null) {
                if (Collection.class.isAssignableFrom(value.getClass())) {
                    Collection collection = (Collection) value;
                    Object[] arr = collection.toArray();
                    arrayArgs2SingleArg(key, arr);
                } else if (Object[].class.isAssignableFrom(value.getClass())) {
                    Object[] arr = (Object[]) value;
                    arrayArgs2SingleArg(key, arr);
                }
            }
        }
    }

    private void arrayArgs2SingleArg(String key, Object[] values) {
        String _key = String.format("%s__%s", key, 0);
        String keyJoin = String.format(" :%s__0 ", key);
        this.sqlArgs.del(key).add(_key, values[0]);
        for (int i = 1, len = values.length; i < len; i++) {
            _key = String.format("%s__%s", key, i);
            keyJoin = String.format("%s, :%s ", keyJoin, _key);
            this.sqlArgs.add(_key, values[i]);
        }
        this.sql = this.sql.replaceAll(String.format(":%s ", key), keyJoin + " ");
        this.sql = this.sql.replaceAll(String.format(":%s\\)", key), keyJoin + ")");
        this.sql = this.sql.replaceAll(String.format(":%s\\,", key), keyJoin + ",");
    }

    public SqlTool sqlFile(String sqlFileRelativePath) {
        if (sqlFileRelativePath.endsWith(".sql")) {
            this.sqlFileRelativePath = sqlFileRelativePath;
        } else {
            this.sqlFileRelativePath = sqlFileRelativePath + ".sql";
        }
        return this;
    }

}
