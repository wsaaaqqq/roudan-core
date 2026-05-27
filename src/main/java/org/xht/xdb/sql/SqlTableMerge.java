package org.xht.xdb.sql;

import lombok.extern.slf4j.*;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.util.MergeUtil;
import org.xht.xdb.vo.DataCompareResult;
import org.xht.xdb.vo.Row;

import java.util.*;
import java.util.stream.*;

@SuppressWarnings("unused")
@Slf4j
public class SqlTableMerge {
    private final String tableName;
    private List<Row> dataNew = new ArrayList<>();
    private List<Row> dataOld = new ArrayList<>();
    private String keyFormats;
    private String[] keyColumns;
    private Integer batchSize;
    private boolean allowInsert = false;
    private boolean allowUpdate = false;
    private boolean allowDelete = false;
    private DataCompareResult<Row> debug;
    private boolean computeDelete = true;
    private boolean computeInsert = true;
    private boolean computeUpdate = true;

    /**
     * 构造 SqlTable
     *
     * @param tableName 待插入的表名
     */
    public SqlTableMerge(String tableName) {
        this.tableName = tableName;
        this.batchSize = 1000;
    }

    public SqlTableMerge computeInsert(boolean computeInsert) {
        this.computeInsert = computeInsert;
        return this;
    }

    public SqlTableMerge computeUpdate(boolean computeUpdate) {
        this.computeUpdate = computeUpdate;
        return this;
    }

    public SqlTableMerge computeDelete(boolean computeDelete) {
        this.computeDelete = computeDelete;
        return this;
    }

    public SqlTableMerge allowInsert(boolean allowInsert) {
        this.allowInsert = allowInsert;
        return this;
    }

    public SqlTableMerge allowUpdate(boolean allowUpdate) {
        this.allowUpdate = allowUpdate;
        return this;
    }

    public SqlTableMerge allowDelete(boolean allowDelete) {
        this.allowDelete = allowDelete;
        return this;
    }

    public SqlTableMerge batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SqlTableMerge keyColumns(String... keyColumns) {
        this.keyColumns = keyColumns;
        return this;
    }

    public SqlTableMerge dataNew(Collection<Row> dataNew) {
        if (this.dataNew == null) {
            this.dataNew = new ArrayList<>();
        } else {
            this.dataNew.clear();
        }
        this.dataNew.addAll(dataNew);
        return this;
    }

    public SqlTableMerge dataOldQueryTableAllData() {
        SqlTool sqlTool = Xdb.sql("select * from " + tableName);
        return dataOldQueryDb(sqlTool);
    }

    public SqlTableMerge dataOldQueryDb(SqlTool sqlTool) {
        Stream<Row> stream = sqlTool.executeQuery().resultRow().stream();
        if (keyFormats != null && !keyFormats.isEmpty()) {
            stream = stream.map(row -> row.formatKey(keyFormats));
        }
        this.dataOld = stream.collect(Collectors.toList());
        return this;
    }

    public SqlTableMerge dataOld(Collection<Row> dataOld) {
        if (this.dataOld == null) {
            this.dataOld = new ArrayList<>();
        } else {
            this.dataOld.clear();
        }
        this.dataOld.addAll(dataOld);
        return this;
    }

    /**
     * @param keyFormats beanField1=db_column1,beanField2=db_column2
     * @return {@link SqlTableMerge }
     */
    public SqlTableMerge formatKey(String keyFormats) {
        this.keyFormats = keyFormats;
        return this;
    }

    public DataCompareResult<Row> execute(boolean autoCommit, boolean... autoCloseConnection) {
        if (debug != null)
            return debug;
        debug = MergeUtil
                .init()
                .keyColumns(keyColumns)
                .dataOld(this.dataOld)
                .dataNew(this.dataNew)
                .computeInsert(computeInsert)
                .computeDelete(computeDelete)
                .computeUpdate(computeUpdate)
                .merge();
        if (allowInsert)
            Xdb
                    .table(tableName)
                    .save()
                    .rows(debug.getDataForInsert())
                    .batchSize(batchSize)
                    .execute(autoCommit, autoCloseConnection)
                    ;
        if (allowUpdate)
            Xdb
                    .table(tableName)
                    .update()
                    .rows(debug.getDataForUpdate())
                    .batchSize(batchSize)
                    .execute(autoCommit, autoCloseConnection)
                    ;
        if (allowDelete)
            Xdb
                    .table(tableName)
                    .delete()
                    .rows(debug.getDataForDelete())
                    .batchSize(batchSize)
                    .execute(autoCommit, autoCloseConnection)
                    ;
        return debug;
    }

    public DataCompareResult<Row> execute() {
        return execute(XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    /**
     * <pre>
     * 打印封装后的sql（参数、分页、limit）
     * </pre>
     *
     * @return {@link SqlTableMerge}
     */
    public SqlTableMerge debug() {
        debug = MergeUtil
                .init()
                .keyColumns(keyColumns)
                .dataOld(this.dataOld)
                .dataNew(this.dataNew)
                .computeInsert(computeInsert)
                .computeDelete(computeDelete)
                .computeUpdate(computeUpdate)
                .debug();
        return this;
    }

}
