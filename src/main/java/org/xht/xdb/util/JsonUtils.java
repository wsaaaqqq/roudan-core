package org.xht.xdb.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.xht.xdb.function.impl.SetBooleanField;

public class JsonUtils {

    /** 按Field缓存分支策略：同一Field只判断一次，LRU有界512（线程安全） */
    private static final LRUCache<Field, FieldMeta> FIELD_META_CACHE = CacheUtil.newLRUCache(512);

    private static final Method HUTOOL_TO_BEAN = init("cn.hutool.json.JSONUtil", "toBean", String.class, Class.class);
    private static final Method HUTOOL_TO_LIST = init("cn.hutool.json.JSONUtil", "toList", String.class, Class.class);
    private static final Method HUTOOL_TO_JSONSTR = init("cn.hutool.json.JSONUtil", "toJsonStr", Object.class);
    private static final Object JACKSON_MAPPER = initMapper();
    private static final Method JACKSON_READ_CLASS = initJacksonRead();
    private static final Method JACKSON_WRITE_STR = initJacksonWrite();

    private static Method init(String cls, String m, Class<?>... pt) {
        try {
            return Class.forName(cls).getMethod(m, pt);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object initMapper() {
        try {
            return Class.forName("com.fasterxml.jackson.databind.ObjectMapper").newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static Method initJacksonRead() {
        if (JACKSON_MAPPER == null) return null;
        try {
            return JACKSON_MAPPER.getClass().getMethod("readValue", String.class, Class.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static Method initJacksonWrite() {
        if (JACKSON_MAPPER == null) return null;
        try {
            return JACKSON_MAPPER.getClass().getMethod("writeValueAsString", Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object toBeanCompatible(String text, Field field) {
        // null不进缓存
        if (text == null) {
            return null;
        }
        FieldMeta meta = metaOf(field);
        switch (meta.kind) {
            case STRING_OR_OBJECT:
                return text;
            case SIMPLE:
                return parseSimple(text, meta.target);
            case ENUM: {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object e = Enum.valueOf((Class<Enum>) meta.target, text);
                return e;
            }
            case ARRAY: {
                List<?> list = fromList(text, meta.elemOrComp);
                Object arr = Array.newInstance(meta.elemOrComp, list.size());
                for (int i = 0; i < list.size(); i++) Array.set(arr, i, list.get(i));
                return arr;
            }
            case COLLECTION:
                return fromCollection(text, meta);
            case BEAN:
            default:
                return fromBean(text, meta.target);
        }
    }

    /** 同一Field的分支判断只做一次：Kind + 预解析好的目标/元素类型 */
    private enum Kind {
        STRING_OR_OBJECT, SIMPLE, ENUM, ARRAY, COLLECTION, BEAN
    }

    private static final class FieldMeta {
        final Kind kind;
        final Class<?> target;
        final Class<?> elemOrComp;

        FieldMeta(Kind kind, Class<?> target, Class<?> elemOrComp) {
            this.kind = kind;
            this.target = target;
            this.elemOrComp = elemOrComp;
        }
    }

    /** 反向：把bean中该字段的值转成可落库的json/字符串（toBeanCompatible的逆操作） */
    public static String toJsonString(Object bean, Field field) {
        Object value = getFieldValue(bean, field);
        if (value == null) {
            return null;
        }
        FieldMeta meta = metaOf(field);
        switch (meta.kind) {
            case STRING_OR_OBJECT:
                // String直接存；Object里若已是String也直接存，否则走json库
                return value instanceof String ? (String) value : toJson(value);
            case SIMPLE:
                return String.valueOf(value);
            case ENUM:
                return ((Enum<?>) value).name();
            case ARRAY:
            case COLLECTION:
            case BEAN:
            default:
                return toJson(value);
        }
    }

    public static boolean isCompatible(Field field) {
        FieldMeta meta = metaOf(field);
        return meta.kind != Kind.STRING_OR_OBJECT && meta.kind != Kind.SIMPLE;
    }

    /** 统一走LRU缓存的策略入口：同一Field只判断一次 */
    private static FieldMeta metaOf(Field field) {
        FieldMeta meta = FIELD_META_CACHE.get(field);
        if (meta == null) {
            meta = resolve(field);
            FIELD_META_CACHE.put(field, meta);
        }
        return meta;
    }

    private static Object getFieldValue(Object bean, Field field) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            field.setAccessible(true);
            try {
                return field.get(bean);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static FieldMeta resolve(Field field) {
        Class<?> type = field.getType();
        Type generic = field.getGenericType();
        // String/Object：直接返回原文
        if (type == String.class || type == Object.class) {
            return new FieldMeta(Kind.STRING_OR_OBJECT, type, null);
        }
        // 基本类型+包装：不走json库，直接parse
        if (isSimple(type)) {
            return new FieldMeta(Kind.SIMPLE, type, null);
        }
        // 枚举
        if (type.isEnum()) {
            return new FieldMeta(Kind.ENUM, type, null);
        }
        // 数组（byte[]除外）
        if (type.isArray() && type != byte[].class) {
            return new FieldMeta(Kind.ARRAY, type, type.getComponentType());
        }
        // 集合：取泛型 List<Foo> -> Foo（只解析一次）
        if (Collection.class.isAssignableFrom(type)) {
            Class<?> elem = Object.class;
            if (generic instanceof ParameterizedType) {
                Type arg = ((ParameterizedType) generic).getActualTypeArguments()[0];
                if (arg instanceof Class) elem = (Class<?>) arg;
            }
            return new FieldMeta(Kind.COLLECTION, type, elem);
        }
        // Map/Bean：走json库
        return new FieldMeta(Kind.BEAN, type, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T fromBean(String json, Class<T> clazz) {
        if (HUTOOL_TO_BEAN != null) {
            try {
                return (T) HUTOOL_TO_BEAN.invoke(null, json, clazz);
            } catch (Exception ignore) {
            }
        }
        if (JACKSON_MAPPER != null && JACKSON_READ_CLASS != null) {
            try {
                return (T) JACKSON_READ_CLASS.invoke(JACKSON_MAPPER, json, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
        }
        throw new RuntimeException("无hutool/jackson可做反序列化");
    }

    private static Collection<?> fromCollection(String json, FieldMeta meta) {
        Collection<Object> result = newCollection(meta.target);
        try {
            result.addAll(fromList(json, meta.elemOrComp));
            return result;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("无法填充集合类型: " + meta.target.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollection(Class<?> target) {
        Class<?> implementation = target;
        if (target.isInterface() || Modifier.isAbstract(target.getModifiers())) {
            // 仅采用能赋给声明类型的默认实现，自定义子接口不可退化为父类型。
            Class<?>[] candidates = {ArrayList.class, LinkedHashSet.class, TreeSet.class, LinkedList.class};
            implementation = null;
            for (Class<?> candidate : candidates) {
                if (target.isAssignableFrom(candidate)) {
                    implementation = candidate;
                    break;
                }
            }
        }
        if (implementation == null) {
            throw new IllegalArgumentException("无法构造集合类型: " + target.getName());
        }
        try {
            return (Collection<Object>) implementation.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalArgumentException("无法构造集合类型: " + target.getName()
                    + "，需要可访问的无参构造器", e);
        }
    }

    public static String toJson(Object value) {
        if (HUTOOL_TO_JSONSTR != null) {
            try {
                return (String) HUTOOL_TO_JSONSTR.invoke(null, value);
            } catch (Exception ignore) {
            }
        }
        if (JACKSON_MAPPER != null && JACKSON_WRITE_STR != null) {
            try {
                return (String) JACKSON_WRITE_STR.invoke(JACKSON_MAPPER, value);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
        }
        throw new RuntimeException("无hutool/jackson可做序列化");
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> fromList(String json, Class<T> elem) {
        if (HUTOOL_TO_LIST != null) {
            try {
                return (List<T>) HUTOOL_TO_LIST.invoke(null, json, elem);
            } catch (Exception ignore) {
            }
        }
        // jackson降级：constructCollectionType
        try {
            Object tf = JACKSON_MAPPER.getClass().getMethod("getTypeFactory").invoke(JACKSON_MAPPER);
            Object javaType = tf.getClass().getMethod("constructCollectionType", Class.class, Class.class)
                    .invoke(tf, List.class, elem);
            Method m = JACKSON_MAPPER.getClass().getMethod("readValue", String.class,
                    Class.forName("com.fasterxml.jackson.databind.JavaType"));
            return (List<T>) m.invoke(JACKSON_MAPPER, json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private static boolean isSimple(Class<?> c) {
        return (c.isPrimitive() && c != void.class)
                || c == Boolean.class
                || c == Character.class
                || c == Byte.class || c == Short.class || c == Integer.class || c == Long.class
                || c == Float.class || c == Double.class
                || c == Timestamp.class || c == java.util.Date.class || c == Date.class || c == Time.class
                || c == LocalDateTime.class || c == LocalDate.class || c == LocalTime.class
                || c == BigDecimal.class || c == BigInteger.class
                || c == AtomicInteger.class || c == AtomicLong.class
                || c == StringBuilder.class || c == StringBuffer.class;
    }

    private static Object parseSimple(String v, Class<?> c) {
        if (c == int.class || c == Integer.class) return Integer.valueOf(v);
        if (c == long.class || c == Long.class) return Long.valueOf(v);
        if (c == double.class || c == Double.class) return Double.valueOf(v);
        if (c == float.class || c == Float.class) return Float.valueOf(v);
        if (c == short.class || c == Short.class) return Short.valueOf(v);
        if (c == byte.class || c == Byte.class) return Byte.valueOf(v);
        if (c == boolean.class || c == Boolean.class) return SetBooleanField.parse(v);
        if (c == char.class || c == Character.class) return v.isEmpty() ? '\0' : v.charAt(0);
        if (c == java.math.BigDecimal.class) return new java.math.BigDecimal(v);
        if (c == java.math.BigInteger.class) return new java.math.BigInteger(v);
        if (c == java.util.Date.class) {
            // 保持已有Date.toString()存储格式；解析器局部创建以保证线程安全。
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            format.setLenient(false);
            ParsePosition position = new ParsePosition(0);
            java.util.Date date = format.parse(v, position);
            if (date == null || position.getIndex() != v.length()) {
                throw new IllegalArgumentException("无效的Date文本，期望格式EEE MMM dd HH:mm:ss zzz yyyy: " + v);
            }
            return date;
        }
        if (c == Date.class) return Date.valueOf(v);
        if (c == Time.class) return Time.valueOf(v);
        if (c == Timestamp.class) return Timestamp.valueOf(v);
        if (c == LocalDate.class) return LocalDate.parse(v);
        if (c == LocalTime.class) return LocalTime.parse(v);
        if (c == LocalDateTime.class) return LocalDateTime.parse(v);
        if (c == StringBuilder.class) return new StringBuilder(v);
        if (c == StringBuffer.class) return new StringBuffer(v);
        if (c == AtomicInteger.class) return new AtomicInteger(Integer.parseInt(v));
        if (c == AtomicLong.class) return new AtomicLong(Long.parseLong(v));
        throw new IllegalArgumentException("不支持的简单类型: " + c.getName());
    }

    private static Object defaultForPrimitive(Class<?> c) {
        if (c == boolean.class) return false;
        if (c == char.class) return '\0';
        if (c == byte.class) return (byte) 0;
        if (c == short.class) return (short) 0;
        if (c == int.class) return 0;
        if (c == long.class) return 0L;
        if (c == float.class) return 0F;
        if (c == double.class) return 0D;
        return null;
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return s;
    }
}
