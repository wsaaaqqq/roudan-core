package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;
import org.xht.xdb.util.ClobUtil;

import java.lang.reflect.Field;
import java.sql.Clob;

public class SetFieldWithValueDefault implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        String text;
        if (value instanceof String) {
            text = (String) value;
        } else if (value instanceof Clob) {
            text = ClobUtil.toString((Clob) value);
        } else {
            field.set(bean, value);
            return;
        }
        Object jsonObjectValue = Class.forName("cn.hutool.json.JSONUtil")
                .getMethod("toBean", String.class, Class.class).invoke(bean,text,field.getType());
        field.set(bean, jsonObjectValue);
    }
}
