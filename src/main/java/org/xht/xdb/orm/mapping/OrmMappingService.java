package org.xht.xdb.orm.mapping;

import lombok.SneakyThrows;
import org.xht.xdb.orm.anno.Id;
import org.xht.xdb.orm.util.AnnoUtil;
import org.xht.xdb.util.FieldNameUtil;
import org.xht.xdb.util.SerializableFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface OrmMappingService {
    OrmMappingService impDefault = new OrmMappingServiceDefault();

    default <T> String getTableName(T t) {
        return getTableNameByBeanClass(t.getClass());
    }

    <T> String getTableNameByBeanClass(Class<T> beanClass);

    default <T> String getColName(T t, String fieldName) {
        return getColNameByBeanClass(t.getClass(), fieldName);
    }

    default <T, R> String getColName(T t, SerializableFunction<T, R> getter) {
        return getColName(t, FieldNameUtil.getFieldName(getter));
    }

    <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName);

    @SneakyThrows
    default <T, R> String getColNameByGetter(Class<T> beanClass, SerializableFunction<T, R> getter) {
        return getColNameByBeanClass(beanClass, FieldNameUtil.getFieldName(getter));
    }

    default <T> String getIdFieldName(T t) {
        return getIdFieldNameByBeanClass(t.getClass());
    }

    <T> String getIdFieldNameByBeanClass(Class<T> beanClass);

    <T> Field getIdFieldByBeanClass(Class<T> beanClass);

    default <T> Set<String> getIdFieldNamesByBeanClass(Class<T> beanClass) {
        List<Field> fields = AnnoUtil.getFieldsByBeanClass(beanClass, Id.class.getName());
        if (fields == null || fields.isEmpty()) {
            String idFieldName = getIdFieldName(beanClass);
            if (idFieldName == null || idFieldName.isEmpty())
                return null;
            return new HashSet<String>() {{
                add(idFieldName);
            }};
        }
        Set<String> ids = new HashSet<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            String colName = getColNameByBeanClass(beanClass, fieldName);
            ids.add(colName);
        }
        return ids;
    }

    default <T> Set<Field> getIdFieldsByBeanClass(Class<T> beanClass) {
        List<Field> fields = AnnoUtil.getFieldsByBeanClass(beanClass, Id.class.getName());
        if (fields == null || fields.isEmpty()) {
            Field idField = getIdFieldByBeanClass(beanClass);
            if (idField == null)
                return null;
            return new HashSet<Field>() {{
                add(idField);
            }};
        }
        return new HashSet<>(fields);
    }

    boolean isIgnoreCol(Object bean, Field field);

    default boolean isIgnoreCol(Object bean, Method method) {
        return true;
    }
}
