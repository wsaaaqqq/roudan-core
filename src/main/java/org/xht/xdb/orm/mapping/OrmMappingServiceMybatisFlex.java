package org.xht.xdb.orm.mapping;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.util.AnnoUtil;

import java.lang.reflect.Field;

@Slf4j
public class OrmMappingServiceMybatisFlex implements OrmMappingService {

    @Override
    public <T> String getTableNameByBeanClass(Class<T> beanClass) {
        String name = AnnoUtil.getClassAnnoValueByBeanClass(beanClass, "com.mybatisflex.annotation.Table", "value");
        if (name == null || name.isEmpty())
            return OrmMappingService.impDefault.getTableNameByBeanClass(beanClass);
        return name;
    }

    @Override
    public <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName) {
        Field field = ReflectUtil.getField(beanClass, fieldName);
        if (field == null) {
            return OrmMappingService.impDefault.getColNameByBeanClass(beanClass, fieldName);
        }
        String name = AnnoUtil.getAnnoValueOfField(field, "com.mybatisflex.annotation.Column", "value");
        if (name == null || name.isEmpty()) {
            return OrmMappingService.impDefault.getColNameByBeanClass(beanClass, fieldName);
        }
        return name;
    }

    @Override
    public <T> String getIdFieldNameByBeanClass(Class<T> beanClass) {
        Field field = AnnoUtil.getFieldByBeanClass(beanClass, "com.mybatisflex.annotation.Id");
        if (field == null) {
            return OrmMappingService.impDefault.getIdFieldNameByBeanClass(beanClass);
        }
        return field.getName();
    }

    @Override
    public <T> Field getIdFieldByBeanClass(Class<T> beanClass) {
        Field field = AnnoUtil.getFieldByBeanClass(beanClass, "com.mybatisflex.annotation.Id");
        if (field == null) {
            return OrmMappingService.impDefault.getIdFieldByBeanClass(beanClass);
        }
        return field;
    }

    @Override
    public boolean isIgnoreCol(Object bean, Field field) {
        if (field == null || "serialVersionUID".equals(field.getName())) {
            return true;
        }
        Boolean ignore = AnnoUtil.getAnnoValueOfField(field, "com.mybatisflex.annotation.Column", "ignore");
        return Boolean.TRUE.equals(ignore);
    }
}
