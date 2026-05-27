package org.xht.xdb.function.impl;

import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SetLocalDateTimeField implements SetFieldValueFunction {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public <T> void apply(T bean, Field field, Object value) {
        try {
            field.set(bean, parse(value));
        } catch (Exception e) {
            throw new RuntimeException("设置LocalDateTime字段失败", e);
        }
    }

    public static LocalDateTime parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof LocalDateTime)
            return (LocalDateTime) value;
        return LocalDateTime.parse(value.toString(), formatter);
    }
}
