package org.xht.xdb.orm.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.mapping.*;
import org.xht.xdb.util.SerializableFunction;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class OrmAnnoUtil {
    private static final OrmMappingService ormServiceXdb = new OrmMappingServiceXdb();
    private static final OrmMappingService ormServiceJpa = new OrmMappingServiceJpa();
    private static final OrmMappingService ormServiceMybatisFlex = new OrmMappingServiceMybatisFlex();
    private static final OrmMappingService ormServiceDefault = new OrmMappingServiceDefault();
    private static final OrmMappingService ormServiceJIMMER = new OrmMappingServiceJimmer();

    public static <T> String getTableName(T t) {
        return getTableNameByBeanClass(getTClass(t));
    }

    public static <T> String getTableNameByBeanClass(Class<T> beanClass) {
        return service().getTableNameByBeanClass(beanClass);
    }

    @SuppressWarnings("unused")
    public static <T> String getColName(T t, String fieldName) {
        return getColNameByBeanClass(getTClass(t), fieldName);
    }

    public static <T> Class<?> getTClass(T t) {
        Class<?> beanClass = t.getClass();
        if (beanClass.equals(Class.class)) {
            beanClass = (Class<?>) t;
        } else {
            if (beanClass.isAnonymousClass()) {
                AnnotatedType annotatedInterface = beanClass.getAnnotatedInterfaces()[0];
                Type type = annotatedInterface.getType();
                beanClass = (Class<?>) type;
            }
        }
        return beanClass;
    }

    public static <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName) {
        return service().getColNameByBeanClass(beanClass, fieldName);
    }

    public static <T, R> String getColNameByGetter(Class<T> beanClass, SerializableFunction<T, R> getter) {
        return service().getColNameByGetter(beanClass, getter);
    }

    public static <T> String getIdColName(T t) {
        return getIdColNameByBeanClass(getTClass(t));
    }

    public static <T> String getIdColNameByBeanClass(Class<T> beanClass) {
        String fieldName = getIdFieldName(beanClass);
        return service().getColNameByBeanClass(beanClass, fieldName);
    }

    public static <T> Set<String> getIdColNamesByBeanClass(Class<T> beanClass) {
        Set<String> idFieldNamesByBeanClass = service().getIdFieldNamesByBeanClass(beanClass);
        if (idFieldNamesByBeanClass == null || idFieldNamesByBeanClass.isEmpty()) {
            Set<String> names = new HashSet<>();
            names.add(getIdColNameByBeanClass(beanClass));
            return names;
        }
        return idFieldNamesByBeanClass.stream()
                                      .map(e -> OrmAnnoUtil.getColNameByBeanClass(beanClass, e))
                                      .collect(Collectors.toSet());
    }

    public static <T> Set<Field> getIdFieldsByBeanClass(Class<T> beanClass) {
        return service().getIdFieldsByBeanClass(beanClass);
    }

    public static <T> Class<?> getIdColJavaTypeByBeanClass(Class<T> beanClass) {
        String fieldName = getIdFieldName(beanClass);
        Field field = ReflectUtil.getField(beanClass, fieldName);
        return field.getType();
    }

    public static <T> String getIdFieldName(T t) {
        return getIdFieldNameByBeanClass(getTClass(t));
    }

    public static <T> Set<String> getIdFieldNames(T t) {
        return getIdFieldNamesByBeanClass(getTClass(t));
    }


    public static <T> String getIdFieldNameByBeanClass(Class<T> beanClass) {
        return service().getIdFieldNameByBeanClass(beanClass);
    }

    public static <T> Set<String> getIdFieldNamesByBeanClass(Class<T> beanClass) {
        return service().getIdFieldNamesByBeanClass(beanClass);
    }

    private static OrmMappingService service() {
        OrmType ormType = XdbConfig.getOrmType();
        if (ormType == null) {
            return ormServiceDefault;
        }
        switch (ormType) {
            case XDB:
                return ormServiceXdb;
            case JPA:
                return ormServiceJpa;
            case MYBATIS_FLEX:
                return ormServiceMybatisFlex;
            case JIMMER:
                return ormServiceJIMMER;
            default:
                return ormServiceDefault;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isIgnoreCol(Object bean, Field field) {
        boolean isIgnoreCol = ormServiceDefault.isIgnoreCol(bean, field);
        if (isIgnoreCol)
            return true;
        OrmMappingService ormServiceI = service();
        if (ormServiceI == null)
            return false;
        return ormServiceI.isIgnoreCol(bean, field);
    }

    public static boolean isNotIgnoreCol(Object bean, Field field) {
        return !isIgnoreCol(bean, field);
    }

    public static boolean isIgnoreCol(Object bean, Method method) {
        return service().isIgnoreCol(bean, method);
    }

    public static boolean isNotIgnoreCol(Object bean, Method method) {
        return !isIgnoreCol(bean, method);
    }
}
