package org.xht.xdb.orm;

import lombok.NonNull;
import org.xht.xdb.sql.SqlPageTool;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.*;

import java.util.*;

@SuppressWarnings("unused")
public interface EntityKeysService<T> {

    //    static <E> EntityKeyService<E> of(Class<E> beanClass, String datasource) {
    //        return new EntityServiceImp<>(beanClass, datasource);
    //    }

    EntityKeysService<T> datasource(String datasource);

    void saveOrUpdate(T t);

    void saveOrUpdate(T t, boolean ignoreNulls);

    void saveOrUpdate(Collection<T> list, boolean ignoreNulls);

    /**
     * 保存或更新，然后返回
     *
     * @param list          列表
     * @param idsInDb       row lower case
     * @param batchSize     批量大小
     * @param ignoreNulls   忽略空值
     * @param computeReturn 计算回报
     * @return {@link SaveOrUpdateBatchResult }<{@link T }>
     */
    SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Collection<Row> idsInDb, Integer batchSize,
            boolean ignoreNulls, boolean computeReturn
    );

    SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void saveOrUpdate(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void save(T t);

    void save(Collection<T> list, Integer batchSize);

    void update(T t);

    void update(T t, boolean ignoreNulls, T idForUpdate);

    void update(T t, boolean ignoreNulls);

    void update(Collection<T> list, Integer batchSize);

    void update(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void delete(T t);

    void delete(Collection<T> list, Integer batchSize);

    void deleteByKeys(T id);

    void deleteByKeys(Collection<T> ids, Integer batchSize);

    boolean exist(T t);

    boolean notExist(T t);

    @NonNull
    List<T> getByKeys(Collection<T> ids);

    @NonNull
    List<T> getByKeys(Collection<T> ids, int batchSize);

    Optional<T> getByKeysOpt(T id);

    T getByKeys(T id);

    @NonNull
    Map<T, T> infos(List<T> ids, int batchSize);

    @NonNull
    Map<T, T> infos(List<T> ids);

    @NonNull
    List<T> keys();

    @NonNull
    Set<T> keysSet();

    @NonNull
    List<T> listAll();

    @NonNull
    List<T> list(WheresBean<T> wheres);

    @NonNull
    List<T> list(Wheres wheres);

    SqlTool sql(String sql);

    SqlPageTool sqlPage();

    @NonNull
    List<T> list(String whereSql);

    @NonNull
    List<T> list(String whereSql, MapUtil<?> args);

    @NonNull
    PageResult<T> page(Wheres wheres);

    @NonNull
    PageResult<T> page(WheresBean<T> wheres);

    @NonNull
    PageResult<T> page(String whereSql, Number pageIndex, Number pageSize);

    @NonNull
    PageResult<T> page(String whereSql, MapUtil<?> args, Number pageIndex, Number pageSize);

    long count();

    long count(String whereSql);

    long count(String whereSql, MapUtil<?> args);
}
