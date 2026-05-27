package org.xht.xdb.orm.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "unused"})
@Slf4j
public class AnnoUtil {

    public static <T, R> R getClassAnnoValue(T t, String annotationName, String methodName) {
        return getClassAnnoValueByBeanClass(t.getClass(), annotationName, methodName);
    }

    public static <T, R> R getClassAnnoValueByBeanClass(Class<T> beanClass, String annotationName,
                                                        String methodName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Annotation annotationInstance = beanClass.getAnnotation(annoClass);
            Method method = annoClass.getMethod(methodName);
            Object invoke = method.invoke(annotationInstance);
            String m = Optional.ofNullable(invoke).map(Object::toString).orElse(null);
            return (R) m;
        } catch (Exception e) {
            return null;
        }
    }

    public static <R> R getClassAnnoValueByBeanClass(String beanClassName, String annotationName,
                                                     String methodName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Class<?> beanClass = Class.forName(beanClassName);
            Annotation annotationInstance = beanClass.getAnnotation(annoClass);
            Method method = annoClass.getMethod(methodName);
            Object invoke = method.invoke(annotationInstance);
            return (R) Optional.ofNullable(invoke).map(String::valueOf).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T, R> R getMethodAnnoValue(T t, String methodName, String annotationName, String annoMethodName) {
        return getMethodAnnoValueByBeanClass(t.getClass(), methodName, annotationName, annoMethodName);
    }

    @SneakyThrows
    public static <R> R getMethodAnnoValue(String className, String fieldName, String annotationName,
                                           String methodName) {
        return getMethodAnnoValueByBeanClass(Class.forName(className), fieldName, annotationName, methodName);
    }

    public static <T, R> R getMethodAnnoValueByBeanClass(Class<T> beanClass, String methodName, String annotationName, String annotationAttrName) {
        try {
            // 获取目标方法
            Method method = beanClass.getMethod(methodName);

            // 获取该方法上的指定注解
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Annotation annotation = method.getDeclaredAnnotation(annoClass);
            if (annotation == null) return null;

            // 获取注解的属性方法
            Method attrMethod = annoClass.getMethod(annotationAttrName);

            // 调用属性方法并返回结果
            Object result = attrMethod.invoke(annotation);
            return (R) Optional.ofNullable(result).map(String::valueOf).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }


    private static Object invoke(Object annotation, Method method) {
        try {
            return method.invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> List<Field> getFieldsByBeanClass(Class<T> beanClass, String annotationName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Field[] fields = ReflectUtil.getFields(beanClass);
            return Arrays.stream(fields)
                    .filter(f -> f.getDeclaredAnnotation(annoClass) != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <R> R getAnnoValueOfField(Field field, String annotationName, String annotationAttrName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Annotation[] declaredAnnotationsByType = field.getDeclaredAnnotationsByType(annoClass);
            if (declaredAnnotationsByType.length == 0) return null;
            Annotation annotation = declaredAnnotationsByType[0];
            Class<? extends Annotation> annotationType = annotation.annotationType();
            Method method = annotationType.getMethod(annotationAttrName);
            return (R) method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("UnusedReturnValue")
    public static <R> R getAnnoValueOfMethod(Method m, String annotationName, String annotationAttrName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Annotation[] declaredAnnotationsByType = m.getDeclaredAnnotationsByType(annoClass);
            if (declaredAnnotationsByType.length == 0) return null;
            Annotation annotation = declaredAnnotationsByType[0];
            Class<? extends Annotation> annotationType = annotation.annotationType();
            Method method = annotationType.getMethod(annotationAttrName);
            return (R) method.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Field getField(T t, String annotationName) {
        return getFieldByBeanClass(t.getClass(), annotationName);
    }

    @SneakyThrows
    public static Field getField(String className, String annotationName) {
        return getFieldByBeanClass(Class.forName(className), annotationName);
    }

    public static <T> Field getFieldByBeanClass(Class<T> beanClass, String annotationName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Field[] fields = ReflectUtil.getFields(beanClass);
            return Arrays.stream(fields).filter(f -> f.getDeclaredAnnotation(annoClass) != null).findAny().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> Method getMethodByBeanClass(Class<T> beanClass, String annotationName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            Method[] methods = ReflectUtil.getMethods(beanClass);
            return Arrays.stream(methods).filter(f -> f.getDeclaredAnnotation(annoClass) != null).findAny().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isFieldAnnotated(Field field, String annotationName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            return field.isAnnotationPresent(annoClass);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMethodAnnotated(Method method, String annotationName) {
        try {
            //noinspection unchecked
            Class<? extends Annotation> annoClass = (Class<? extends Annotation>) Class.forName(annotationName);
            return method.isAnnotationPresent(annoClass);
        } catch (Exception e) {
            return false;
        }
    }
}
