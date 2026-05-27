package org.xht.xdb.orm.listener;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class ListenerConfig<T> {
    private final List<EntityListener<T>> onSaveList = new ArrayList<>();
    private final List<EntityListener<T>> onUpdateList = new ArrayList<>();
    private final List<EntityListener<T>> onSaveOrUpdateList = new ArrayList<>();
    private final List<EntityListener<T>> onDeleteList = new ArrayList<>();
    private final List<EntityListener<Object>> onDeleteByIdList = new ArrayList<>();
    private final List<EntityListener<Collection<T>>> onBatchSaveList = new ArrayList<>();
    private final List<EntityListener<Collection<T>>> onBatchUpdateList = new ArrayList<>();
    private final List<EntityListener<Collection<T>>> onBatchSaveOrUpdateList = new ArrayList<>();
    private final List<EntityListener<Collection<T>>> onBatchDeleteList = new ArrayList<>();
    private final List<EntityListener<Collection<Object>>> onBatchDeleteByIdList = new ArrayList<>();

    public static <T> ListenerConfig<T> of(Class<T> entityClass) {
        return new ListenerConfig<>();
    }

    public ListenerConfig<T> onSave(boolean sync, Consumer<T> consumer) {
        onSaveList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onUpdate(boolean sync, Consumer<T> consumer) {
        onUpdateList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onSaveOrUpdate(boolean sync, Consumer<T> consumer) {
        onSaveOrUpdateList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onDelete(boolean sync, Consumer<T> consumer) {
        onDeleteList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onDeleteById(boolean sync, Consumer<Object> consumer) {
        onDeleteByIdList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onBatchSave(boolean sync, Consumer<Collection<T>> consumer) {
        onBatchSaveList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onBatchUpdate(boolean sync, Consumer<Collection<T>> consumer) {
        onBatchUpdateList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onBatchSaveOrUpdate(boolean sync, Consumer<Collection<T>> consumer) {
        onBatchSaveOrUpdateList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onBatchDelete(boolean sync, Consumer<Collection<T>> consumer) {
        onBatchDeleteList.add(new EntityListener<>(sync, consumer));
        return this;
    }

    public ListenerConfig<T> onBatchDeleteById(boolean sync, Consumer<Collection<Object>> consumer) {
        onBatchDeleteByIdList.add(new EntityListener<>(sync, consumer));
        return this;
    }

}
