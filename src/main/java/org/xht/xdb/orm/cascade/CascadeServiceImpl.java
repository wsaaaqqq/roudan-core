package org.xht.xdb.orm.cascade;

import java.util.Collection;
import java.util.Collections;

/**
 * 级联保存工具类
 * 特性：
 * 1. 使用迭代代替递归，无深度限制
 * 2. 支持批量保存同类型实体
 * 3. 自动处理循环引用
 * 4. 保存策略可切换
 *
 * @version 4.0
 */
public class CascadeServiceImpl implements CascadeService {

    private final BaseCascadeServiceImpl baseCascadeService = new BaseCascadeServiceImpl();

    @Override
    public CascadeService beanSaveOrders(Class<?>... beanSaveOrders) {
        baseCascadeService.beanSaveOrders(beanSaveOrders);
        return this;
    }

    @Override
    public <T> void save(T entity) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeSave())
                          .process(Collections.singletonList(entity), 1, baseCascadeService.DB_FUNC_SAVE_OR_UPDATE);
    }

    @Override
    public <T> void saveAll(Collection<T> entities, int batchSize) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeSave())
                          .process(entities, batchSize, baseCascadeService.DB_FUNC_SAVE_OR_UPDATE);
    }

    @Override
    public <T> void update(T entity, boolean ignoreNulls) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeUpdate())
                          .ignoreNulls(ignoreNulls)
                          .process(Collections.singletonList(entity), 1, baseCascadeService.DB_FUNC_SAVE_OR_UPDATE);
    }

    @Override
    public <T> void updateAll(Collection<T> entities, int batchSize, boolean ignoreNulls) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeUpdate())
                          .ignoreNulls(ignoreNulls)
                          .process(entities, batchSize, baseCascadeService.DB_FUNC_SAVE_OR_UPDATE);
    }

    @Override
    public <T> void delete(T entity) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeDelete())
                          .process(Collections.singletonList(entity), 1, baseCascadeService.DB_FUNC_DELETE);
    }

    @Override
    public <T> void deleteAll(Collection<T> entities, int batchSize) {
        baseCascadeService.cascadeFunction(a -> a.enabled() && a.cascadeDelete())
                          .process(entities, batchSize, baseCascadeService.DB_FUNC_DELETE);
    }

}

