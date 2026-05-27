package org.xht.xdb.vo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

/**
 * key: 大写
 */
@SuppressWarnings("unused")
public class RowMap<K, V> extends HashMap<K, V> {
    public static <K, V> RowMap<K, V> of(HashMap<K, V> map) {
        RowMap<K, V> rowMap = new RowMap<>();
        if (map != null && !map.isEmpty()) {
            rowMap.putAll(map);
        }
        return rowMap;
    }

    public static <K, V> RowMap<K, V> init(Class<K> kClass, Class<V> vClass) {
        return new RowMap<>();
    }

    public RowMap(int i) {
        super(i);
    }

    public RowMap() {
        super();
    }

    public RowMap<K, V> set(K k, V v) {
        put(k, v);
        return this;
    }

    public String getString(K key, String defaultValue) {
        return getAs(key, defaultValue, Object::toString);
    }

    public Long getLong(K key, Long defaultValue) {
        return getAs(key, defaultValue, e -> Long.valueOf(e.toString()));
    }

    public Double getDouble(K key, Double defaultValue) {
        return getAs(key, defaultValue, e -> Double.valueOf(e.toString()));
    }

    public Float getFloat(K key, Float defaultValue) {
        return getAs(key, defaultValue, e -> Float.valueOf(e.toString()));
    }

    public BigDecimal getFloat(K key, BigDecimal defaultValue) {
        return getAs(key, defaultValue, e -> new BigDecimal(e.toString()));
    }

    public BigInteger getFloat(K key, BigInteger defaultValue) {
        return getAs(key, defaultValue, e -> new BigInteger(e.toString()));
    }

    public Integer getInt(K key, Integer defaultValue) {
        return getAs(key, defaultValue, e -> Integer.valueOf(e.toString()));
    }

    public Timestamp getTimestamp(K key, Timestamp defaultValue) {
        return getAs(key, defaultValue, e -> (Timestamp) e);
    }

    public <T> T getAs(K key, T defaultValue, Function<Object, T> format) {
        return Optional.ofNullable(get(key))
                .map(format)
                .orElse(defaultValue);
    }

    public <T> T getIgnoreError(K key, T defaultValue, Function<Object, T> format) {
        T t = defaultValue;
        try {
            t = getAs(key, defaultValue, format);
        } catch (Exception ignored) {
        }
        return t;
    }
}
