package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.Row;
import org.xht.xdb.vo.SqlArgBatchVo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTableSave {
    private final String tableName;
    private List<Row> rows = new ArrayList<>();
    private String keyFormats;
    private Integer batchSize;
    private boolean rowsFromBeans = false;

    public SqlTableSave setRowsFromBeans(boolean rowsFromBeans) {
        this.rowsFromBeans = rowsFromBeans;
        return this;
    }

    /**
     * 构造 SqlTable
     *
     * @param tableName 待插入的表名
     */
    public SqlTableSave(String tableName) {
        ExecuteTimeHelp.executeStart();
        this.tableName = tableName;
        this.batchSize = 1000;
    }

    public SqlTableSave batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public <T> SqlTableSave row(Row row) {
        this.rows.add(row);
        return this;
    }

    public <T> SqlTableSave rowBean(T t) {
        this.rows.add(BeanUtil.toRow(t));
        this.rowsFromBeans = true;
        return this;
    }

    public <T> SqlTableSave rowMap(Map<String, Object> map) {
        Row row = new Row();
        row.putAll(map);
        this.rows.add(row);
        return this;
    }

    public SqlTableSave rowMapUtil(MapUtil<Object> mapUtil) {
        rowMap(mapUtil.value());
        return this;
    }

    public <T> SqlTableSave rowsBean(Collection<T> beans) {
        this.rows = BeanUtil.toRows(beans);
        this.rowsFromBeans = true;
        return this;
    }

    public SqlTableSave rowsMap(Collection<Map<String, Object>> maps) {
        this.rows.clear();
        if (maps != null && !maps.isEmpty()) {
            maps.forEach(map -> this.rows.add(Row.of(map)));
        }
        return this;
    }

    public SqlTableSave rows(Collection<Row> rows) {
        if (this.rows == null)
            this.rows = new ArrayList<>();
        this.rows.clear();
        this.rows.addAll(rows);
        return this;
    }

    /**
     * @param keyFormats beanField1=db_column1,beanField2=db_column2
     * @return {@link SqlTableSave }
     */
    public SqlTableSave formatKey(String keyFormats) {
        this.keyFormats = keyFormats;
        return this;
    }

    public void execute(boolean autoCommit, boolean... autoCloseConnection) {
        formatKey();
        Map<String, List<Object[]>> sqlArgVoList =
                SqlTableFormatHelper.insertNoNamedSql(tableName, rows, this.rowsFromBeans);
        if (!sqlArgVoList.isEmpty()) {
            AtomicReference<String> sql = new AtomicReference<>();
            List<Object[]> args = new ArrayList<>();
            sqlArgVoList.forEach((_sql, parameters) -> {
                if (_sql != null && !_sql.isEmpty()) {
                    sql.set(_sql);
                    if (parameters != null && !parameters.isEmpty()) {
                        args.addAll(parameters);
                    }
                }
            });
            ConnTool.executeBatch(sql.get(), args, this.batchSize, autoCommit, autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
    }
//
//    public void execute(boolean autoCommit, boolean... autoCloseConnection) {
//        formatKey();
//        Map<String, List<Object[]>> sqlArgVoList =
//                SqlTableFormatHelper.insertNoNamedSql(tableName, rows, this.rowsFromBeans);
//        if (!sqlArgVoList.isEmpty()) {
//            sqlArgVoList.forEach((sql, parameters) -> {
//                if (sql != null && !sql.isEmpty()) {
//                    if (parameters != null && !parameters.isEmpty()) {
//                        ConnTool.executeBatch(sql, parameters, this.batchSize, autoCommit, autoCloseConnection);
//                    }
//                }
//            });
//        }
//        ExecuteTimeHelp.debug();
//    }

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

    /**
     * <pre>
     * 打印封装后的sql（参数、分页、limit）
     * </pre>
     *
     * @return {@link SqlTableSave}
     */
    public SqlTableSave debug() {
        Map<String, List<Object[]>> sqlArgBatchVos =
                SqlTableFormatHelper.insertNoNamedSql(tableName, this.rows, this.rowsFromBeans);
        sqlArgBatchVos.forEach((k, v) -> new SqlArgBatchVo(k, v).debug());
        return this;
    }

}
