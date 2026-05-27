package org.xht.xdb.orm.cascade;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CascadeQueryService {

    // 线程本地变量，用于递归时检测循环引用，避免栈溢出
    private final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(HashSet::new);

    // 不需要级联处理的基础类型白名单
    private static final Set<Class<?>> BASIC_TYPES = new HashSet<>(Arrays.asList(
            String.class,
            Integer.class,
            Long.class,
            Short.class,
            Byte.class,
            Double.class,
            Float.class,
            Boolean.class,
            Character.class,
            int.class,
            long.class,
            short.class,
            byte.class,
            double.class,
            float.class,
            boolean.class,
            char.class
    ));

    // ========== 核心类型标识逻辑 ==========

    /**
     * 生成集合类型的唯一Key，区分不同集合类型+不同元素类型的组合
     */
    private static Object collectionKey(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return new SimpleEntry<>(collectionClass, elementClass);
    }

    // ========== 对外查询API ==========
    @SuppressWarnings("unchecked")
    public <F, T> T query(F from, Class<T> toClass) {
        if (from == null) {
            return null;
        }
        try {
            Class<F> fromClass = (Class<F>) from.getClass();
            Function<F, T> function = (Function<F, T>) key2Map.get(fromClass, toClass);
            if (function == null) {
                throw new RuntimeException("No converter registered for " + fromClass + " to " + toClass);
            }
            T result = function.apply(from);
            return cascadeFill(result);
        } finally {
            processedObjects.remove();
        }
    }

    public <F> List<F> query(List<F> from) {
        if (from == null || from.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return query(from, (Class<F>) from.get(0).getClass());
    }
    public <F, T> List<T> query(List<F> from, Class<T> toClass) {
        if (from == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (F f : from) {
            result.add(query(f, toClass));
        }
        return result;
    }

    public <F> Set<F> query(Set<F> from) {
        if (from == null || from.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return query(from, (Class<F>) from.iterator().next().getClass());
    }

    public <F, T> Set<T> query(Set<F> from, Class<T> toClass) {
        if (from == null) {
            return null;
        }
        Set<T> result = new HashSet<>();
        for (F f : from) {
            result.add(query(f, toClass));
        }
        return result;
    }

    /**
     * 泛化集合查询API，支持任意Collection子类
     */
    @SuppressWarnings({"unchecked", "unused"})
    public <F, T, C extends Collection<T>> C queryCollection(F from, Class<C> collectionClass, Class<T> elementClass) {
        if (from == null) {
            return createEmptyCollection(collectionClass);
        }
        try {
            Class<F> fromClass = (Class<F>) from.getClass();
            Function<F, C> function =
                    (Function<F, C>) key2Map.get(fromClass, collectionKey(collectionClass, elementClass));
            if (function == null) {
                throw new RuntimeException("No collection converter registered for " +
                                                   fromClass +
                                                   " to " +
                                                   collectionClass.getSimpleName() +
                                                   "<" +
                                                   elementClass.getSimpleName() +
                                                   ">");
            }
            C result = function.apply(from);
            result.forEach(this::cascadeFill);
            return result;
        } finally {
            processedObjects.remove();
        }
    }

    // ========== 级联填充逻辑 ==========
    @SuppressWarnings("unchecked")
    private <T> T cascadeFill(T obj) {
        if (obj == null || BASIC_TYPES.contains(obj.getClass()) || processedObjects.get().contains(obj)) {
            return obj;
        }
        processedObjects.get().add(obj);

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                if (fieldValue == null)
                    continue;
                Class<?> fieldType = field.getType();

                // 1. 处理普通类型字段
                if (!BASIC_TYPES.contains(fieldType)) {
                    Function<Object, Object> converter =
                            (Function<Object, Object>) key2Map.get(fieldValue.getClass(), fieldType);
                    if (converter != null) {
                        Object convertedValue = converter.apply(fieldValue);
                        field.set(obj, convertedValue);
                        cascadeFill(convertedValue);
                        continue;
                    }
                }

                // 2. 处理所有Collection类型集合字段（List/Set/Queue等）
                if (Collection.class.isAssignableFrom(fieldType) && fieldValue instanceof Collection) {
                    Collection<?> fieldCollection = (Collection<?>) fieldValue;
                    if (fieldCollection.isEmpty())
                        continue;
                    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                    Class<?> elementType = (Class<?>) genericType.getActualTypeArguments()[0];
                    Class<? extends Collection> collectionClass = (Class<? extends Collection>) fieldType;

                    // 优先匹配注册的集合转换器
                    Function<Object, Collection<?>> collectionConverter = (Function<Object, Collection<?>>) key2Map.get(
                            fieldValue.getClass(),
                            collectionKey(collectionClass, elementType)
                    );
                    if (collectionConverter != null) {
                        Collection<?> convertedCollection = collectionConverter.apply(fieldValue);
                        field.set(obj, convertedCollection);
                        convertedCollection.forEach(this::cascadeFill);
                        continue;
                    }

                    // 没有集合转换器则尝试用单元素转换器批量转换
                    Object firstItem = fieldCollection.iterator().next();
                    Function<Object, Object> elementConverter =
                            (Function<Object, Object>) key2Map.get(firstItem.getClass(), elementType);
                    if (elementConverter != null) {
                        Collection<Object> convertedCollection = createEmptyCollection(collectionClass);
                        for (Object item : fieldCollection) {
                            Object convertedItem = elementConverter.apply(item);
                            convertedCollection.add(convertedItem);
                            cascadeFill(convertedItem);
                        }
                        field.set(obj, convertedCollection);
                    }
                }

                // 无转换规则则递归处理原有字段值
                cascadeFill(fieldValue);

            } catch (Exception e) {
                throw new RuntimeException(
                        "Cascade fill field failed: " +
                                                   obj.getClass().getSimpleName() +
                                                   "." +
                                                   field.getName(), e
                );
            }
        }
        return obj;
    }

    // ========== 转换器注册API ==========
    private final Key2Map<Class<?>, Object, Function<?, ?>> key2Map = new Key2Map<>();

    /**
     * 注册普通类型转换器 F → T
     */
    public <F, T> CascadeQueryService register(Class<F> fClass, Class<T> tClass, Function<F, T> function) {
        key2Map.put(fClass, tClass, function);
        return this;
    }

    /**
     * 注册普通类型转换器 F → F
     */
    public <F> CascadeQueryService register(Class<F> fClass, Consumer<F> function) {
        Function<F, F> objectObjectFunction = (e) -> {
            function.accept(e);
            return e;
        };
        key2Map.put(fClass, fClass, objectObjectFunction);
        return this;
    }

    public CascadeQueryService register(Consumer<CascadeQueryService> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * 注册泛化集合类型转换器 F → C<T>
     * @param toCollectionType 集合类型（比如List.class/Set.class/HashSet.class等）
     * @param toElementClass 集合元素类型
     */
    @SuppressWarnings("unused")
    public <F, T, C extends Collection<T>> CascadeQueryService registerCollectionConverter(Class<F> fromClass,
            Class<C> toCollectionType, Class<T> toElementClass, Function<F, C> transform
    ) {
        key2Map.put(fromClass, collectionKey(toCollectionType, toElementClass), transform);
        return this;
    }

    /**
     * 注册泛化集合类型转换器 F → C<T>
     * @param toCollectionType 集合类型（比如List.class/Set.class/HashSet.class等）
     */
    @SuppressWarnings("unused")
    public <F, C extends Collection<F>> CascadeQueryService registerCollectionConverter(Class<F> fromClass,
            Class<C> toCollectionType, Function<F, C> transform
    ) {
        key2Map.put(fromClass, collectionKey(toCollectionType, fromClass), transform);
        return this;
    }

    // ========== 工具方法 ==========

    /**
     * 创建空集合实例，优先使用目标类型的无参构造，失败则 fallback 到默认实现
     */
    @SuppressWarnings("unchecked")
    private static <C extends Collection> C createEmptyCollection(Class<C> collectionClass) {
        try {
            Constructor<C> constructor = collectionClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            // 接口/不可变类型创建失败时走fallback逻辑
            if (Set.class.isAssignableFrom(collectionClass)) {
                return (C) new HashSet<>();
            } else if (List.class.isAssignableFrom(collectionClass)) {
                return (C) new ArrayList<>();
            } else if (Queue.class.isAssignableFrom(collectionClass)) {
                return (C) new LinkedList<>();
            }
            throw new RuntimeException("Unsupported collection type: " + collectionClass.getName(), e);
        }
    }

    // 假设Key2Map是原有自定义的双键Map实现，这里保留原有逻辑即可
    public static class Key2Map<K1, K2, V> {
        private final Map<K1, Map<K2, V>> map = new HashMap<>();

        public void put(K1 key1, K2 key2, V value) {
            map.computeIfAbsent(key1, k -> new HashMap<>()).put(key2, value);
        }

        public V get(K1 key1, K2 key2) {
            Map<K2, V> subMap = map.get(key1);
            return subMap == null ? null : subMap.get(key2);
        }
    }
}
