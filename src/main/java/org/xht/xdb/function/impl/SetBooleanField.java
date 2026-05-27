package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

public class SetBooleanField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T target, Field field, Object value) {
        field.set(target, parse(value));
    }

    @SneakyThrows
    public static Boolean parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String s = String.valueOf(value).trim();
        return "1".equals(s) ||
                "true".equalsIgnoreCase(s) ||
                "T".equalsIgnoreCase(s) ||
                "yes".equalsIgnoreCase(s) ||
                "Y".equalsIgnoreCase(s);
    }

    public static void main(String[] args) {
        Object value = 1.1d;
        System.out.println(value);
    }
}
