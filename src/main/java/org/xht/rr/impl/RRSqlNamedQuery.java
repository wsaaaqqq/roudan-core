package org.xht.rr.impl;

import org.xht.xdb.function.ResultSetHandler;
import org.xht.xdb.function.ResultSetRowHandler;
import org.xht.xdb.sql.ResultQuery;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.MapUtil;

import java.sql.ResultSet;
import java.util.function.Supplier;

public class RRSqlNamedQuery {

    private final SqlTool tool = new SqlTool();

    public RRSqlNamedQuery sql(String sql) {
        tool.sql(sql);
        return this;
    }

    public RRSqlNamedQuery sqlFile(String sqlFileRelativePath) {
        tool.sqlFile(sqlFileRelativePath);
        return this;
    }

    public RRSqlNamedQuery sqlFile(Class sqlFileClass, String sqlFileName) {
        tool.sqlFile(sqlFileClass, sqlFileName);
        return this;
    }

    public RRSqlNamedQuery args(MapUtil args) {
        tool.sqlArgs(args);
        return this;
    }

    public RRSqlNamedQuery args(String key, Object sqlArg) {
        tool.sqlArg(key, sqlArg);
        return this;
    }

    public RRSqlNamedQuery args(String key, Object sqlArg, boolean condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public RRSqlNamedQuery args(String key, Object sqlArg, Supplier<Boolean> condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public ResultQuery executeQuery(boolean... autoCloseConnection) {
        return tool.executeQuery(autoCloseConnection);
    }

    public ResultQuery executeQuery() {
        return tool.executeQuery();
    }

    public long executeCount(boolean... autoCloseConnection) {
        return tool.executeCount(autoCloseConnection);
    }

    public long executeCount() {
        return tool.executeCount();
    }

    public <T> T executeWithHandler(ResultSetHandler<T> resultSetHandler) {
        return tool.resultSetHandler(resultSetHandler);
    }

    public long executeWithHandler(ResultSetRowHandler resultSetRowHandler, int batchSize) {
        return tool.resultSetRowHandler(resultSetRowHandler, batchSize);
    }

    public ResultSet executeQueryResultSet() {
        return tool.executeQueryResultSet();
    }
}
