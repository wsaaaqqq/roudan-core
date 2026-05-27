package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;
import java.math.BigInteger;

public class SetBigIntegerField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        if (value instanceof BigInteger) {
            field.set(bean, value);
        } else {
            field.set(bean, new BigInteger(value.toString()));
        }
    }

}
