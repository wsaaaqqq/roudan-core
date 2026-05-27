package org.xht.xdb.sql;

import lombok.SneakyThrows;
import org.xht.xdb.util.CloseUtil;
import org.xht.xdb.util.ResultSetUtil;
import org.xht.xdb.vo.Row;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author xht
 */
@SuppressWarnings("unused")
public class ResultQueryBatch implements AutoCloseable {
    private final ResultSet resultSet;
    private final Connection conn;
    private final int batchSize;
    private final int colCount;
    private final ResultSetMetaData resultSetMetaData;

    /**
     * 结果查询批处理
     *
     * @param conn 连接
     * @param resultSet 结果集
     * @param batchSize 批量大小
     */
    public ResultQueryBatch(Connection conn, ResultSet resultSet, int batchSize) {
        ExecuteTimeHelp.debug();
        this.conn = conn;
        this.resultSet = resultSet;
        this.batchSize = batchSize;
        try {
            this.resultSetMetaData = resultSet.getMetaData();
            this.colCount = resultSetMetaData.getColumnCount() + 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 每批
     *
     * @param consumer 消费者
     */
    public void forEachBatch(Consumer<List<Map<String, Object>>> consumer) {
        for (List<Map<String, Object>> maps = nextBatch(); !maps.isEmpty(); maps = nextBatch()) {
            consumer.accept(maps);
        }
    }

    /**
     * 每批
     *
     * @param consumer 消费者
     */
    public void forEachBatchRow(Consumer<Collection<Row>> consumer) {
        for (List<Row> maps = nextBatchRow(); !maps.isEmpty(); maps = nextBatchRow()) {
            consumer.accept(maps);
        }
    }

    /**
     * 每批
     *
     * @param beanClass 类
     * @param consumer  消费者
     */
    public <T> void forEachBatchBean(Class<T> beanClass, Consumer<Collection<T>> consumer) {
        for (List<T> maps = nextBatchBean(beanClass); !maps.isEmpty(); maps = nextBatchBean(beanClass)) {
            consumer.accept(maps);
        }
    }

    /**
     * 对于每个批处理对象数组
     *
     * @param consumer 消费者
     */
    public <T> void forEachBatchObjectArray(Consumer<List<Object[]>> consumer) {
        for (List<Object[]> maps = nextBatchObjectArray(); !maps.isEmpty(); maps = nextBatchObjectArray()) {
            consumer.accept(maps);
        }
    }

    /**
     * 是否有下一批数据
     *
     * @return boolean
     */
    @SneakyThrows
    public boolean hasNextBatch() {
        return resultSet.next();
    }

    /**
     * 下一批数据（Map<String, Object>类型）
     *
     * @return {@link List}<{@link Map}<{@link String}, {@link Object}>>
     */
    @SneakyThrows
    public List<Map<String, Object>> nextBatch() {
        return ResultSetUtil.limit(resultSet, 0, batchSize, colCount, resultSetMetaData);
    }

    /**
     * 下一批数据（bean类型）
     *
     * @param beanClass bean类
     * @return {@link List}<{@code T}>
     */
    @SneakyThrows
    public <T> List<T> nextBatchBean(Class<T> beanClass) {
        return ResultSetUtil.limitBean(resultSet, beanClass, 0, batchSize, colCount, resultSetMetaData);
    }

    /**
     * 下一批数据（Row类型）
     *
     * @return {@link List }<{@link Row }>
     */
    @SneakyThrows
    public List<Row> nextBatchRow() {
        return ResultSetUtil.limitRow(resultSet, 0, batchSize, colCount, resultSetMetaData);
    }

    /**
     * 下一批数据（Object[]类型）
     */
    @SneakyThrows
    public List<Object[]> nextBatchObjectArray() {
        return ResultSetUtil.limitObjectArray(resultSet, 0, batchSize, colCount);
    }

    @Override
    public void close() {
        CloseUtil.close(resultSet);
        CloseUtil.close(conn);
    }
}
