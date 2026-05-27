package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.CloseUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.Row;
import org.xht.xdb.vo.SqlArgBatchVo;

import java.sql.Connection;
import java.util.*;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTableUpdate {
    private final String tableName;
    private List<Row> rows = new ArrayList<>();
    private final List<Object> idsForUpdate = new ArrayList<>();
    private String id;
    private String keyFormats;
    private Integer batchSize;
    private Boolean ignoreNulls;
    private boolean rowsFromBeans = false;

    public SqlTableUpdate setRowsFromBeans(boolean rowsFromBeans) {
        this.rowsFromBeans = rowsFromBeans;
        return this;
    }

    public SqlTableUpdate(String tableName) {
        ExecuteTimeHelp.executeStart();
        this.tableName = tableName;
        this.batchSize = 1000;
        this.ignoreNulls = true;
        this.id = "id";
    }

    public SqlTableUpdate batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SqlTableUpdate ignoreNulls(Boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public SqlTableUpdate id(String id) {
        this.id = id;
        return this;
    }

    public SqlTableUpdate row(Row row) {
        this.rows.add(row);
        return this;
    }

    public <T> SqlTableUpdate rowBean(T t) {
        this.rows.add(BeanUtil.toRow(t));
        this.rowsFromBeans = true;
        return this;
    }

    public <T> SqlTableUpdate rowsBean(Collection<T> beans) {
        this.rows = BeanUtil.toRows(beans);
        this.rowsFromBeans = true;
        return this;
    }

    public SqlTableUpdate rowMap(Map<String, Object> map) {
        Row row = new Row();
        row.putAll(map);
        this.rows.add(row);
        return this;
    }

    public SqlTableUpdate rowMapUtil(MapUtil<Object> mapUtil) {
        rowMap(mapUtil.value());
        return this;
    }

    public SqlTableUpdate rowsMap(Collection<Map<String, Object>> maps) {
        this.rows.clear();
        for (Map<String, Object> map : maps) {
            rowMap(map);
        }
        return this;
    }

    public SqlTableUpdate rows(Collection<Row> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
        return this;
    }

    /**
     * @param keyFormats beanField1=db_column1,beanField2=db_column2
     * @return {@link SqlTableUpdate }
     */
    public SqlTableUpdate formatKey(String keyFormats) {
        this.keyFormats = keyFormats;
        return this;
    }

    public void execute(boolean autoCommit, boolean... autoCloseConnection) {
        try {
            formatKey();
            List<SqlArgBatchVo> sqlArgList = SqlTableFormatHelper.updateNoNamedSql(
                    tableName,
                    rows,
                    this.ignoreNulls,
                    this.id,
                    this.idsForUpdate,
                    this.rowsFromBeans
            );
            Map<String, List<Object[]>> joinSameSqlArgs = SqlTableFormatHelper.joinSameSqlArgs(sqlArgList);
            Set<String> keySet = joinSameSqlArgs.keySet();
            if (!keySet.isEmpty()) {
                int i = 1;
                for (String sql : keySet) {
                    List<Object[]> parameters = joinSameSqlArgs.get(sql);
                    ConnTool.executeBatch(sql, parameters, this.batchSize, autoCommit, true);
                }
            }
        } finally {
            Connection conn = Xdb.getConnection();
            if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
                CloseUtil.close(conn);
            }
            ExecuteTimeHelp.debug();
        }
    }

    private void formatKey() {
        if (keyFormats != null && !keyFormats.isEmpty()) {
            for (Row row : rows) {
                row.formatKey(keyFormats);
            }
        }
    }

    public void execute() {
        execute(XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    public SqlTableUpdate debug() {
        List<SqlArgBatchVo> sqlArgBatchVos = SqlTableFormatHelper.updateNoNamedSql(
                tableName,
                this.rows,
                this.ignoreNulls,
                this.id,
                this.idsForUpdate,
                this.rowsFromBeans
        );
        if (!sqlArgBatchVos.isEmpty()) {
            for (SqlArgBatchVo sqlArgBatchVo : sqlArgBatchVos) {
                sqlArgBatchVo.debug();
            }
        }
        return this;
    }

    public <T, ID> SqlTableUpdate rowBean(T t, ID idForUpdate) {
        this.rowBean(t);
        this.rowsFromBeans = true;
        this.idsForUpdate.add(idForUpdate);
        return this;
    }
}
