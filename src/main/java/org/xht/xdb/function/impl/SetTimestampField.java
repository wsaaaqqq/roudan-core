package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.sql.Timestamp;

public class SetTimestampField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static Timestamp parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        String trim = value.toString().trim();
        return new Timestamp(Long.parseLong(trim));
    }

}
