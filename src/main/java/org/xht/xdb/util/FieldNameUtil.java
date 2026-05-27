package org.xht.xdb.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class FieldNameUtil {
    @SneakyThrows
    public static <T, R> String getFieldName(SerializableFunction<T, R> getter) {
        SerializedLambda lambda = getSerializedLambda(getter);
        String methodName = lambda.getImplMethodName();
        return convertMethodNameToFieldName(methodName);
    }

    private static <T, R> SerializedLambda getSerializedLambda(
            Function<T, R> getter) throws InvocationTargetException, IllegalAccessException {
        Method method = ReflectUtil.getMethodByName(getter.getClass(), "writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(getter);
    }

    private static String convertMethodNameToFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            return toLowerStart(methodName.substring(3));
        } else if (methodName.startsWith("is")) {
            return toLowerStart(methodName.substring(2));
        }
        throw new IllegalArgumentException("Method is not a standard getter: " + methodName);
    }

    private static String toLowerStart(String string) {
        if (string == null || string.isEmpty()) return string;
        char[] chars = string.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}
