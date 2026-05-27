package org.xht.xdb.orm;

import cn.hutool.core.util.ReflectUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.sql.ResultQueryBatch;
import org.xht.xdb.sql.SqlPageTool;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.util.ListUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.util.SerializableFunction;
import org.xht.xdb.vo.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class EntityKeysServiceImp<T> implements EntityKeysService<T> {

    private Class<T> beanClass;
    private String datasource;

    public EntityKeysServiceImp(Class<T> beanClass, String datasource) {
        this.beanClass = beanClass;
        this.datasource = datasource;
    }

    @Override
    public EntityKeysService<T> datasource(String datasource) {
        if (datasource != null && !datasource.isEmpty()) {
            Xdb.selectDataSourceByName(datasource);
        }
        return this;
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
    public SaveOrUpdateBatchResult<T> saveOrUpdateThenReturn(Collection<T> list, Collection<Row> idsInDb,
            Integer batchSize, boolean ignoreNulls, boolean computeReturn
    ) {
        SaveOrUpdateBatchResult<T> ret = new SaveOrUpdateBatchResult<>();
        if (list == null || list.isEmpty())
            return ret;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        Set<String> idColNames = OrmAnnoUtil.getIdColNamesByBeanClass(beanClass);
        String idsJoin = idColNames
                .stream()
                .map(idColName -> idColName + " as \"" + idColName + "\"")
                .collect(Collectors.joining(", "));
        datasource(this.datasource);
        if (idsInDb == null) {
            idsInDb =
                    new HashSet<>(Xdb.sql("select " + idsJoin + " from " + tableName).executeQuery().resultRowOrigin());
        }
        if (idsInDb.isEmpty()) {
            save(list, batchSize);
            ret.setSaveList(list);
            return ret;
        }
        List<T> _saveList = new ArrayList<>();
        List<T> _updateList = new ArrayList<>();
        BiConsumer<T, Row> success = (b, r) -> _updateList.add(b);
        BiConsumer<T, Row> fail = (b, r) -> _saveList.add(b);
        //根据idColNames 创建一个comparator
        Comparator<? super Row> comparator = getComparator(idColNames);
        TreeSet<Row> finalIdsInDb = new TreeSet<>(comparator);
        finalIdsInDb.addAll(idsInDb);
        ListUtil
                .batchCollection(list, batchSize)
                .forEach(_list -> BeanUtil.toRows(_list, finalIdsInDb::contains, success, fail));
        if (!_updateList.isEmpty()) {
            update(_updateList, batchSize, ignoreNulls);
            ret.setUpdateList(_updateList);
        }
        if (!_saveList.isEmpty()) {
            save(_saveList, batchSize);
            ret.setSaveList(_saveList);
        }
        return ret;
    }

    public Comparator<? super Row> getComparator(Set<String> idColNames) {
        return (o1, o2) -> {
            for (String idColName : idColNames) {
                Object v1 = o1.get(idColName);
                Object v2 = o2.get(idColName);

                if (v1 == null && v2 == null) {
                    continue; // Both are null, compare next id column
                }

                if (v1 == null) {
                    return -1; // null values first
                }

                if (v2 == null) {
                    return 1; // null values first
                }

                // Both are not null, compare values
                if (v1 instanceof Comparable && v2 instanceof Comparable) {
                    @SuppressWarnings("unchecked") int result = ((Comparable<Object>) v1).compareTo(v2);
                    if (result != 0) {
                        return result;
                    }
                    // 如果相等，继续比较下一个id列
                } else {
                    // For non-comparable objects, use equals and hashCode
                    if (!v1.equals(v2)) {
                        // Not equal, compare hash codes to establish consistent ordering
                        return Integer.compare(v1.hashCode(), v2.hashCode());
                    }
                    // 如果相等，继续比较下一个id列
                }
            }
            return 0; // All id columns are equal
        };
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
        ListUtil
                .batchCollection(list, batchSize)
                .forEach(_list -> Xdb.table(tableName).save().rowsBean(_list).batchSize(batchSize).execute());
    }

    @Override
    public void update(T t) {
        if (t instanceof Collection) {
            //noinspection unchecked
            update((Collection<T>) t, 500, true);
            return;
        }
        update(t, true);
    }

    @Override
    public void update(T t, boolean ignoreNulls, T keysForUpdate) {
        T poInDB = getByKeys(keysForUpdate);
        if (poInDB == null) {
            save(t);
        } else {
            BeanUtil.copyProperties(t, poInDB, ignoreNulls);
            delete(poInDB);
            save(poInDB);
        }
    }

    @Override
    public void update(T t, boolean ignoreNulls) {
        update(t, ignoreNulls, t);
    }

    @Override
    public void update(Collection<T> list, Integer batchSize) {
        update(list, batchSize, true);
    }

    @Override
    public void update(Collection<T> list, Integer batchSize, boolean ignoreNulls) {
        ListUtil.batchAndConsume(
                list, batchSize, _list -> {
                    List<T> posInDB = getByKeys(_list, batchSize);
                    if (posInDB.size() != _list.size()) {
                        _list.forEach(t -> update(t, ignoreNulls));
                    } else {
                        int i = 0;
                        for (T t : _list) {
                            T poInDB = posInDB.get(i++);
                            if (poInDB != null) {
                                BeanUtil.copyProperties(t, poInDB, ignoreNulls);
                            }
                        }
                        deleteByKeys(posInDB, batchSize);
                        save(posInDB, batchSize);
                    }
                }
        );
    }

    @Override
    public void delete(T t) {
        if (t == null)
            return;
        if (t instanceof Collection) {
            //noinspection unchecked
            deleteByKeys((Collection<T>) t, 500);
            return;
        }
        deleteByKeys(t);
    }

    @Override
    public void delete(Collection<T> list, Integer batchSize) {
        deleteByKeys(list, batchSize);
    }

    @Override
    public void deleteByKeys(T t) {
        Collection<T> list = new ArrayList<>();
        list.add(t);
        deleteByKeys(list, 1);
    }

    @Override
    public void deleteByKeys(Collection<T> beans, Integer batchSize) {
        if (beans == null || beans.isEmpty())
            return;
        T t = beans.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        Set<Field> fields = OrmAnnoUtil.getIdFieldsByBeanClass(beanClass);
        String where = fields.stream().map(f -> {
            String colName = OrmAnnoUtil.getColName(t, f.getName());
            return String.format("%s = ?", colName);
        }).collect(Collectors.joining(" and "));
        String sql = String.format("delete from %s where %s", tableName, where);
        List<Object[]> list = new ArrayList<>();
        for (T bean : beans) {
            Object[] values = fields.stream().map(f -> {
                try {
                    return f.get(bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).toArray();
            list.add(values);
        }
        Xdb.datasource(datasource).sql(sql).executeBatch(list, batchSize);
    }


    @Override
    public boolean exist(T t) {
        T byKeys = getByKeys(t);
        return byKeys != null;
    }

    @Override
    public boolean notExist(T t) {
        return !exist(t);
    }

    @Override
    public @NonNull List<T> getByKeys(Collection<T> ids) {
        return getByKeys(ids, 500);
    }

    @Override
    public @NonNull List<T> getByKeys(Collection<T> ids, int batchSize) {
        List<T> list = new ArrayList<>();
        if (ids == null || ids.isEmpty())
            return list;
        T t = ids.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        Set<Field> fields = OrmAnnoUtil.getIdFieldsByBeanClass(beanClass);
        Map<Field, String> fieldColName = new HashMap<>();
        fields.forEach(f -> {
            String colName = OrmAnnoUtil.getColName(t, f.getName());
            fieldColName.put(f, colName);
        });
        String sql = String.format("select * from %s where ", tableName);
        ListUtil.batchAndConsume(
                ids, batchSize, _ids -> {
                    if (_ids == null || _ids.isEmpty())
                        return;
                    MapUtil<Object> args = MapUtil.init();
                    int i = 0;
                    StringBuilder where = new StringBuilder();
                    for (T id : _ids) {
                        int finalI = i;
                        String oneRow = fields.stream().map(f -> {
                            String col = fieldColName.get(f);
                            String colN = col + finalI;
                            args.add(colN, ReflectUtil.getFieldValue(id, f));
                            return col + " = :" + col + finalI;
                        }).collect(Collectors.joining(" and "));
                        where.append(oneRow).append(" or ");
                        i++;
                    }
                    List<T> batch = Xdb
                            .datasource(datasource)
                            .sql(sql + where.substring(0, where.length() - 4))
                            .sqlArgs(args)
                            .executeQuery()
                            .resultBean(beanClass);
                    list.addAll(batch);
                }
        );
        return list;
    }

    @Override
    public Optional<T> getByKeysOpt(T id) {
        return Optional.ofNullable(getByKeys(id));
    }

    @Override
    public T getByKeys(T id) {
        String tableName = OrmAnnoUtil.getTableName(id);
        Set<Field> fields = OrmAnnoUtil.getIdFieldsByBeanClass(beanClass);
        Map<Field, String> fieldColName = new HashMap<>();
        fields.forEach(f -> {
            String colName = OrmAnnoUtil.getColName(id, f.getName());
            fieldColName.put(f, colName);
        });
        String sql = String.format("select * from %s where ", tableName);
        MapUtil<Object> args = MapUtil.init();
        String oneRow = fields.stream().map(f -> {
            String col = fieldColName.get(f);
            args.add(col, ReflectUtil.getFieldValue(id, f));
            return col + " = :" + col;
        }).collect(Collectors.joining(" and "));
        List<T> batch = Xdb.datasource(datasource).sql(sql + oneRow).sqlArgs(args).executeQuery().resultBean(beanClass);
        if (batch == null || batch.isEmpty())
            return null;
        return batch.get(0);
    }

    @Override
    public @NonNull Map<T, T> infos(List<T> ids, int batchSize) {
        List<T> pos = getByKeys(ids, batchSize);
        @NonNull Map<T, T> map = new HashMap<>(pos.size());
        for (int i = 0, len = ids.size(); i < len; i++) {
            T value;
            T id = ids.get(i);
            try {
                value = pos.get(i);
            } catch (Exception e) {
                log.error("getByKeys error: id[{}]", id);
                throw e;
            }
            map.put(id, value);
        }
        return map;
    }

    @Override
    public @NonNull Map<T, T> infos(List<T> ids) {
        return infos(ids, 500);
    }

    @Override
    public @NonNull List<T> keys() {
        String tableName = OrmAnnoUtil.getTableName(beanClass);
        Set<String> idColNames = OrmAnnoUtil.getIdColNamesByBeanClass(beanClass);
        String cols = String.join(",", idColNames);
        String sql = String.format("select %s from %s ", cols, tableName);
        return Xdb.datasource(datasource).sql(sql).executeQuery().resultBean(beanClass);
    }

    @Override
    public @NonNull Set<T> keysSet() {
        String tableName = OrmAnnoUtil.getTableName(beanClass);
        Set<String> idColNames = OrmAnnoUtil.getIdColNamesByBeanClass(beanClass);
        String cols = String.join(",", idColNames);
        String sql = String.format("select %s from %s ", cols, tableName);
        Set<T> keys = new HashSet<>();
        try (ResultQueryBatch resultQueryBatch = Xdb.datasource(datasource).sql(sql).executeQuery().resultBatch(500)) {
            resultQueryBatch.forEachBatchBean(beanClass, keys::addAll);
        }
        return keys;
    }


    @Override
    public @NonNull List<T> listAll() {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb.sql("select *  from " + tableName).executeQuery().resultBean(getBeanClass());
    }

    @Override
    public @NonNull List<T> list(WheresBean<T> wheres) {
        MapUtil<?> args = wheres.getArgs();
        List<SerializableFunction<T, ?>> selectList = wheres.getSelectList();
        if (selectList == null || selectList.isEmpty()) {
            if (args == null)
                return list(wheres.getWhereSql());
            return list(wheres.getWhereSql(), args);
        } else {
            List<String> columns = selectList
                    .stream()
                    .map(getter -> OrmAnnoUtil.getColNameByGetter(getBeanClass(), getter))
                    .collect(Collectors.toList());
            if (args == null)
                return list(wheres.getWhereSql(), columns);
            return list(wheres.getWhereSql(), args, columns);
        }
    }

    private @NonNull List<T> list(String whereSql, List<String> columns) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        if (columns == null || columns.isEmpty()) {
            return Xdb
                    .sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .executeQuery()
                    .resultBean(getBeanClass());
        } else {
            String _columns = String.join(" , ", columns);
            return Xdb
                    .sql("select  " + _columns + "  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .executeQuery()
                    .resultBean(getBeanClass());
        }
    }

    private @NonNull List<T> list(String whereSql, MapUtil<?> args, List<String> columns) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        if (columns == null || columns.isEmpty()) {
            return Xdb
                    .sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .sqlArgs(args)
                    .executeQuery()
                    .resultBean(getBeanClass());
        } else {
            String _columns = String.join(" , ", columns);
            return Xdb
                    .sql("select " + _columns + " from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                    .sqlArgs(args)
                    .executeQuery()
                    .resultBean(getBeanClass());
        }
    }

    @Override
    public @NonNull List<T> list(Wheres wheres) {
        MapUtil<?> args = wheres.getArgs();
        if (args == null)
            return list(wheres.getWhereSql());
        return list(wheres.getWhereSql(), args);
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
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb
                .sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .executeQuery()
                .resultBean(getBeanClass());
    }

    @Override
    public @NonNull List<T> list(String whereSql, MapUtil<?> args) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb
                .sql("select *  from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .sqlArgs(args)
                .executeQuery()
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

    @Override
    public @NonNull PageResult<T> page(String whereSql, Number pageIndex, Number pageSize) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb
                .sqlPage()
                .sqlSelect("select * ")
                .sqlMain(" from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .pageIndex(pageIndex)
                .pagePerSize(pageSize)
                .resultBean(getBeanClass());
    }

    @Override
    public @NonNull PageResult<T> page(String whereSql, MapUtil<?> args, Number pageIndex, Number pageSize) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(getBeanClass());
        datasource(this.datasource);
        return Xdb
                .sqlPage()
                .sqlSelect("select * ")
                .sqlMain(" from " + tableName + " " + Optional.ofNullable(whereSql).orElse(""))
                .sqlArgs(args)
                .pageIndex(pageIndex)
                .pagePerSize(pageSize)
                .resultBean(getBeanClass());
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
}
