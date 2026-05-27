package org.xht.xdb.orm.cascade;

import cn.hutool.core.util.ReflectUtil;
import org.xht.xdb.Xdb;
import org.xht.xdb.orm.EntityService;
import org.xht.xdb.orm.util.OrmAnnoUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 级联保存工具类
 * 特性：
 * 1. 使用迭代代替递归，无深度限制
 * 2. 支持批量保存同类型实体
 * 3. 自动处理循环引用
 * 4. 保存策略可切换
 * 5. 根据ID字段值去重重复实体
 *
 * @version 4.0
 */
public class BaseCascadeServiceImpl {

    // ==================== 常量 ====================

    private static final Set<String> BASIC_PACKAGES = new HashSet<String>() {{
        add("java.lang");
        add("java.time");
        add("java.math");
        add("java.sql");
        add("java.util.Date");
    }};

    private static final Set<Class<?>> BASIC_TYPES = new HashSet<Class<?>>() {{
        add(String.class);
        add(Boolean.class);
        add(Integer.class);
        add(Long.class);
        add(Double.class);
        add(Float.class);
        add(Short.class);
        add(Byte.class);
        add(Character.class);
        add(BigDecimal.class);
        add(BigInteger.class);
    }};

    private static final Map<Class<?>, List<Field>> ID_FIELDS_CACHE = new ConcurrentHashMap<>();

    private Function<Cascade, Boolean> cascadeFunction;

    // 分析保存顺序（可选）
    private List<Class<?>> saveOrder = new ArrayList<>();

    // 数据源配置（可选）
    private final Map<Class<?>, String> dataSources = new HashMap<>();

    private int batchSize = 1000;
    private boolean ignoreNulls = true;

    final BiConsumer<Class<?>, List<?>> DB_FUNC_SAVE_OR_UPDATE = (type, entities) -> {
        if (entities == null || entities.isEmpty())
            return;
        String datasource = dataSources.getOrDefault(type, Xdb.DEFAULT_DATASOURCE_NAME);
        @SuppressWarnings("unchecked") EntityService<Object> service =
                EntityService.of((Class<Object>) type, datasource);
        //noinspection unchecked
        service.saveOrUpdate((Collection<Object>) entities, batchSize, ignoreNulls);
    };

    final BiConsumer<Class<?>, List<?>> DB_FUNC_DELETE = (type, entities) -> {
        if (entities == null || entities.isEmpty())
            return;
        String datasource = dataSources.getOrDefault(type, Xdb.DEFAULT_DATASOURCE_NAME);
        @SuppressWarnings("unchecked") EntityService<Object> service =
                EntityService.of((Class<Object>) type, datasource);
        //noinspection unchecked
        service.delete((Collection<Object>) entities, batchSize);
    };

    // ==================== 运行时状态 ====================

    /** 已访问实体（基于引用地址） */
    private Set<Object> visited;

    /** 按类型分组的待保存实体 */
    Map<Class<?>, List<Object>> pendingByType;

    /** 按类型和ID值存储已存在的实体，用于去重 */
    private Map<Class<?>, Set<List<Object>>> cacheClassIds;

    // ==================== 公共API ====================

    @SuppressWarnings("UnusedReturnValue")
    public BaseCascadeServiceImpl beanSaveOrders(Class<?>... beanSaveOrders) {
        this.saveOrder = Arrays.asList(beanSaveOrders);
        return this;
    }

    public BaseCascadeServiceImpl cascadeFunction(Function<Cascade, Boolean> cascadeFunction) {
        this.cascadeFunction = cascadeFunction;
        return this;
    }

    public BaseCascadeServiceImpl ignoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public <T> void process(Collection<T> beans, int batchSize,BiConsumer<Class<?>, List<?>> functionToDB) {
        this.batchSize = batchSize;
        if (beans == null) {
            return;
        }
        init();
        try {
            for (T bean : beans) {
                collectEntities(bean);
            }


            if (pendingByType.isEmpty()) {
                return;
            }
            Set<Class<?>> remainingTypes;
            // 优先保存指定了顺序的类型
            Set<Class<?>> keys = pendingByType.keySet();
            if (saveOrder == null || saveOrder.isEmpty()) {
                remainingTypes = keys;
            } else {
                saveOrder.forEach(type -> functionToDB.accept(type, pendingByType.get(type)));
                remainingTypes = keys.stream().filter(type -> !saveOrder.contains(type)).collect(Collectors.toSet());
            }
            // 再保存剩余的类型
            for (Class<?> remainingType : remainingTypes) {
                functionToDB.accept(remainingType, pendingByType.get(remainingType));
            }

        } finally {
            cleanup();
        }
    }

    // ==================== 初始化与清理 ====================

    private void init() {
        this.visited = Collections.newSetFromMap(new IdentityHashMap<>());
        this.pendingByType = new LinkedHashMap<>();
        this.cacheClassIds = new HashMap<>();
    }

    private void cleanup() {
        if (visited != null)
            visited.clear();
        if (pendingByType != null)
            pendingByType.clear();
        if (cacheClassIds != null)
            cacheClassIds.clear();
    }

    // ==================== 阶段1：收集实体（迭代方式） ====================

