package org.xht.xdb.util;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.vo.Row;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
public class BeanUtil {

    public static <T> void copyProperties(T from, T to, boolean ignoreNullFields) {
        cn.hutool.core.bean.BeanUtil.copyProperties(
                from,
                to,
                CopyOptions.create().setIgnoreNullValue(ignoreNullFields)
        );
    }

    public static MapUtil<Object> toMapUtil(Object bean) {
        MapUtil<Object> map = new MapUtil<>();
        try {
            Field[] fields = ReflectUtil.getFields(bean.getClass());
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    addField(bean, field, map);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return map;
    }

    private static void addField(Object bean, Field field, Map<String, Object> map) throws IllegalAccessException {
        if (OrmAnnoUtil.isNotIgnoreCol(bean, field)) {
            String colName = OrmAnnoUtil.getColName(bean, field.getName());
            map.put(colName, field.get(bean));
        }
    }

    private static void addField(Object bean, Field field, MapUtil<Object> map) throws IllegalAccessException {
        if (OrmAnnoUtil.isNotIgnoreCol(bean, field)) {
            String colName = OrmAnnoUtil.getColName(bean, field.getName());
            map.add(colName, field.get(bean));
        }
    }

    private static void addField(Object bean, Field field, Row map) throws IllegalAccessException {
        if (OrmAnnoUtil.isNotIgnoreCol(bean, field)) {
            String colName = OrmAnnoUtil.getColName(bean, field.getName());
            map.put(colName, field.get(bean));
        }
    }

    public static Map<String, Object> toMap(Object bean) {
        Map<String, Object> map = new HashMap<>();
        try {
            Field[] fields = ReflectUtil.getFields(bean.getClass());
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    addField(bean, field, map);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return map;
    }

    public static Row toRow(Object bean) {
        Row map = new Row();
        try {
            Field[] fields = ReflectUtil.getFields(bean.getClass());
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    addField(bean, field, map);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return map;
    }

    public static <T> Map<T, Row> toMapBeanRow(Collection<T> beans) {
        Map<T, Row> mapBeanRow = new HashMap<>();
        if (beans == null || beans.isEmpty())
            return mapBeanRow;
        try {
            Object bean0 = beans.stream().findFirst().get();
            Field[] fields = ReflectUtil.getFields(bean0.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
            }
            Map<Field, Boolean> notIgnoreColMap = new HashMap<>();
            Map<Field, String> colNameMap = new HashMap<>();
            for (T bean : beans) {
                Row map = new Row();
                for (Field field : fields) {
                    Boolean notIgnoreCol = notIgnoreColMap.get(field);
                    if (notIgnoreCol == null) {
                        notIgnoreCol = OrmAnnoUtil.isNotIgnoreCol(bean, field);
                        notIgnoreColMap.put(field, notIgnoreCol);
                    }
                    if (notIgnoreCol) {
                        String colName = colNameMap.get(field);
                        if (colName == null) {
                            colName = OrmAnnoUtil.getColName(bean, field.getName());
                            colNameMap.put(field, colName);
                        }
                        map.put(colName, field.get(bean));
                    }
                }
                mapBeanRow.put(bean, map);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return mapBeanRow;
    }

    public static <T> List<Row> toRows(Collection<T> beans, Function<Row, Boolean> test, BiConsumer<T, Row> success,
            BiConsumer<T, Row> fail
    ) {
        List<Row> rows = new ArrayList<>();
        if (beans == null || beans.isEmpty()) {
            return rows;
        }
        try {
            Object bean0 = beans.stream().findFirst().get();
            Field[] fields = ReflectUtil.getFields(bean0.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
            }
            Map<Field, Boolean> notIgnoreColMap = new HashMap<>();
            Map<Field, String> colNameMap = new HashMap<>();
            for (T bean : beans) {
                Row row = new Row();
                for (Field field : fields) {
                    Boolean notIgnoreCol = notIgnoreColMap.get(field);
                    if (notIgnoreCol == null) {
                        notIgnoreCol = OrmAnnoUtil.isNotIgnoreCol(bean, field);
                        notIgnoreColMap.put(field, notIgnoreCol);
                    }
                    if (notIgnoreCol) {
                        String colName = colNameMap.get(field);
                        if (colName == null) {
                            colName = OrmAnnoUtil.getColName(bean, field.getName());
                            colNameMap.put(field, colName);
                        }
                        row.put(colName, field.get(bean));
                    }
                }
                rows.add(row);
                if (test.apply(row)) {
                    success.accept(bean, row);
                } else {
                    fail.accept(bean, row);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return rows;
    }

    public static <T> List<Row> toRows(Collection<T> beans) {
        List<Row> rows = new ArrayList<>();
        if (beans == null || beans.isEmpty()) {
            return rows;
        }
        try {
            Object bean0 = beans.stream().findFirst().get();
            Field[] fields = ReflectUtil.getFields(bean0.getClass());
            for (Field field : fields) {
                field.setAccessible(true);
            }
            Map<Field, Boolean> notIgnoreColMap = new HashMap<>();
            Map<Field, String> colNameMap = new HashMap<>();
            for (T bean : beans) {
                Row row = new Row();
                for (Field field : fields) {
                    Boolean notIgnoreCol = notIgnoreColMap.get(field);
                    if (notIgnoreCol == null) {
                        notIgnoreCol = OrmAnnoUtil.isNotIgnoreCol(bean, field);
                        notIgnoreColMap.put(field, notIgnoreCol);
                    }
                    if (notIgnoreCol) {
                        String colName = colNameMap.get(field);
                        if (colName == null) {
                            colName = OrmAnnoUtil.getColName(bean, field.getName());
                            colNameMap.put(field, colName);
                        }
                        row.put(colName, field.get(bean));
                    }
                }
                rows.add(row);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return rows;
    }

    public static List<MapUtil<Object>> toMapUtils(Collection<Object> beans) {
        List<MapUtil<Object>> maps = new ArrayList<>();
        if (beans != null && !beans.isEmpty()) {
            Class<?> beanClass = beans.iterator().next().getClass();
            Field[] fields = ReflectUtil.getFields(beanClass);
            for (Field field : fields) {
                field.setAccessible(true);
            }
            for (Object bean : beans) {
                MapUtil<Object> map = new MapUtil<>();
                for (Field field : fields) {
                    try {
                        addField(bean, field, map);
                    } catch (Exception ignored) {
                    }
                }
                maps.add(map);
            }
        }
        return maps;
    }

    public static List<Map<String, Object>> toMaps(Collection<Object> beans) {
        List<Map<String, Object>> maps = new ArrayList<>();
        if (beans != null && !beans.isEmpty()) {
            Class<?> beanClass = beans.iterator().next().getClass();
            Field[] fields = ReflectUtil.getFields(beanClass);
            for (Field field : fields) {
                field.setAccessible(true);
            }
            for (Object bean : beans) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : fields) {
                    try {
                        addField(bean, field, map);
                    } catch (Exception ignored) {
                    }
                }
                maps.add(map);
            }
        }
        return maps;
    }

    @SneakyThrows
    public static <T, V> T toBean(Map<String, V> map, Class<T> beanClass) {
        T t = beanClass.newInstance();
        Set<String> keySet = map.keySet();
        Field[] fields = ReflectUtil.getFields(beanClass);
        Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, f -> f));
        for (String key : keySet) {
            V v = map.get(key);
            try {
                Field field = fieldMap.get(key);
                field.setAccessible(true);
                field.set(t, v);
            } catch (Exception ignored) {
            }
        }
        return t;
    }

    @SneakyThrows
    public static <T, V> List<T> toBeans(Collection<Map<String, V>> maps, Class<T> beanClass) {
        List<T> ts = new ArrayList<>();//创建对象
        if (maps != null) {
            Map<String, Field> fieldMap = new HashMap<>();
            for (Map<String, V> map : maps) {
                T t = beanClass.newInstance();
                Set<String> keySet = map.keySet();
                for (String key : keySet) {
                    V v = map.get(key);
                    Field field = fieldMap.get(key);
                    try {
                        if (field == null) {
                            field = ReflectUtil.getField(beanClass, key);
                            field.setAccessible(true);
                            fieldMap.put(key, field);
                        }
                        field.set(t, v);
                    } catch (Exception ignored) {
                    }
                }
                ts.add(t);
            }
        }
        return ts;
    }

}
