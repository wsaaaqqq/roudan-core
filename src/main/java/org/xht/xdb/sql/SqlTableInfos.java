package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.util.CollectionUtl;
import org.xht.xdb.util.ListUtil;
import org.xht.xdb.vo.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTableInfos {
    private final String tableName;
    private String idCol;
    private int batchSize = 500;
    private Collection<?> ids;

    public SqlTableInfos(String tableName) {
        this.tableName = tableName;
    }

    public <ID> SqlTableInfos ids(String idCol, Collection<ID> ids) {
        this.idCol = idCol;
        this.ids = ids;
        return this;
    }

    public SqlTableInfos batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public List<Row> execute(boolean... autoCloseConnection) {
        List<Row> all;
        if (batchSize > 0 && ids.size() <= batchSize) {
            all = Xdb.sql("select * from " + tableName + " where " + idCol + " in ( :" + idCol + " ) ")
                     .sqlArg(idCol, ids)
                     .executeQuery(autoCloseConnection)
                     .resultRow();
        } else {
            all = new ArrayList<>();
            CollectionUtl.split(
                    ids, batchSize, ids -> {
                        try (ResultQueryBatch queryBatch = Xdb.sql("select * from " +
                                                                           tableName +
                                                                           " where " +
                                                                           idCol +
                                                                           " in ( :" +
                                                                           idCol +
                                                                           " ) ")
                                                              .sqlArg(idCol, ids)
                                                              .executeQuery()
                                                              .resultBatch(batchSize)
                        ) {
                            queryBatch.forEachBatchRow(all::addAll);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
        return all;
    }

    public List<Row> execute() {
        return execute(XdbConfig.isAutoClose());
    }

    public <T> List<T> execute(Class<T> tClass, boolean... autoCloseConnection) {
        String sql = "select * from " + tableName + " where " + idCol + " in ( :" + idCol + " )";
        List<T> data = new ArrayList<>();
        ListUtil.batchCollection(ids, batchSize).forEach(_ids -> {
            try (ResultQueryBatch resultQueryBatch = Xdb.sql(sql)
                                                        .sqlArg(idCol, _ids)
                                                        .executeQuery(autoCloseConnection)
                                                        .resultBatch(batchSize)
            ) {
                resultQueryBatch.forEachBatchBean(tClass, data::addAll);
            }
        });
        return data;
    }

    public <T> List<T> execute(Class<T> tClass) {
        return execute(tClass, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    public SqlTableInfos debug() {
        Xdb.sql("select * from " + tableName + " where " + idCol + " in ( :" + idCol + " )").sqlArg(idCol, ids).debug();
        return this;
    }

}
