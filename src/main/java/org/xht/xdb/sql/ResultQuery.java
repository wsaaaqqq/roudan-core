package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.util.CloseUtil;
import org.xht.xdb.util.ResultSetUtil;
import org.xht.xdb.util.RowTransformer;
import org.xht.xdb.vo.Row;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Function;

/**
 * ResultQuery是sql查询结果的封装类
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class ResultQuery {

    /**
     * 自动关闭连接
     */
    private final boolean[] autoCloseConnection;
    /**
     * jdbc连接
     */
    private final Connection conn;

    @Override
    public String toString() {
        return "ResultQuery{sql='" + sql + '\'' + '}';
    }

    /**
     * sql执行结果
     */
    private final ResultSet resultSet;

    /**
     * 执行的sql
     */
    private final String sql;

    public ResultQuery(String sql, ResultSet resultSet, Connection conn, boolean... autoCloseConnection) {
        this.sql = sql;
        this.resultSet = resultSet;
        this.autoCloseConnection = autoCloseConnection;
        this.conn = conn;
    }

    /**
     * 返回查询结果集的limit部分
     *
     * @return Map
     */
    public List<Map<String, Object>> limit(int start, int count) {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.limit(this.resultSet, start, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集的limit部分
     */
    public <T> List<T> limitBean(Class<T> beanClass, int start, int count) {
        List<T> list;
        try {
            list = ResultSetUtil.limitBean(this.resultSet, beanClass, start, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集的limit部分
     */
    public List<Object[]> limitObjectArray(int start, int count) {
        List<Object[]> list;
        try {
            list = ResultSetUtil.limitObjectArray(this.resultSet, start, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集的第一个元素，若查询结果为空或执行失败时，返回空的map实例对象
     *
     * @return Map
     */
    public Map<String, Object> first() {
        Map<String, Object> r;
        try {
            r = ResultSetUtil.first(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return r;
    }

    /**
     * same as: firstRowUpperCase
     *
     * @return Row
     */
    public Row firstRow() {
        Row r;
        try {
            r = ResultSetUtil.firstRow(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return r;
    }

    public Row firstRowOrigin() {
        Row first;
        try {
            first = ResultSetUtil.firstRowOrigin(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return first;
    }

    public Row firstRowCamel() {
        Row first;
        try {
            first = ResultSetUtil.firstRowCamel(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return first;
    }

    public Row firstRowLowerCase() {
        Row first;
        try {
            first = ResultSetUtil.firstRowLowerCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return first;
    }

    public Row firstRowUpperCase() {
        Row first;
        try {
            first = ResultSetUtil.firstRowUpperCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return first;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     *
     * @return List
     */
    public Object[] firstArray() {
        Object[] first;
        try {
            first = ResultSetUtil.firstArray(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return first;
    }

    /**
     * 返回查询结果集的第一个元素，若查询结果为空或执行失败时，返回空的map实例对象
     *
     * @return Map
     */
    public <T> T firstBean(Class<T> beanClass) {
        T t;
        try {
            t = ResultSetUtil.firstBean(this.resultSet, beanClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return t;
    }

    /**
     * 第一行第一列的结果
     */
    public <T> T firstRowFirstCol(Class<T> beanClass) {
        return firstBean(beanClass);
    }

    /**
     * 返回查询结果集
     *
     * @return ResultSet
     */
    public ResultSet resultSet() {
        return this.resultSet;
    }

    /**
     * same as: resultRowOrigin
     *
     * @return List
     */
    public List<Map<String, Object>> result() {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.toList(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * same as: resultRowUpperCase
     *
     * @return List
     */
    public List<Row> resultRow() {
        List<Row> list;
        try {
            list = ResultSetUtil.toListRow(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    public List<Row> resultRowOrigin() {
        List<Row> list;
        try {
            list = ResultSetUtil.toListRowOrigin(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    public List<Row> resultRowCamel() {
        List<Row> list;
        try {
            list = ResultSetUtil.toListRowCamel(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    public List<Row> resultRowLowerCase() {
        List<Row> list;
        try {
            list = ResultSetUtil.toListRowLowerCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    public List<Row> resultRowUpperCase() {
        List<Row> list;
        try {
            list = ResultSetUtil.toListRowUpperCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     *
     * @return List
     */
    public List<Object[]> resultArray() {
        List<Object[]> list;
        try {
            list = ResultSetUtil.toListArray(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     *
     * @return List
     */
    public <T> List<T> resultBean(Class<T> beanClass) {
        List<T> list;
        try {
            list = ResultSetUtil.toListBean(this.resultSet, beanClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的TreeMap实例对象
     *
     * @return TreeMap
     */
    public <V extends Comparable<V>> TreeMap<V, V> resultTreeMap(Class<V> beanClass) {
        TreeMap<V, V> list;
        try {
            list = ResultSetUtil.toTreeMap(this.resultSet, beanClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的TreeMap实例对象
     *
     * @return TreeMap
     */
    public <V> TreeMap<V, V> resultTreeMap(Class<V> beanClass, Comparator<? super V> comparator) {
        TreeMap<V, V> list;
        try {
            list = ResultSetUtil.toTreeMap(this.resultSet, beanClass, comparator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的TreeMap实例对象
     *
     * @return TreeMap
     */
    public <K extends Comparable<K>, V> TreeMap<K, V> resultTreeMap(Class<V> beanClass, Function<V, K> keyFunction) {
        TreeMap<K, V> list;
        try {
            list = ResultSetUtil.toTreeMap(this.resultSet, beanClass, keyFunction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的TreeMap实例对象
     *
     * @return TreeMap
     */
    public <K, V> TreeMap<K, V> resultTreeMap(Class<V> beanClass, Function<V, K> keyFunction,
            Comparator<? super K> comparator
    ) {
        TreeMap<K, V> list;
        try {
            list = ResultSetUtil.toTreeMap(this.resultSet, beanClass, keyFunction, comparator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     *
     * @return List
     */
    public <T> List<T> resultFirstColumn(Class<T> beanClass) {
        List<T> list;
        try {
            list = ResultSetUtil.toListBeanFirstColumn(this.resultSet, beanClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 返回查询结果集合，若查询结果为空或执行失败时返回空的list实例对象
     *
     * @return List
     */
    public <T> List<T> resultTo(RowTransformer<T> rowTransformer) {
        List<T> list;
        try {
            list = ResultSetUtil.to(this.resultSet, rowTransformer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    public ResultQueryBatch resultBatch(int batchSize) {
        return new ResultQueryBatch(this.conn, this.resultSet, batchSize);
    }

    /**
     * 将查询结果集转换：列名（key值）转换为驼峰的格式
     *
     * @return List
     */
    public List<Map<String, Object>> toCamelList() {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.toListCamp(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 将查询结果集转换：列名（key值）转换为小写的格式
     *
     * @return List
     */
    public List<Map<String, Object>> toLowerCaseList() {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.toListLowerCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 将查询结果集转换：列名（key值）转换为大写的格式
     *
     * @return List
     */
    public List<Map<String, Object>> toUpperCaseList() {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.toListUpperCase(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 将查询结果集转换：列名（key值）转换为大写的格式
     *
     * @return List
     */
    public List<Map<String, Object>> toOriginList() {
        List<Map<String, Object>> list;
        try {
            list = ResultSetUtil.toListOrigin(this.resultSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            CloseUtil.close(this.resultSet);
            CloseUtil.close(this.conn, this.autoCloseConnection);
        }
        ExecuteTimeHelp.debug();
        return list;
    }

    /**
     * 将group-count查询的结果集转换为map
     * <pre>
     * demo sql：
     * select
     *    type as g,
     *    count(1) as c
     * from xxx
     * group by type
     * </pre>
     *
     * @return Map
     */
    public Map<String, Object> toGroupCount() {
        return toGroupCount("g", "c");
    }

    /**
     * 将group-count查询的结果集转换为map
     * <pre>
     * demo java:
     * .toGroupCount("g1","c1")
     *
     * demo sql：
     * select
     *    type as g1,
     *    count(1) as c1
     * from xxx
     * group by type
     * </pre>
     *
     * @return Map
     */
    public Map<String, Object> toGroupCount(String groupKey, String countKey) {
        Map<String, Object> r = new HashMap<>();
        List<Map<String, Object>> result = result();
        if (result != null) {
            for (Map<String, Object> map : result) {
                r.put(String.valueOf(map.get(groupKey)), map.get(countKey));
            }
        }
        ExecuteTimeHelp.debug();
        return r;
    }

    /**
     * 打印结果，同时打印集合元素个数
     * <pre>
     * </pre>
     *
     * @return ResultQuery
     */
    public ResultQuery out() {
        List<Map<String, Object>> list = result();
        for (Object o : list) {
            log.info(String.valueOf(o));
        }
        log.info("count: {}", list.size());
        ExecuteTimeHelp.debug();
        return this;
    }

}
