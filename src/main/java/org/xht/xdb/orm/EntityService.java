package org.xht.xdb.orm;

import lombok.NonNull;
import org.xht.xdb.sql.SqlPageTool;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.PageResult;
import org.xht.xdb.vo.SaveOrUpdateBatchResult;
import org.xht.xdb.vo.Wheres;
import org.xht.xdb.vo.WheresBean;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface EntityService<T> {

    default WheresBean<T> wheres() {
        return WheresBean.init(getBeanClass());
    }

    Class<T> getBeanClass();

    static <E> EntityService<E> of(Class<E> beanClass, String datasource) {
        return new EntityServiceImp<>(beanClass, datasource);
    }

    long count(Wheres wheres);

    long count(WheresBean<T> wheres);

    EntityService<T> datasource(String datasource);

    void saveOrUpdate(T t);

    void saveOrUpdate(T t, boolean ignoreNulls);

    void saveOrUpdate(Collection<T> list, boolean ignoreNulls);

    SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Collection<?> idsInDb, Integer batchSize,
            boolean ignoreNulls, boolean computeReturn
    );

    SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void saveOrUpdate(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void save(T t);

    void save(Collection<T> list, Integer batchSize);

    void update(T t);

    <ID> void update(T t, boolean ignoreNulls, ID idForUpdate);

    void update(T t, boolean ignoreNulls);

    void update(Collection<T> list, Integer batchSize);

    void update(Collection<T> list, Integer batchSize, boolean ignoreNulls);

    void delete(T t);

    void delete(Collection<T> list, Integer batchSize);

    <ID> void deleteById(ID id);

    <ID> void deleteById(Collection<ID> ids, Integer batchSize);

    <ID> boolean existId(ID id);

    boolean exist(T t);

    boolean exist(Wheres wheres);

    boolean exist(WheresBean<T> wheres);

    <ID> boolean notExistId(ID id);

    boolean notExist(T t);

    @NonNull
    <ID> List<T> getByIds(List<ID> ids);

    @NonNull
    <ID> List<T> getByIds(List<ID> ids, int batchSize);

    @NonNull
    <ID> List<T> getByIds(List<ID> ids, List<T> list);

    @NonNull
    <ID> List<T> getByIds(List<ID> ids, int batchSize, List<T> list);

    @NonNull
    <ID> Set<T> getByIds(List<ID> ids, Set<T> list);

    @NonNull
    <ID> Set<T> getByIds(List<ID> ids, int batchSize, Set<T> list);

    @NonNull
    <ID> Map<ID, T> infos(List<ID> ids, int batchSize);

    @NonNull
    <ID> Map<ID, T> infos(List<ID> ids);

    <ID> Optional<T> getByIdOpt(ID id);

    <ID> T getById(ID id);

    @NonNull
    <ID> List<T> getById(Collection<ID> ids, Integer batchSize);

    @NonNull
    <ID> List<ID> ids(Class<ID> idType);

    @NonNull <ID> List<ID> ids(Class<ID> idType, Wheres wheres);

    @NonNull <ID> List<ID> ids(Class<ID> idType, WheresBean<T> wheres);

    @NonNull
    <ID> List<ID> ids(Class<ID> idType, List<ID> list);

    @NonNull <ID> List<ID> ids(Class<ID> idType, List<ID> list, Wheres wheres);

    @NonNull <ID> List<ID> ids(Class<ID> idType, List<ID> list, WheresBean<T> wheres);

    @NonNull
    <ID> Set<ID> ids(Class<ID> idType, Set<ID> set);

    @NonNull <R> List<R> asList(String whereSql, Class<R> rClass);

    @NonNull
    List<T> listAll();

    @NonNull <R> List<R> asList(Wheres wheres, Class<R> rClass);

    @NonNull
    List<T> list(WheresBean<T> wheres);

    @NonNull <R> List<R> asListAll(Class<R> rClass);

    @NonNull
    List<T> list(Wheres wheres);

    @NonNull <ID> Set<ID> ids(Class<ID> idType, Set<ID> set, Wheres wheres);

    @NonNull <ID> Set<ID> ids(Class<ID> idType, Set<ID> set, WheresBean<T> wheres);

    SqlTool sql(String sql);

    SqlPageTool sqlPage();

    @NonNull
    List<T> list(String whereSql);

    @NonNull <R> List<R> asList(WheresBean<T> wheres, Class<R> rClass);

    @NonNull List<T> list(Consumer<WheresBean<T>> consumer);

    @NonNull
    List<T> list(String whereSql, MapUtil<?> args);

    @NonNull
    PageResult<T> page(Wheres wheres);

    @NonNull
    PageResult<T> page(WheresBean<T> wheres);

    @NonNull
    PageResult<T> page(String whereSql, Number pageIndex, Number pageSize);

    @NonNull <R> List<R> asList(String whereSql, MapUtil<?> args, Class<R> rClass);

    @NonNull
    PageResult<T> page(String whereSql, MapUtil<?> args, Number pageIndex, Number pageSize);

    long count();

    long count(String whereSql);

    long count(String whereSql, MapUtil<?> args);
}
