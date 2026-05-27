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
import org.xht.xdb.util.ResultSetUtil;
import org.xht.xdb.vo.SqlArgVo;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * sql执行工具
 */
@SuppressWarnings("ALL")
@Slf4j
public class SqlToolOfNoNamedArgs implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
    private String sql;
    private DbType dbType;
    private List<Object> sqlArgs = new ArrayList<>();
    private String sqlFileName;
    private String sqlFileRelativePath;
    private Class sqlFileClass;
    private Long pageIndex;
    private Long pagePerSize;
    private Long limitFrom;
    private Long limitTo;
    private boolean notFormated = true;

    public List<Object> getSqlArgs() {
        return sqlArgs;
    }

    public SqlToolOfNoNamedArgs() {
        this.dbType = DbType.SQLITE;
        XDataSource xDataSource = Xdb.getXDataSource();
        if (xDataSource != null) {
            this.dbType = xDataSource.getDbType();
        }
    }

    public SqlToolOfNoNamedArgs(DbType dbType) {
        this.dbType = dbType;
    }

    public SqlToolOfNoNamedArgs dbType(DbType dbType) {
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
        return ConnTool.executeQuery(this.sql, this.sqlArgs.toArray(), autoCloseConnection);
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
        return ConnTool.executeQueryResultSet(this.sql, this.sqlArgs.toArray());
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
        ResultQuery resultQuery = ConnTool.executeQuery(this.sql, this.sqlArgs.toArray(), autoCloseConnection);
        return resultQuery.firstBean(Long.class);
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
        return ConnTool.execute(this.sql, this.sqlArgs.toArray(), autoCloseConnection);
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
    }

    public void executeBatch(List<Object[]> values, int batchSize) {
        executeBatch(values, batchSize, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    /**
     * 获取执行前格式化完毕的sql语句
     *
     * @return {@link SqlToolOfNoNamedArgs}
     */
    public SqlToolOfNoNamedArgs debug() {
        format();
        debug(Level.INFO);
        return this;
    }

    public SqlToolOfNoNamedArgs debug(Level level) {
        XdbConfig.setLogLevel(level);
        SqlTool.debugOjectArray(this.sql, this.sqlArgs.toArray());
        return this;
    }

    /**
     * 格式sql,args,page
     *
     * @return {@link SqlToolOfNoNamedArgs}
     */
    public SqlToolOfNoNamedArgs format() {
        if (notFormated) {
            limitSql();
            notFormated = false;
        }
        return this;
    }

    public SqlArgVo formatSqlArgVo() {
        format();
        return new SqlArgVo(this.sql, this.sqlArgs.toArray());
    }

    /**
     * 结果集限制：开始位置(结果集包含此位置)
     *
     * @param limitFrom 开始位置(结果集包含此位置)
     * @return SqlTool
     */
    public SqlToolOfNoNamedArgs limitFrom(Object limitFrom) {
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
    public SqlToolOfNoNamedArgs limitTo(Object limitTo) {
        this.limitTo = limitTo == null ? null : Long.parseLong(String.valueOf(limitTo));
        return this;
    }

    /**
     * 分页：当前第几页数据 ( 序号从1起始 )
     *
     * @param pageIndex 当前第几页 ( 序号从1起始 )
     * @return SqlTool
     */
    public SqlToolOfNoNamedArgs pageIndex(Object pageIndex) {
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
    public SqlToolOfNoNamedArgs pagePerSize(Object pagePerSize) {
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
    public SqlToolOfNoNamedArgs sql(String sql) {
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
    public SqlToolOfNoNamedArgs sqlArgs(Object... sqlArgs) {
        for (Object sqlArg : sqlArgs) {
            this.sqlArgs.add(sqlArg);
        }
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
    public SqlToolOfNoNamedArgs sqlFile(Class sqlFileClass, String sqlFileName) {
        this.sqlFileClass = sqlFileClass;
        this.sqlFileName = sqlFileName;
        return this;
    }

    private SqlToolOfNoNamedArgs limitSql() {
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

    public SqlToolOfNoNamedArgs sqlFile(String sqlFileRelativePath) {
        if (sqlFileRelativePath.endsWith(".sql")) {
            this.sqlFileRelativePath = sqlFileRelativePath;
        } else {
            this.sqlFileRelativePath = sqlFileRelativePath + ".sql";
        }
        return this;
    }

}
