package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

public class SetShortField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static Short parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Short) {
            return (Short) value;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        String string = value.toString();
        int index = string.indexOf(".");
        if (index != -1) {
            string = string.substring(0, index);
        }
        return Short.valueOf(string);
    }

}
