package org.xht.xdb.util;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.vo.Row;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class BeanUtil {

    public static <T> void copyProperties(T from, T to, boolean ignoreNullFields) {
        cn.hutool.core.bean.BeanUtil.copyProperties(
                from,
                to,
                CopyOptions.create().setIgnoreNullValue(ignoreNullFields)
        );
    }

    public static MapUtil<Object> toMapUtil(Object bean) {
        return MapUtil.clone(toRow(bean));
    }

    public static Map<String, Object> toMap(Object bean) {
        return new HashMap<>(toRow(bean));
    }

    public static Row toRow(Object bean) {
        return bean == null ? new Row() : toRow(bean, columnsFor(bean));
    }

    public static <T> Map<T, Row> toMapBeanRow(Collection<T> beans) {
        Map<T, Row> mapBeanRow = new HashMap<>();
        forEachConverted(beans, mapBeanRow::put);
        return mapBeanRow;
    }

    public static <T> List<Row> toRows(Collection<T> beans, Function<Row, Boolean> test, BiConsumer<T, Row> success,
                                       BiConsumer<T, Row> fail
    ) {
        // 全批转换成功后再回调，避免向调用方暴露失败批次的前缀。
        List<Row> rows = toRows(beans);
        if (rows.isEmpty()) return rows;
        Iterator<T> iterator = beans.iterator();
        for (Row row : rows) {
            T bean = iterator.next();
            if (test.apply(row)) {
                success.accept(bean, row);
            } else {
                fail.accept(bean, row);
            }
        }
        return rows;
    }

    public static <T> List<Row> toRows(Collection<T> beans) {
        List<Row> rows = new ArrayList<>();
        forEachConverted(beans, (bean, row) -> rows.add(row));
        return rows;
    }

    public static List<MapUtil<Object>> toMapUtils(Collection<Object> beans) {
        List<MapUtil<Object>> maps = new ArrayList<>();
        forEachConverted(beans, (bean, row) -> maps.add(MapUtil.clone(row)));
        return maps;
    }

    public static List<Map<String, Object>> toMaps(Collection<Object> beans) {
        List<Map<String, Object>> maps = new ArrayList<>();
        forEachConverted(beans, (bean, row) -> maps.add(new HashMap<>(row)));
        return maps;
    }

    private static <T> void forEachConverted(Collection<T> beans, BiConsumer<T, Row> consumer) {
        if (beans == null || beans.isEmpty()) return;
        // 限于本批次，既避免全局缓存锁开销，也不会跨 ormType 配置复用列映射。
        Map<Class<?>, List<ColumnWriter>> plans = new HashMap<>();
        for (T bean : beans) {
            Objects.requireNonNull(bean, "批量转换的实体不能为空");
            List<ColumnWriter> columns = plans.computeIfAbsent(bean.getClass(), type -> columnsFor(bean));
            consumer.accept(bean, toRow(bean, columns));
        }
    }

    private static List<ColumnWriter> columnsFor(Object bean) {
        List<ColumnWriter> columns = new ArrayList<>();
        for (Field field : ReflectUtil.getFields(bean.getClass())) {
            try {
                if (!OrmAnnoUtil.isNotIgnoreCol(bean, field)) continue;
                field.setAccessible(true);
                columns.add(new ColumnWriter(field, OrmAnnoUtil.getColName(bean, field.getName()),
                        JsonUtils.jdbcValueConverter(field)));
            } catch (Exception e) {
                throw conversionFailure(bean, field, e);
            }
        }
        return columns;
    }

    private static Row toRow(Object bean, List<ColumnWriter> columns) {
        Row row = new Row(columns.size());
        for (ColumnWriter column : columns) {
            try {
                row.put(column.name, column.converter.apply(column.field.get(bean)));
            } catch (Exception e) {
                throw conversionFailure(bean, column.field, e);
            }
        }
        return row;
    }

    private static IllegalArgumentException conversionFailure(Object bean, Field field, Exception cause) {
        return new IllegalArgumentException("实体字段转换失败: " + bean.getClass().getName()
                + "." + field.getName(), cause);
    }

    private static final class ColumnWriter {
        final Field field;
        final String name;
        final Function<Object, Object> converter;

        ColumnWriter(Field field, String name, Function<Object, Object> converter) {
            this.field = field;
            this.name = name;
            this.converter = converter;
        }
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
