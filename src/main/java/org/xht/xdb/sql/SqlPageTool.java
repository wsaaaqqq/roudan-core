package org.xht.xdb.sql;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.PageResult;
import org.xht.xdb.vo.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * sql执行工具
 */
@SuppressWarnings("ALL")
@Slf4j
public class SqlPageTool {
    private String sqlSelect;
    private String sqlMain;
    private String sqlOrder;
    private MapUtil sqlArgs;
    private String sql;
    private String sqlCount;
    private MapUtil sqlArgsCount;
    private SqlTool sqlTool;
    private SqlTool sqlToolCount;
    private Long pageIndex;
    private Long pagePerSize;

    public SqlPageTool sqlSelect(String sqlSelect) {
        this.sqlSelect = sqlSelect;
        return this;
    }

    public SqlPageTool sqlCount(String sqlCount) {
        this.sqlCount = sqlCount;
        return this;
    }

    public SqlPageTool sqlMain(String sqlMain) {
        this.sqlMain = sqlMain;
        return this;
    }

    public SqlPageTool sqlMain(String sqlMain, SqlBuild wherePart) {
        if (!sqlMain.endsWith("\n")) {
            sqlMain = sqlMain + "\n";
        }
        this.sqlMain = sqlMain + wherePart.getSql();
        if (this.sqlArgs == null) {
            this.sqlArgs = wherePart.getArgs();
        } else {
            sqlArgs.addAll(wherePart.getArgs());
        }
        return this;
    }

    public SqlPageTool sqlOrder(String sqlOrder) {
        this.sqlOrder = sqlOrder;
        return this;
    }

    public SqlPageTool sqlArgs(MapUtil sqlArgs) {
        if (this.sqlArgs == null) {
            this.sqlArgs = sqlArgs;
        } else {
            sqlArgs.addAll(sqlArgs);
        }
        return this;
    }

    /**
     * 序号从1起始
     */
    public SqlPageTool pageIndex(Object pageIndex) {
        this.pageIndex = pageIndex == null ? null : Long.parseLong(String.valueOf(pageIndex));
        return this;
    }

    public SqlPageTool pagePerSize(Object pagePerSize) {
        this.pagePerSize = pagePerSize == null ? null : Long.parseLong(String.valueOf(pagePerSize));
        return this;
    }

    private SqlPageTool prepare() {
        sql = String.format("%s \n %s \n %s", sqlSelect, sqlMain, Optional.ofNullable(sqlOrder).orElse(""));
        if (sqlArgs != null) {
            sqlArgsCount = sqlArgs.clone();
        }
        sqlTool = new SqlTool().sql(sql).sqlArgs(sqlArgs).pageIndex(pageIndex).pagePerSize(pagePerSize);
        if (sqlCount == null || sqlCount.isEmpty()) {
            sqlCount = String.format("%s %s", "select count(1) count", sqlMain);
        } else {
            sqlCount = String.format("%s %s", sqlCount, sqlMain);
        }
        sqlToolCount = new SqlTool().sql(sqlCount).sqlArgs(sqlArgsCount);
        return this;
    }

    public @NonNull PageResult<Map<String, Object>> result(boolean... autoCloseConnection) {
        prepare();
        return new PageResult<>(
                sqlToolCount.executeQuery(autoCloseConnection).firstBean(Long.class),
                sqlTool.executeQuery(autoCloseConnection).result()
        );
    }

    public @NonNull PageResult<Map<String, Object>> result() {
        return result(XdbConfig.isAutoClose());
    }

    public @NonNull PageResult<Row> resultRow(boolean... autoCloseConnection) {
        prepare();
        return new PageResult<>(
                sqlToolCount.executeQuery(autoCloseConnection).firstBean(Long.class),
                sqlTool.executeQuery(autoCloseConnection).resultRow()
        );
    }

    public @NonNull PageResult<Row> resultRow() {
        return resultRow(XdbConfig.isAutoClose());
    }

    public @NonNull PageResult<Object[]> resultArray(boolean... autoCloseConnection) {
        prepare();
        return new PageResult<>(
                sqlToolCount.executeQuery(autoCloseConnection).firstBean(Long.class),
                sqlTool.executeQuery(autoCloseConnection).resultArray()
        );
    }

    public @NonNull PageResult<Object[]> resultArray() {
        return resultArray(XdbConfig.isAutoClose());
    }

    public <T> @NonNull PageResult<T> resultBean(Class<T> tClass, boolean... autoCloseConnection) {
        prepare();
        return new PageResult<>(
                sqlToolCount.executeQuery(autoCloseConnection).firstBean(Long.class),
                sqlTool.executeQuery(autoCloseConnection).resultBean(tClass)
        );
    }

    public <T> @NonNull PageResult<T> resultBean(Class<T> tClass) {
        return resultBean(tClass, XdbConfig.isAutoClose());
    }

    public <T> @NonNull PageResult<T> resultBean(Class<T> tClass, Function<Row, T> format,
            boolean... autoCloseConnection
    ) {
        prepare();
        Long count = sqlToolCount.executeQuery(autoCloseConnection).firstBean(Long.class);
        List<T> items = new ArrayList<>();
        try (ResultQueryBatch queryBatch = sqlTool.executeQuery(autoCloseConnection).resultBatch(200);
        ) {
            queryBatch.forEachBatchRow(rows -> {
                rows.forEach(row -> {
                    if (row == null) {
                        items.add(null);
                    } else {
                        T t = format.apply(row);
                        items.add(t);
                    }
                });
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new PageResult<>(count, items);
    }

    public <T> @NonNull PageResult<T> resultBean(Class<T> tClass, Function<Row, T> format) {
        return resultBean(tClass, format, XdbConfig.isAutoClose());
    }

    public void debug() {
        prepare();
        sqlToolCount.debug();
        sqlTool.debug();
    }
}
