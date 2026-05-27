package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;

public class SetStringField implements SetFieldValueFunction {
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        try {
            field.set(bean, parse(value));
        } catch (Exception e) {
            throw new RuntimeException("设置String字段失败", e);
        }
    }

    @SneakyThrows
    public static String parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof String)
            return (String) value;
        if (value instanceof Clob) {
            Clob clob = (Clob) value;
            try (Reader reader = clob.getCharacterStream()) {
                StringBuilder stringBuilder = new StringBuilder();
                char[] buffer = new char[4096];
                int charsRead;
                while ((charsRead = reader.read(buffer)) != -1) {
                    stringBuilder.append(buffer, 0, charsRead);
                }
                return stringBuilder.toString();
            }
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value.toString();
    }
}
