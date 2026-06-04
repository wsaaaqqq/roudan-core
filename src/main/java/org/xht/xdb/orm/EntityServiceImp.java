package org.xht.xdb.orm;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.sql.SqlPageTool;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.sql.statement.ConnTool;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.ListUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.util.SerializableFunction;
import org.xht.xdb.vo.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class EntityServiceImp<T> implements EntityService<T> {

    private Class<T> beanClass;
    private String datasource;

    public EntityServiceImp(Class<T> beanClass, String datasource) {
        this.beanClass = beanClass;
        this.datasource = datasource;
    }

    @Override
    public long count() {
        return count("");
    }

    @Override
    public long count(String whereSql) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String sql = "select count(1) from " + tableName + " " + whereSql;
        datasource(this.datasource);
        return Xdb.sql(sql).executeQuery().firstRowFirstCol(Long.class);
    }

    @Override
    public long count(String whereSql, MapUtil<?> args) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String sql = "select count(1) from " + tableName + " " + whereSql;
        datasource(this.datasource);
        return Xdb.sql(sql).sqlArgs(args).executeQuery().firstRowFirstCol(Long.class);
    }

    @Override
    public long count(Wheres wheres) {
        return count(wheres.getWhereSql(), wheres.getArgs());
    }

    @Override
    public long count(WheresBean<T> wheres) {
        MapUtil<?> args = wheres.getArgs();
        if (args == null)
            return count(wheres.getWhereSql());
        return count(wheres.getWhereSql(), args);
    }

    @Override
    public EntityService<T> datasource(String datasource) {
        if (datasource != null && !datasource.isEmpty()) {
            Xdb.selectDataSourceByName(datasource);
        }
        return this;
    }

    @Override
    public void save(T t) {
        if (t == null)
            return;
        if (t instanceof Collection) {
            //noinspection unchecked
            save((Collection<T>) t, 1000);
            return;
        }
        datasource(this.datasource);
        String tableName = OrmAnnoUtil.getTableName(t);
        Xdb.table(tableName).save().rowBean(t).execute();
    }

    @Override
    public void save(Collection<T> list, Integer batchSize) {
        if (list == null || list.isEmpty())
            return;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        datasource(this.datasource);
        Xdb.table(tableName).save().rowsBean(list).batchSize(batchSize).execute();
    }

    @Override
    public void saveOrUpdate(T t) {
        if (t instanceof Collection) {
            //noinspection unchecked
            saveOrUpdate((Collection<T>) t, 1000, true);
            return;
        }
        saveOrUpdate(t, true);
    }

    @Override
    public void saveOrUpdate(T t, boolean ignoreNulls) {
        saveOrUpdate(Collections.singletonList(t), 1, ignoreNulls);
    }

    @Override
    public void saveOrUpdate(Collection<T> list, boolean ignoreNulls) {
        saveOrUpdate(list, 1000, ignoreNulls);
    }

    @Override
    public SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Collection<?> idsInDb,
                                                             Integer batchSize, boolean ignoreNulls, boolean computeReturn
    ) {
        SaveOrUpdateBatchResult<T> ret = new SaveOrUpdateBatchResult<>();
        if (list == null || list.isEmpty())
            return ret;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        Class<?> idColJavaType = OrmAnnoUtil.getIdColJavaTypeByBeanClass(beanClass);
        datasource(this.datasource);
        if (idsInDb == null) {
            idsInDb = new HashSet<>(Xdb.sql("select " + idColName + " from " + tableName)
                    .executeQuery()
                    .resultFirstColumn(idColJavaType));
        }
        if (idsInDb.isEmpty()) {
            save(list, batchSize);
            ret.setSaveList(list);
            return ret;
        }
        List<Row> saveList = new ArrayList<>();
        List<Row> updateList = new ArrayList<>();
        List<T> _saveList = new ArrayList<>();
        List<T> _updateList = new ArrayList<>();
        BiConsumer<T, Row> success = computeReturn ? (b, r) -> {
            updateList.add(r);
            _updateList.add(b);
        } : (b, r) -> updateList.add(r);
        BiConsumer<T, Row> fail = computeReturn ? (b, r) -> {
            saveList.add(r);
            _saveList.add(b);
        } : (b, r) -> saveList.add(r);
        Collection<?> finalIdsInDb = idsInDb;
        ListUtil.batchCollection(list, batchSize)
                .forEach(_list -> BeanUtil.toRows(
                        _list,
                        row -> finalIdsInDb.contains(row.get(idColName)),
                        success,
                        fail
                ));
        if (!updateList.isEmpty()) {
            ret.setUpdateList(_updateList);
            Xdb.table(tableName)
                    .update()
                    .id(idColName)
                    .rows(updateList)
                    .ignoreNulls(ignoreNulls)
                    .batchSize(batchSize)
                    .setRowsFromBeans(true)
                    .execute();
        }
        if (!saveList.isEmpty()) {
            ret.setSaveList(_saveList);
            Xdb.table(tableName).save().rows(saveList).batchSize(batchSize).setRowsFromBeans(true).execute();
        }
        return ret;
    }

    @Override
    public SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Integer batchSize,
                                                             boolean ignoreNulls
    ) {
        return saveOrUpdateThenReturn(list, null, batchSize, ignoreNulls, true);
    }

    @Override
    public void saveOrUpdate(Collection<T> list, Integer batchSize, boolean ignoreNulls) {
        saveOrUpdateThenReturn(list, null, batchSize, ignoreNulls, false);
    }

    @Override
    public void update(T t) {
        if (t instanceof Collection) {
            //noinspection unchecked
            update((Collection<T>) t, 1000, true);
            return;
        }
        update(t, true);
    }

    @Override
    public <ID> void update(T t, boolean ignoreNulls, ID idForUpdate) {
        if (t == null)
            return;
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(this.datasource);
        Xdb.table(tableName).update().id(idColName).rowBean(t, idForUpdate).ignoreNulls(ignoreNulls).execute();
    }

    @Override
    public void update(T t, boolean ignoreNulls) {
        if (t == null)
            return;
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(this.datasource);
        Xdb.table(tableName).update().id(idColName).rowBean(t).ignoreNulls(ignoreNulls).execute();
    }

    @Override
    public void update(Collection<T> list, Integer batchSize, boolean ignoreNulls) {
        if (list == null || list.isEmpty())
            return;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(this.datasource);
        List<Row> updateList = new ArrayList<>();
        ListUtil.batchCollection(list, batchSize).forEach(_list -> updateList.addAll(BeanUtil.toRows(_list)));

        Xdb.table(tableName)
                .update()
                .id(idColName)
                .rows(updateList)
                .ignoreNulls(ignoreNulls)
                .batchSize(batchSize)
                .execute();
    }

    @Override
    public void update(Collection<T> list, Integer batchSize) {
        update(list, batchSize, true);
    }

    @Override
    public void delete(T t) {
        if (t == null)
            return;
        if (t instanceof Collection) {
            //noinspection unchecked
            delete((Collection<T>) t, 1000);
            return;
        }
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(this.datasource);
        Xdb.table(tableName).delete().id(idColName).rowBean(t).execute();
    }

    @Override
    public void delete(Collection<T> list, Integer batchSize) {
        if (list == null || list.isEmpty())
            return;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(this.datasource);
        Xdb.table(tableName).delete().id(idColName).rowsBean(list).batchSize(batchSize).execute();
    }

    @Override
    public <ID> void deleteById(ID id) {
        if (id == null)
            return;
        if (id instanceof Collection) {
            //noinspection unchecked
            deleteById((Collection<Object>) id, 1000);
            return;
        }
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        Row row = new Row().set(idColName, id);
        datasource(this.datasource);
        Xdb.table(tableName).delete().id(idColName).row(row).execute();
    }

    @Override
    public <ID> void deleteById(Collection<ID> ids, Integer batchSize) {
        if (ids == null || ids.isEmpty())
            return;
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        String sql = "delete from " + tableName + " where " + idColName + " = ? ";
        datasource(this.datasource);
        ConnTool.executeBatch(
                sql,
                ids.stream().map(id -> new Object[]{id}).collect(Collectors.toList()),
                batchSize,
                XdbConfig.isAutoCommit(),
                XdbConfig.isAutoClose()
        );
    }

    @Override
    public <ID> boolean existId(ID id) {
        return !notExistId(id);
    }

    @Override
    public boolean exist(T t) {
        return !notExist(t);
    }

    @Override
    public boolean exist(Wheres wheres) {
        return count(wheres) > 0;
    }

    @Override
    public boolean exist(WheresBean<T> wheres) {
        return count(wheres) > 0;
    }

    @Override
    public <ID> boolean notExistId(ID id) {
        if (id == null)
            return false;
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        String sql = "select count(1) from " + tableName + " where " + idColName + " = :ID ";
        datasource(this.datasource);
        Long count = Xdb.sql(sql).sqlArg("ID", id).executeQuery().firstRowFirstCol(Long.class);
        return count == null || count == 0;
    }

    @Override
    public boolean notExist(T t) {
        Row row = BeanUtil.toRow(t);
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        Object id = row.get(idColName);
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String sql = "select count(1) from " + tableName + " where " + idColName + " = :ID ";
        datasource(this.datasource);
        Long count = Xdb.sql(sql).sqlArg("ID", id).executeQuery().firstRowFirstCol(Long.class);
        return count == null || count == 0;
    }

    @Override
    public <ID> T getById(ID id) {
        if (id == null)
            return null;
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.table(tableName).info().id(idColName, id).execute(getBeanClass());
    }

    @NonNull
    @Override
    public <ID> List<T> getByIds(List<ID> ids) {
        return getByIds(ids, 500);
    }

    @NonNull
    @Override
    public <ID> List<T> getByIds(List<ID> ids, int batchSize) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.table(tableName).list().ids(idColName, ids).batchSize(batchSize).execute(getBeanClass());
    }

    @Override
    public @NonNull <ID> List<T> getByIds(List<ID> ids, List<T> list) {
        list.addAll(getByIds(ids));
        return list;
    }

    @Override
    public @NonNull <ID> List<T> getByIds(List<ID> ids, int batchSize, List<T> list) {
        list.addAll(getByIds(ids, batchSize));
        return list;
    }

    @Override
    public @NonNull <ID> Set<T> getByIds(List<ID> ids, Set<T> set) {
        set.addAll(getByIds(ids));
        return set;
    }

    @Override
    public @NonNull <ID> Set<T> getByIds(List<ID> ids, int batchSize, Set<T> set) {
        set.addAll(getByIds(ids, batchSize));
        return set;
    }

    @Override
    public @NonNull <ID> Map<ID, T> infos(List<ID> ids, int batchSize) {
        List<T> pos = getByIds(ids, batchSize);
        @NonNull Map<ID, T> map = new HashMap<>(pos.size());
        for (int i = 0, len = ids.size(); i < len; i++) {
            T value;
            ID id = ids.get(i);
            try {
                value = pos.get(i);
            } catch (Exception e) {
                log.error("getByIds error: id[{}]", id);
                throw e;
            }
            map.put(id, value);
        }
        return map;
    }

    @Override
    public @NonNull <ID> Map<ID, T> infos(List<ID> ids) {
        return infos(ids, 500);
    }

    @Override
    public <ID> Optional<T> getByIdOpt(ID id) {
        return Optional.ofNullable(getById(id));
    }

    @Override
    public <ID> @NonNull List<T> getById(Collection<ID> ids, Integer batchSize) {
        if (ids == null || ids.isEmpty())
            return new ArrayList<>();
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.table(tableName).list().ids(idColName, ids).batchSize(batchSize).execute(getBeanClass());
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sql("select " + idColName + "  from " + tableName).executeQuery().resultFirstColumn(idType);
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType, Wheres wheres) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        MapUtil<?> args = wheres.getArgs();
        if (args != null)
            return Xdb.sql("select " + idColName + "  from " + tableName + " " + wheres.getWhereSql())
                    .sqlArgs(args)
                    .executeQuery()
                    .resultFirstColumn(idType);
        return Xdb.sql("select " + idColName + "  from " + tableName + " " + wheres.getWhereSql())
                .executeQuery()
                .resultFirstColumn(idType);
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType, WheresBean<T> wheres) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        String idColName = OrmAnnoUtil.getIdColNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        MapUtil<?> args = wheres.getArgs();
        if (args != null)
            return Xdb.sql("select " + idColName + "  from " + tableName + " " + wheres.getWhereSql())
                    .sqlArgs(args)
                    .executeQuery()
                    .resultFirstColumn(idType);
        return Xdb.sql("select " + idColName + "  from " + tableName + " " + wheres.getWhereSql())
                .executeQuery()
                .resultFirstColumn(idType);
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType, List<ID> list) {
        list.addAll(ids(idType));
        return list;
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType, List<ID> list, Wheres wheres) {
        list.addAll(ids(idType, wheres));
        return list;
    }

    @Override
    public @NonNull <ID> List<ID> ids(Class<ID> idType, List<ID> list, WheresBean<T> wheres) {
        list.addAll(ids(idType, wheres));
        return list;
    }

    @Override
    public @NonNull <ID> Set<ID> ids(Class<ID> idType, Set<ID> set) {
        set.addAll(ids(idType));
        return set;
    }

    @Override
    public @NonNull <ID> Set<ID> ids(Class<ID> idType, Set<ID> set, Wheres wheres) {
        set.addAll(ids(idType, wheres));
        return set;
    }

    @Override
    public @NonNull <ID> Set<ID> ids(Class<ID> idType, Set<ID> set, WheresBean<T> wheres) {
        set.addAll(ids(idType, wheres));
        return set;
    }

    @Override
    public SqlTool sql(String sql) {
        datasource(this.datasource);
        return Xdb.sql(sql);
    }

    @Override
    public SqlPageTool sqlPage() {
        datasource(this.datasource);
        return Xdb.sqlPage();
    }

    @Override
    public @NonNull List<T> list(String whereSql) {
        return asList(whereSql, getBeanClass());
    }

    @Override
    public @NonNull <R> List<R> asList(String whereSql, Class<R> rClass) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .executeQuery()
                .resultBean(rClass);
    }

    private @NonNull List<T> list(String whereSql, List<String> columns) {
        return asList(whereSql, columns, getBeanClass());
    }

    private @NonNull <R> List<R> asList(String whereSql, List<String> columns, Class<R> rClass) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        if (columns == null || columns.isEmpty()) {
            return Xdb.sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .executeQuery()
                    .resultBean(rClass);
        } else {
            String _columns = String.join(" , ", columns);
            return Xdb.sql("select  " +
                    _columns +
                    "  from " +
                    tableName +
                    " " +
                    Optional.ofNullable(whereSql).orElse("")).executeQuery().resultBean(rClass);
        }
    }

    @Override
    public @NonNull List<T> listAll() {
        return asListAll(getBeanClass());
    }

    @Override
    public @NonNull <R> List<R> asListAll(Class<R> rClass) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sql("select *  from " + tableName).executeQuery().resultBean(rClass);
    }

    @Override
    public @NonNull List<T> list(Wheres wheres) {
        return asList(wheres, getBeanClass());
    }

    @Override
    public @NonNull <R> List<R> asList(Wheres wheres, Class<R> rClass) {
        MapUtil<?> args = wheres.getArgs();
        if (args == null)
            return asList(wheres.getWhereSql(), rClass);
        return asList(wheres.getWhereSql(), args, rClass);
    }

    @Override
    public @NonNull List<T> list(WheresBean<T> wheres) {
        return asList(wheres, getBeanClass());
    }

    @Override
    public @NonNull <R> List<R> asList(WheresBean<T> wheres, Class<R> rClass) {
        MapUtil<?> args = wheres.getArgs();
        List<SerializableFunction<T, ?>> selectList = wheres.getSelectList();
        if (selectList == null || selectList.isEmpty()) {
            if (args == null)
                return asList(wheres.getWhereSql(), rClass);
            return asList(wheres.getWhereSql(), args, rClass);
        } else {
            List<String> columns = selectList.stream()
                    .map(getter -> OrmAnnoUtil.getColNameByGetter(getBeanClass(), getter))
                    .collect(Collectors.toList());
            if (args == null)
                return asList(wheres.getWhereSql(), columns, rClass);
            return asList(wheres.getWhereSql(), args, columns, rClass);
        }
    }

    @Override
    public @NonNull List<T> list(Consumer<WheresBean<T>> consumer) {
        WheresBean<T> wheresBean = WheresBean.init(getBeanClass());
        consumer.accept(wheresBean);
        return list(wheresBean);
    }

    @Override
    public @NonNull List<T> list(String whereSql, MapUtil<?> args) {
        return asList(whereSql, args, getBeanClass());
    }

    @Override
    public @NonNull <R> List<R> asList(String whereSql, MapUtil<?> args, Class<R> rClass) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .sqlArgs(args)
                .executeQuery()
                .resultBean(rClass);
    }

    private @NonNull <R> List<R> asList(String whereSql, MapUtil<?> args, List<String> columns, Class<R> rClass) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        if (columns == null || columns.isEmpty()) {
            return Xdb.sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .sqlArgs(args)
                    .executeQuery()
                    .resultBean(rClass);
        } else {
            String _columns = String.join(" , ", columns);
            return Xdb.sql("select " + _columns + " from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .sqlArgs(args)
                    .executeQuery()
                    .resultBean(rClass);
        }
    }

    @Override
    public @NonNull PageResult<T> page(String whereSql, MapUtil<?> args, Number pageIndex, Number pageSize) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sqlPage()
                .sqlSelect("select * ")
                .sqlMain(" from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .sqlArgs(args)
                .pageIndex(pageIndex)
                .pagePerSize(pageSize)
                .resultBean(getBeanClass());
    }

    /**
     * 分页查询
     *
     * @param whereSql  where部分的sql
     * @param pageIndex 序号从1起始
     * @param pageSize  分页大小
     * @return {@link PageResult }<{@link T }>
     */
    @Override
    public @NonNull PageResult<T> page(String whereSql, Number pageIndex, Number pageSize) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sqlPage()
                .sqlSelect("select * ")
                .sqlMain(" from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .pageIndex(pageIndex)
                .pagePerSize(pageSize)
                .resultBean(getBeanClass());
    }

    @Override
    public @NonNull PageResult<T> page(Wheres wheres) {
        MapUtil<?> args = wheres.getArgs();
        if (args == null)
            return page(wheres.getWhereSql(), wheres.getPageIndex(), wheres.getPageSize());
        return page(wheres.getWhereSql(), args, wheres.getPageIndex(), wheres.getPageSize());
    }

    @Override
    public @NonNull PageResult<T> page(WheresBean<T> wheres) {
        MapUtil<?> args = wheres.getArgs();
        if (args == null)
            return page(wheres.getWhereSql(), wheres.getPageIndex(), wheres.getPageSize());
        return page(wheres.getWhereSql(), args, wheres.getPageIndex(), wheres.getPageSize());
    }

}
