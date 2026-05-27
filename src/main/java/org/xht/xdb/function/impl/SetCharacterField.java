package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

public class SetCharacterField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static Character parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof Character) {
            return (Character) value;
        }
        String trim = value.toString().trim();
        return trim.charAt(0);
    }
}