    /**
     * 使用BFS迭代收集所有待保存实体
     */
    private void collectEntities(Object root) {
        // 使用队列进行BFS遍历
        Queue<Object> queue = new LinkedList<>();
        queue.offer(root);
        visited.add(root);

        while (!queue.isEmpty()) {
            Object current = queue.poll();

            // 跳过基础类型
            if (isBasicType(current.getClass())) {
                continue;
            }

            // 添加到待保存队列（如果不存在重复的ID）
            if (addToPending(current)) {
                // 收集子实体并入队
                collectChildren(current, queue);
            }
        }
    }

    /**
     * 收集子实体并入队
     */
    private void collectChildren(Object entity, Queue<Object> queue) {
        List<Field> fields = getCascadeFields(entity.getClass());

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(entity);

                if (value == null)
                    continue;

                // 处理不同类型
                collectByType(value, queue);

            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }
    }

    /**
     * 按类型收集并入队
     */
    private void collectByType(Object value, Queue<Object> queue) {
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (!isProxyCollection(collection)) {
                for (Object item : collection) {
                    enqueueIfNew(item, queue);
                }
            }
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                enqueueIfNew(item, queue);
            }
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                enqueueIfNew(entry.getKey(), queue);
                enqueueIfNew(entry.getValue(), queue);
            }
        } else if (!isBasicType(value.getClass())) {
            enqueueIfNew(value, queue);
        }
    }

    /**
     * 如果是新对象则入队
     */
    private void enqueueIfNew(Object obj, Queue<Object> queue) {
        if (obj != null && !visited.contains(obj) && !isBasicType(obj.getClass()) && !isDuplicateEntity(obj)) {
            visited.add(obj);
            queue.offer(obj);
        }
    }

    /**
     * 判断实体是否为重复的（基于ID字段值）
     */
    private boolean isDuplicateEntity(Object entity) {
        Class<?> type = entity.getClass();
        List<Object> idValues = getIdFieldValues(entity);

        if (idValues == null || idValues.isEmpty()) {
            // 如果没有ID字段，则无法去重，视为非重复
            return false;
        }
        Set<List<Object>> typeCache = cacheClassIds.computeIfAbsent(type, k -> new HashSet<>());
        return typeCache.contains(idValues); // 存在重复的ID值，返回true表示是重复实体
    }

    /**
     * 添加到待保存队列（按类型分组），如果存在重复ID则不添加
     * @return true 如果成功添加，false 如果是重复实体
     */
    private boolean addToPending(Object entity) {
        Class<?> type = entity.getClass();
        List<Object> idValues = getIdFieldValues(entity);

        Set<List<Object>> typeCache = cacheClassIds.computeIfAbsent(type, k -> new HashSet<>());

        // 检查是否存在相同ID值的实体
        if (idValues != null && !idValues.isEmpty() && typeCache.contains(idValues)) {
            // 存在相同ID值的实体，不添加重复项
            return false;
        }

        // 将实体添加到缓存中（用于后续去重）
        if (idValues != null && !idValues.isEmpty()) {
            typeCache.add(idValues);
        }

        // 添加到待保存队列
        pendingByType.computeIfAbsent(type, t -> new ArrayList<>()).add(entity);
        return true;
    }

    private List<Object> getIdFieldValues(Object entity) {
        List<Field> fields = getIdField(entity);
        if (fields.isEmpty()) {
            return new ArrayList<>(); // 如果没有ID字段，返回空列表
        }
        return fields.stream().map(f -> {
            try {
                f.setAccessible(true);
                return ReflectUtil.getFieldValue(entity, f);
            } catch (Exception e) {
                // 如果获取字段值失败，返回null
                return null;
            }
        }).collect(Collectors.toList());
    }

    private List<Field> getIdField(Object entity) {
        Class<?> entityClass = entity.getClass();
        List<Field> fields = ID_FIELDS_CACHE.get(entityClass);
        if (fields == null) {
            Set<Field> idFields = OrmAnnoUtil.getIdFieldsByBeanClass(entityClass);
            fields = new ArrayList<>(idFields);
            ID_FIELDS_CACHE.put(entityClass, fields);
        }
        return fields;
    }


    // ==================== 字段处理 ====================

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private List<Field> getCascadeFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, this::resolveCascadeFields);
    }

    private List<Field> resolveCascadeFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            if (shouldCascade(field)) {
                result.add(field);
            }
        }
        return result;
    }

    private boolean shouldCascade(Field field) {
        // 检查 @CascadeSave 注解
        Cascade annotation = field.getAnnotation(Cascade.class);
        if (annotation != null) {
            String datasource = annotation.datasource();
            if (datasource == null || datasource.isEmpty()) {
                dataSources.put(field.getType(), Xdb.DEFAULT_DATASOURCE_NAME);
            } else {
                dataSources.put(field.getType(), datasource);
            }
            return cascadeFunction.apply(annotation);
        }
        return false;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    // ==================== 类型判断 ====================

    private boolean isBasicType(Class<?> type) {
        if (type == null)
            return true;
        if (type.isPrimitive())
            return true;
        if (type.isEnum())
            return true;
        if (BASIC_TYPES.contains(type))
            return true;

        String name = type.getName();
        return BASIC_PACKAGES.stream().anyMatch(name::startsWith);
    }

    private boolean isProxyCollection(Collection<?> collection) {
        String className = collection.getClass().getName();
        return className.contains("Hibernate") || className.contains("Persistent") || className.contains("$Proxy");
    }

}
