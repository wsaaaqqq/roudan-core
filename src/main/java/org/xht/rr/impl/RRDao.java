package org.xht.rr.impl;

import org.xht.xdb.Xdb;
import org.xht.xdb.orm.dao.BaseDao;
import org.xht.xdb.orm.dao.BaseDaoImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RRDao {

    public static final RRDao INSTANCE = new RRDao();

    private static final Map<String, BaseDao<?>> cache = new ConcurrentHashMap<>();

    public <T> BaseDao<T> baseDao(Class<T> entityClass) {
        return baseDao(entityClass, Xdb.DEFAULT_DATASOURCE_NAME);
    }

    private <T> String getKey(Class<T> entityClass, String datasourceName) {
        return entityClass.getName() + ":" + datasourceName;
    }

    public <T> BaseDao<T> baseDao(Class<T> entityClass, String datasource) {
        String key = getKey(entityClass, datasource);
        //noinspection unchecked
        return (BaseDao<T>) cache.computeIfAbsent(key, k -> new BaseDaoImpl<>(entityClass, datasource));
    }

    public <T, E extends BaseDao<T>> E of(Class<E> daoClass, String datasource) {
        String key = getKey(daoClass, datasource);
        //noinspection unchecked
        return (E) cache.computeIfAbsent(key, k -> BaseDaoImpl.createProxy(daoClass, datasource));
    }

    public <T, E extends BaseDao<T>> E of(Class<E> daoClass) {
        return of(daoClass, Xdb.DEFAULT_DATASOURCE_NAME);
    }
}
