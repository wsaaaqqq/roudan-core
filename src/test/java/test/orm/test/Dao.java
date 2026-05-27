package test.orm.test;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.listener.EntityServiceWithListenerImp;
import org.xht.xdb.orm.listener.ListenerConfig;
import test.orm.po.PoJPA;

import java.util.function.Consumer;

@Slf4j
public class Dao extends EntityServiceWithListenerImp<PoJPA> {


    @Override
    public Class<PoJPA> beanClass() {
        return PoJPA.class;
    }

    @Override
    public String datasource() {
        return "h2";
    }

    @Override
    public Consumer<ListenerConfig<PoJPA>> listenerConfig() {
        return c -> c
                .onSave(true, t -> log.info("onSave：{}", t))
                .onSave(false, t -> log.info("onSave2：{}", t))
                .onDelete(false, t -> log.info("onDelete：{}", t))
                .onDeleteById(false, t -> log.info("onDeleteById：{}", t))
                .onUpdate(false, t -> log.info("onUpdate：{}", t))
                .onSaveOrUpdate(false, t -> log.info("onSaveOrUpdate：{}", t))
                .onBatchSave(true, t -> log.info("onBatchSave[{}]：{}", t.size(), t))
                .onBatchSaveOrUpdate(true, t -> log.info("onBatchSaveOrUpdate[{}]：{}", t.size(), t))
                .onBatchUpdate(true, t -> log.info("onBatchUpdate[{}]：{}", t.size(), t))
                .onBatchDelete(true, t -> log.info("onBatchDelete[{}]：{}", t.size(), t))
                .onBatchDeleteById(true, t -> log.info("onBatchDeleteById[{}]：{}", t.size(), t));
    }

}
