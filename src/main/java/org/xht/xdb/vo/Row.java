package org.xht.xdb.vo;

import org.xht.xdb.util.MapUtil;

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.function.*;

/**
 * key: jdbc查询的情况下默认大写下划线命名方式： USER_ID
 */
@SuppressWarnings("unused")
public class Row extends HashMap<String, Object> {

    public Row(int i) {
        super(i);
    }

    public Row() {
        super();
    }

    public static Row init() {
        return new Row();
    }

    public static Row of(Map<String, Object> map) {
        Row row = new Row();
        if (map != null && !map.isEmpty()) {
            row.putAll(map);
        }
        return row;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Row formatKey(String keyFormats) {
        if (keyFormats == null || keyFormats.isEmpty())
            return this;
        // 根据指定的key格式创建别名映射表
        Map<String, String> mapOldNew = new HashMap<>();
        String[] split = keyFormats.split(",");
        for (String s : split) {
            String[] split1 = s.split("=");
            mapOldNew.put(split1[0], split1[1]);
        }
        // 获取别名映射表的key集合和原始Map对象的key集合
        Set<String> aliasesKeySet = mapOldNew.keySet();

        // 遍历原始Map对象中的所有键值对，并根据别名映射表将key进行重命名
        mapOldNew.forEach((oldKey, newKey) -> {
            if (containsKey(oldKey)) {
                put(newKey, get(oldKey));
                remove(oldKey);
            }
        });
        return this;
    }

    public Row set(String k, Object v) {
        put(k, v);
        return this;
    }

    public String getString(String key, String defaultValue) {
        return getAs(key, defaultValue, Object::toString);
    }

    public Long getLong(String key, Long defaultValue) {
        return getAs(key, defaultValue, e -> Long.valueOf(e.toString()));
    }

    public Double getDouble(String key, Double defaultValue) {
        return getAs(key, defaultValue, e -> Double.valueOf(e.toString()));
    }

    public Float getFloat(String key, Float defaultValue) {
        return getAs(key, defaultValue, e -> Float.valueOf(e.toString()));
    }

    public BigDecimal getFloat(String key, BigDecimal defaultValue) {
        return getAs(key, defaultValue, e -> new BigDecimal(e.toString()));
    }

    public BigInteger getFloat(String key, BigInteger defaultValue) {
        return getAs(key, defaultValue, e -> new BigInteger(e.toString()));
    }

    public Integer getInt(String key, Integer defaultValue) {
        return getAs(key, defaultValue, e -> Integer.valueOf(e.toString()));
    }

    public Timestamp getTimestamp(String key, Timestamp defaultValue) {
        return getAs(key, defaultValue, e -> (Timestamp) e);
    }

    public <T> T getAs(String key, T defaultValue, Function<Object, T> format) {
        return Optional.ofNullable(get(key)).map(format).orElse(defaultValue);
    }

    public <T> T getIgnoreError(String key, T defaultValue, Function<Object, T> format) {
        T t = defaultValue;
        try {
            t = getAs(key, defaultValue, format);
        } catch (Exception ignored) {
        }
        return t;
    }

    public MapUtil<Object> toMapUtil() {
        MapUtil<Object> init = MapUtil.init();
        this.forEach(init::add);
        return init;
    }

    public MapUtil<Object> toMapUtil(boolean ignoreNulls) {
        MapUtil<Object> init = MapUtil.init();
        this.forEach((key, value) -> {
            if (ignoreNulls) {
                init.addOnlyNotNull(key, value);
            } else {
                init.add(key, value);
            }
        });
        return init;
    }
}
