package org.xht.xdb.orm.mapping;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.util.Underline2CamelUtil;

import java.lang.reflect.Field;

@Slf4j
public class OrmMappingServiceDefault implements OrmMappingService {

    /**
     * 获取表名
     */
    @Override
    public <T> String getTableNameByBeanClass(Class<T> beanClass) {
        return Underline2CamelUtil.camel2Underline(beanClass.getSimpleName(), true);
    }

    @Override
    public <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName) {
        return Underline2CamelUtil.camel2Underline(fieldName, true);
    }

    @Override
    public <T> String getIdFieldNameByBeanClass(Class<T> beanClass) {
        return "id";
    }

    @SneakyThrows
    @Override
    public <T> Field getIdFieldByBeanClass(Class<T> beanClass) {
        return beanClass.getField("id");
    }

    @Override
    public boolean isIgnoreCol(Object bean, Field field) {
        return field == null || "serialVersionUID".equals(field.getName());
    }
}
