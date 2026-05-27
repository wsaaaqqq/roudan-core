package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class SetBigDecimalField implements SetFieldValueFunction {
    @Override
    @SneakyThrows
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static BigDecimal parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString().trim());
    }
}
