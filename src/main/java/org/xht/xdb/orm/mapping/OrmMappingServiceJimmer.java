package org.xht.xdb.orm.mapping;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.util.AnnoUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public class OrmMappingServiceJimmer implements OrmMappingService {
    @Override
    public <T> String getTableNameByBeanClass(Class<T> beanClass) {
        String name = AnnoUtil.getClassAnnoValueByBeanClass(beanClass, "org.babyfish.jimmer.sql.Table", "name");
        if (name == null || name.isEmpty())
            return OrmMappingService.impDefault.getTableNameByBeanClass(beanClass);
        return name;
    }

    @Override
    public <T> String getColNameByBeanClass(Class<T> beanClass, String fieldName) {
        String name =
                AnnoUtil.getMethodAnnoValueByBeanClass(beanClass, fieldName, "org.babyfish.jimmer.sql.Column", "name");
        if (name == null || name.isEmpty())
            name = AnnoUtil.getMethodAnnoValueByBeanClass(
                    beanClass,
                    fieldName,
                    "org.babyfish.jimmer.sql.JoinColumn",
                    "name"
            );
        if (name == null || name.isEmpty())
            return OrmMappingService.impDefault.getColNameByBeanClass(beanClass, fieldName);
        return name;
    }

    @Override
    public <T> String getIdFieldNameByBeanClass(Class<T> beanClass) {
        Method methodByBeanClass = AnnoUtil.getMethodByBeanClass(beanClass, "org.babyfish.jimmer.sql.Id");
        if (methodByBeanClass == null) {
            return OrmMappingService.impDefault.getIdFieldNameByBeanClass(beanClass);
        }
        return methodByBeanClass.getName();
    }

    @Override
    public <T> Field getIdFieldByBeanClass(Class<T> beanClass) {
        throw new UnsupportedOperationException("jimmer not support field, use getIdMethodByBeanClass instead");
    }

    public <T> Method getIdMethodByBeanClass(Class<T> beanClass) {
        return AnnoUtil.getMethodByBeanClass(beanClass, "org.babyfish.jimmer.sql.Id");
    }

    @Override
    public boolean isIgnoreCol(Object bean, Field field) {
        if (field == null || "serialVersionUID".equals(field.getName())) {
            return true;
        }
        return AnnoUtil.isFieldAnnotated(field, "org.babyfish.jimmer.sql.Transient") ||
                AnnoUtil.isFieldAnnotated(field, "org.babyfish.jimmer.sql.ManyToMany") ||
                AnnoUtil.isFieldAnnotated(field, "org.babyfish.jimmer.sql.OneToMany");
    }

    public boolean isIgnoreCol(Object bean, Method method) {
        if (method == null) {
            return true;
        }
        return AnnoUtil.isMethodAnnotated(method, "org.babyfish.jimmer.sql.Transient") ||
                AnnoUtil.isMethodAnnotated(method, "org.babyfish.jimmer.sql.ManyToMany") ||
                AnnoUtil.isMethodAnnotated(method, "org.babyfish.jimmer.sql.OneToMany");
    }

}
