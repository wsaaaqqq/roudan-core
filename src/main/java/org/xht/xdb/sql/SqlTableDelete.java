package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.Row;
import org.xht.xdb.vo.SqlArgBatchVo;

import java.util.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTableDelete {

    private final String tableName;
    private String id;
    private List<Row> rows;
    private int batchSize;

    public SqlTableDelete(String tableName) {
        ExecuteTimeHelp.executeStart();
        this.tableName = tableName;
        this.batchSize = 1000;
        this.id = "id";
        this.rows = new ArrayList<>();
    }

    public SqlTableDelete batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SqlTableDelete id(String idCol) {
        this.id = idCol;
        return this;
    }

    public SqlTableDelete row(Row row) {
        this.rows.add(row);
        return this;
    }

    public <T> SqlTableDelete rowBean(T t) {
        this.rows.add(BeanUtil.toRow(t));
        return this;
    }

    public SqlTableDelete rowMap(Map<String, Object> map) {
        Row row = new Row();
        row.putAll(map);
        this.rows.add(row);
        return this;
    }

    public SqlTableDelete rowMapUtil(MapUtil<Object> mapUtil) {
        rowMap(mapUtil.value());
        return this;
    }

    public <T> SqlTableDelete rowsBean(Collection<T> beans) {
        this.rows = BeanUtil.toRows(beans);
        return this;
    }

    public SqlTableDelete rowsMap(Collection<Map<String, Object>> maps) {
        this.rows.clear();
        for (Map<String, Object> map : maps) {
            rowMap(map);
        }
        return this;
    }

    public SqlTableDelete rows(Collection<Row> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
        return this;
    }

    public void execute(boolean autoCommit, boolean... autoCloseConnection) {
        SqlArgBatchVo vo = format();
        if (vo == null) {
            return;
        }
        ConnTool.executeBatch(vo.getSql(), vo.getParameters(), this.batchSize, autoCommit, autoCloseConnection);
        ExecuteTimeHelp.debug();
    }

    public void execute() {
        execute(XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    public SqlArgBatchVo format() {
        if (this.rows == null || this.rows.isEmpty()) {
            return null;
        }
        String sql = "delete from " + tableName + " where " + id + " = ?";
        List<Object[]> args = new ArrayList<>();
        Row row0 = this.rows.get(0);
        //默认兼容 id / ID
        id = SqlTableFormatHelper.getIdColName(row0, id);
        for (Row row : this.rows) {
            Object value = row.get(this.id);
            if (value != null) {
                args.add(new Object[]{value});
            }
        }
        return new SqlArgBatchVo(sql, args);
    }

    public SqlTableDelete debug() {
        Optional.ofNullable(format()).ifPresent(SqlArgBatchVo::debug);
        return this;
    }

}
