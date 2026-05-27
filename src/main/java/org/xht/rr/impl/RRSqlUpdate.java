package org.xht.rr.impl;

import lombok.NonNull;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RRSqlUpdate {

    private final SqlTool tool = new SqlTool();
    private List<Object[]> batchValues;

    public RRSqlUpdate sql(String sql) {
        tool.sql(sql);
        return this;
    }

    public RRSqlUpdate sqlFile(String sqlFileRelativePath) {
        tool.sqlFile(sqlFileRelativePath);
        return this;
    }

    public RRSqlUpdate sqlFile(Class sqlFileClass, String sqlFileName) {
        tool.sqlFile(sqlFileClass, sqlFileName);
        return this;
    }

    public RRSqlUpdate argsBatch(List<Object[]> args) {
        this.batchValues = args;
        return this;
    }

    public RRSqlUpdate argsBatch(Consumer<@NonNull List<Object[]>> addFunction) {
        if (this.batchValues == null) {
            this.batchValues = new ArrayList<>();
        }
        addFunction.accept(this.batchValues);
        return this;
    }

    public RRSqlUpdate args(MapUtil args) {
        tool.sqlArgs(args);
        return this;
    }

    public RRSqlUpdate args(String key, Object sqlArg) {
        tool.sqlArg(key, sqlArg);
        return this;
    }

    public RRSqlUpdate args(String key, Object sqlArg, boolean condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public RRSqlUpdate args(String key, Object sqlArg, Supplier<Boolean> condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public int execute(boolean... autoCloseConnection) {
        return tool.executeUpdate(autoCloseConnection);
    }

    public int execute() {
        return tool.executeUpdate();
    }

    public void executeBatch(int batchSize, boolean autoCommit, boolean... autoCloseConnection) {
        tool.executeBatch(batchValues, batchSize, autoCommit, autoCloseConnection);
    }

    public void executeBatch(int batchSize) {
        tool.executeBatch(batchValues, batchSize, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

}
