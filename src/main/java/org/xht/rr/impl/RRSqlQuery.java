package org.xht.rr.impl;

import org.xht.xdb.function.ResultSetHandler;
import org.xht.xdb.function.ResultSetRowHandler;
import org.xht.xdb.sql.ResultQuery;
import org.xht.xdb.sql.SqlToolOfNoNamedArgs;

import java.sql.ResultSet;

public class RRSqlQuery {

    private final SqlToolOfNoNamedArgs tool = new SqlToolOfNoNamedArgs();

    public RRSqlQuery sql(String sql) {
        tool.sql(sql);
        return this;
    }

    public RRSqlQuery sqlFile(String sqlFileRelativePath) {
        tool.sqlFile(sqlFileRelativePath);
        return this;
    }

    public RRSqlQuery sqlFile(Class sqlFileClass, String sqlFileName) {
        tool.sqlFile(sqlFileClass, sqlFileName);
        return this;
    }

    public RRSqlQuery args(Object... args) {
        tool.sqlArgs(args);
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
