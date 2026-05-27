package org.xht.xdb.function.impl;

import lombok.SneakyThrows;
import org.xht.xdb.function.SetFieldValueFunction;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;

public class SetByteArrayField implements SetFieldValueFunction {
    @SneakyThrows
    @Override
    public <T> void apply(T bean, Field field, Object value) {
        field.set(bean, parse(value));
    }

    @SneakyThrows
    public static byte[] parse(Object value) {
        if (value == null)
            return null;
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof Blob) {
            Blob blob = (Blob) value;
            try (InputStream inputStream = blob.getBinaryStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        }
        if (value instanceof Byte[]) {
            return toPrimitive((Byte[]) value);
        }
        String trim = value.toString().trim();
        return trim.isEmpty() ? null : trim.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] toPrimitive(Byte[] wrapperArray) {
        byte[] basicArray = new byte[wrapperArray.length];
        for (int i = 0; i < wrapperArray.length; i++) {
            basicArray[i] = wrapperArray[i] == null ? 0 : wrapperArray[i]; // 处理 null 元素（按需替换默认值）
        }
        return basicArray;
    }
}
