package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

public class SetLongField implements SetFieldValueFunction {
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        try {
            field.set(bean, parse(value));
        } catch (Exception e) {
            throw new RuntimeException("设置Long字段失败", e);
        }
    }

    @SneakyThrows
    public static Long parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Long)
            return (Long) value;
        return new Long(value.toString());
    }
}
