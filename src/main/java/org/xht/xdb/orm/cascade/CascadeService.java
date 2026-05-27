package org.xht.xdb.orm.cascade;

import java.util.Collection;

public interface CascadeService {
    CascadeService beanSaveOrders(Class<?>... beanOrders);

    <T> void save(T entity);

    <T> void saveAll(Collection<T> entities, int batchSize);

    <T> void update(T entity, boolean ignoreNulls);

    <T> void updateAll(Collection<T> entities, int batchSize, boolean ignoreNulls);

    <T> void delete(T entity);

    <T> void deleteAll(Collection<T> entities, int batchSize);

}
