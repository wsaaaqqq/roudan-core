package org.xht.xdb.orm.mapping;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.anno.Column;
import org.xht.xdb.orm.anno.Id;
import org.xht.xdb.orm.anno.Ignore;
import org.xht.xdb.orm.anno.Table;
import org.xht.xdb.orm.util.AnnoUtil;

import java.lang.reflect.Field;

@Slf4j
public class OrmMappingServiceXdb implements OrmMappingService {

    @Override
    public <T> String getTableNameByBeanClass(Class<T> beanClass) {
        String name = AnnoUtil.getClassAnnoValueByBeanClass(beanClass, Table.class.getName(), "value");
        if (name == null || name.isEmpty()) {
            return OrmMappingService.impDefault.getTableNameByBeanClass(beanClass);
        }
        return name;
    }

    @Override
    public <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName) {
        Field field = ReflectUtil.getField(beanClass, fieldName);
        if (field == null) {
            return OrmMappingService.impDefault.getColNameByBeanClass(beanClass, fieldName);
        }
        String name = AnnoUtil.getAnnoValueOfField(field, Column.class.getName(), "name");
        if (name == null || name.isEmpty()) {
            name = OrmMappingService.impDefault.getColNameByBeanClass(beanClass, fieldName);
        }
        return name;
    }

    @Override
    public <T> String getIdFieldNameByBeanClass(Class<T> beanClass) {
        Field field = AnnoUtil.getFieldByBeanClass(beanClass, Id.class.getName());
        if (field == null) {
            return OrmMappingService.impDefault.getIdFieldNameByBeanClass(beanClass);
        }
        return field.getName();
    }

    @Override
    public <T> Field getIdFieldByBeanClass(Class<T> beanClass) {
        Field field = AnnoUtil.getFieldByBeanClass(beanClass, Id.class.getName());
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
        return AnnoUtil.isFieldAnnotated(field, Ignore.class.getName());
    }
}
