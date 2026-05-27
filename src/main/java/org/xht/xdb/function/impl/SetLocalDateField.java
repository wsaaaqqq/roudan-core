package org.xht.xdb.function.impl;

import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SetLocalDateField implements SetFieldValueFunction {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public <T> void apply(T bean, Field field, Object value) {
        try {
            field.set(bean, parse(value));
        } catch (Exception e) {
            throw new RuntimeException("设置LocalDate字段失败", e);
        }
    }

    public static LocalDate parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof LocalDate)
            return (LocalDate) value;
        return LocalDate.parse(value.toString(), formatter);
    }
}
