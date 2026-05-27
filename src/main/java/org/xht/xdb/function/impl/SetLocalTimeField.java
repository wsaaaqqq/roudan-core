package org.xht.xdb.function.impl;

import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SetLocalTimeField implements SetFieldValueFunction {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public <T> void apply(T bean, Field field, Object value) {
        try {
            field.set(bean, parse(value));
        } catch (Exception e) {
            throw new RuntimeException("设置LocalTime字段失败", e);
        }
    }

    public static LocalTime parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof LocalTime)
            return (LocalTime) value;
        return LocalTime.parse(value.toString(), formatter);
    }
}
