package org.xht.rr.impl;

import lombok.NonNull;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.MapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RRSqlNamedUpdate {

    private final SqlTool tool = new SqlTool();

    private List<Map<String, Object>> batchValues;
    private String sql;
    private String sqlFilePath;

    public RRSqlNamedUpdate sql(String sql) {
        tool.sql(sql);
        return this;
    }

    public RRSqlNamedUpdate sqlFile(String sqlFileRelativePath) {
        tool.sqlFile(sqlFileRelativePath);
        return this;
    }

    public RRSqlNamedUpdate sqlFile(Class sqlFileClass, String sqlFileName) {
        tool.sqlFile(sqlFileClass, sqlFileName);
        return this;
    }

    public RRSqlNamedUpdate args(MapUtil args) {
        tool.sqlArgs(args);
        return this;
    }

    public RRSqlNamedUpdate args(String key, Object sqlArg) {
        tool.sqlArg(key, sqlArg);
        return this;
    }

    public RRSqlNamedUpdate args(String key, Object sqlArg, boolean condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public RRSqlNamedUpdate args(String key, Object sqlArg, Supplier<Boolean> condition) {
        tool.sqlArgIf(key, sqlArg, condition);
        return this;
    }

    public RRSqlNamedUpdate argsBatch(List<Map<String, Object>> args) {
        this.batchValues = args;
        return this;
    }

    public RRSqlNamedUpdate argsBatch(Consumer<@NonNull List<Map<String, Object>>> addFunction) {
        if (this.batchValues == null) {
            this.batchValues = new ArrayList<>();
        }
        addFunction.accept(this.batchValues);
        return this;
    }

    public int execute(boolean... autoCloseConnection) {
        return tool.executeUpdate(autoCloseConnection);
    }

    public int execute() {
        return tool.executeUpdate();
    }

    public void executeBatch(int batchSize, boolean autoCommit, boolean... autoCloseConnection) {
        ConnTool.executeBatchRow(tool.getSql(), batchValues, batchSize, autoCommit, autoCloseConnection);
    }

    public void executeBatch(int batchSize) {
        ConnTool.executeBatchRow(
                tool.getSql(),
                batchValues,
                batchSize,
                XdbConfig.isAutoCommit(),
                XdbConfig.isAutoClose()
        );
    }

}
