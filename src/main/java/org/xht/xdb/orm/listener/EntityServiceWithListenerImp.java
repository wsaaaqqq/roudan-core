package org.xht.xdb.orm.listener;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.orm.EntityServiceImp;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.util.BeanUtil;
import org.xht.xdb.vo.Row;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
@Slf4j
public abstract class EntityServiceWithListenerImp<T> extends EntityServiceImp<T> {
    private final ListenerConfig<T> listenerConfig;

    public EntityServiceWithListenerImp() {
        super(null, null);
        Class<T> beanClass = beanClass();
        super.setDatasource(datasource());
        super.setBeanClass(beanClass);
        this.listenerConfig = ListenerConfig.of(beanClass);
        listenerConfig().accept(this.listenerConfig);
    }

    public abstract Class<T> beanClass();

    public abstract String datasource();

    public abstract Consumer<ListenerConfig<T>> listenerConfig();

    public EntityServiceWithListenerImp(Class<T> beanClass, String datasource,
            Consumer<ListenerConfig<T>> listenerConfig
    ) {
        super(beanClass, datasource);
        this.listenerConfig = ListenerConfig.of(beanClass);
        listenerConfig.accept(this.listenerConfig);
    }

    private final ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void save(T t) {
        super.save(t);
        doListener(t, listenerConfig.getOnSaveList());
    }

    private void doListener(T t, List<EntityListener<T>> listenerList) {
        if (listenerList != null && !listenerList.isEmpty()) {
            listenerList.forEach(listener -> {
                if (listener.isSync()) {
                    listener.getListener().accept(t);
                } else {
                    es.submit(() -> listener.getListener().accept(t));
                }
            });
        }
    }

    private void doListenerBatch(Collection<T> entities, List<EntityListener<Collection<T>>> listeners) {
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(listener -> {
                if (listener.isSync()) {
                    listener.getListener().accept(entities);
                } else {
                    es.submit(() -> listener.getListener().accept(entities));
                }
            });
        }
    }


    @Override
    public void save(Collection<T> list, Integer batchSize) {
        super.save(list, batchSize);
        doListenerBatch(list, listenerConfig.getOnBatchSaveList());
    }

    @Override
    public void saveOrUpdate(Collection<T> list, Integer batchSize, boolean ignoreNulls) {
        if (list == null || list.isEmpty())
            return;
        T t = list.stream().findFirst().get();
        String tableName = OrmAnnoUtil.getTableName(t);
        String idColName = OrmAnnoUtil.getIdColName(t);
        datasource(super.getDatasource());
        Set<Object> idsInDb = new HashSet<>(Xdb
                                                    .sql("select " + idColName + " from " + tableName)
                                                    .executeQuery()
                                                    .resultFirstColumn(Object.class));
        if (idsInDb.isEmpty()) {
            save(list, batchSize);
            return;
        }
        Map<T, Row> mapBeanRow = BeanUtil.toMapBeanRow(list);
        List<Row> saveList = new ArrayList<>();
        List<T> saveListBean = new ArrayList<>();
        List<Row> updateList = new ArrayList<>();
        List<T> updateListBean = new ArrayList<>();
        mapBeanRow.forEach((bean, row) -> {
            if (idsInDb.contains(row.get(idColName))) {
                updateList.add(row);
                updateListBean.add(bean);
            } else {
                saveList.add(row);
                saveListBean.add(bean);
            }
        });
        if (!updateList.isEmpty()) {
            Xdb
                    .table(tableName)
                    .update()
                    .id(idColName)
                    .rows(updateList)
                    .ignoreNulls(ignoreNulls)
                    .batchSize(batchSize)
                    .execute()
            ;
            doListenerBatch(updateListBean, listenerConfig.getOnBatchUpdateList());
        }
        if (!saveList.isEmpty()) {
            Xdb.table(tableName).save().rows(saveList).batchSize(batchSize).execute();
            doListenerBatch(saveListBean, listenerConfig.getOnBatchSaveList());
        }
    }

    @Override
    public void update(T t) {
        super.update(t);
        doListener(t, listenerConfig.getOnUpdateList());
    }

    @Override
    public void update(T t, boolean ignoreNulls) {
        super.update(t, ignoreNulls);
        doListener(t, listenerConfig.getOnUpdateList());
    }

    @Override
    public <ID> void update(T t, boolean ignoreNulls, ID idForUpdate) {
        super.update(t, ignoreNulls, idForUpdate);
        doListener(t, listenerConfig.getOnUpdateList());
    }

    @Override
    public void update(Collection<T> list, Integer batchSize, boolean ignoreNulls) {
        super.update(list, batchSize, ignoreNulls);
        doListenerBatch(list, listenerConfig.getOnBatchUpdateList());
    }

    @Override
    public void update(Collection<T> list, Integer batchSize) {
        super.update(list, batchSize);
        doListenerBatch(list, listenerConfig.getOnBatchUpdateList());
    }

    @Override
    public void delete(T t) {
        super.delete(t);
        doListener(t, listenerConfig.getOnDeleteList());
    }

    @Override
    public void delete(Collection<T> list, Integer batchSize) {
        super.delete(list, batchSize);
        doListenerBatch(list, listenerConfig.getOnBatchDeleteList());
    }

    @Override
    public <ID> void deleteById(ID id) {
        super.deleteById(id);
        List<EntityListener<Object>> listeners = listenerConfig.getOnDeleteByIdList();
        listeners.forEach(listener -> {
            Consumer<Object> listener1 = listener.getListener();
            if (listener.isSync()) {
                listener1.accept(id);
            } else {
                es.submit(() -> listener1.accept(id));
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <id> void deleteById(Collection<id> ids, Integer batchSize) {
        super.deleteById(ids, batchSize);
        List<EntityListener<Collection<Object>>> listeners = listenerConfig.getOnBatchDeleteByIdList();
        listeners.forEach(listener -> {
            if (listener.isSync()) {
                listener.getListener().accept((Collection<Object>) ids);
            } else {
                es.submit(() -> listener.getListener().accept((Collection<Object>) ids));
            }
        });
    }
}
