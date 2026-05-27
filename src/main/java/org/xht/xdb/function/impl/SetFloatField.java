package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

public class SetFloatField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static Float parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return new Float(value.toString().trim());
    }
}
