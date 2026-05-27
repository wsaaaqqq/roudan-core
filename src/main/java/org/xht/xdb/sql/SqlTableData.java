package org.xht.xdb.sql;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
@Data
public class SqlTableData {
    private List<Row> rows = new ArrayList<>();
    private String keyFormats;

    public SqlTableData() {
    }

    public <T> SqlTableData add(Row row) {
        this.rows.add(row);
        return this;
    }

    public <T> SqlTableData addBean(T t) {
        this.rows.add(BeanUtil.toRow(t));
        return this;
    }

    public <T> SqlTableData addBeans(List<T> t) {
        if (t != null && !t.isEmpty()) {
            this.rows.addAll(BeanUtil.toRows(t));
        }
        return this;
    }

    public <T> SqlTableData addMap(Map<String, Object> map) {
        Row row = new Row();
        row.putAll(map);
        this.rows.add(row);
        return this;
    }

    public SqlTableData addMapUtil(MapUtil<Object> mapUtil) {
        addMap(mapUtil.value());
        return this;
    }

    public <T> SqlTableData rowsBean(List<T> beans) {
        this.rows = BeanUtil.toRows(beans);
        return this;
    }

    public SqlTableData rowsMap(List<Map<String, Object>> maps) {
        this.rows.clear();
        for (Map<String, Object> map : maps) {
            addMap(map);
        }
        return this;
    }

    public SqlTableData rows(List<Row> rows) {
        this.rows = rows;
        return this;
    }

}
