package org.xht.xdb.util;

import cn.hutool.core.lang.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "unchecked"})
@Slf4j
public class MapUtil<T> implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, T> self;

    public MapUtil() {
        this.self = new HashMap<>();
    }

    public static MapUtil<Object> init() {
        MapUtil<Object> maps = new MapUtil<>();
        maps.self = new HashMap<>();
        return maps;
    }

    public static <T> MapUtil<T> init(Class<T> tClass) {
        MapUtil<T> maps = new MapUtil<>();
        maps.self = new HashMap<>();
        return maps;
    }

    public static <T> MapUtil<T> init(TypeReference<T> tClass) {
        MapUtil<T> maps = new MapUtil<>();
        maps.self = new HashMap<>();
        return maps;
    }

    public MapUtil<T> add(String key, T value) {
        self.put(key, value);
        return this;
    }

    public MapUtil<T> addIf(String key, T value, Supplier<Boolean> condition) {
        if (condition.get()) {
            self.put(key, value);
        }
        return this;
    }

    public MapUtil<T> addIf(String key, T value, boolean condition) {
        if (condition) {
            self.put(key, value);
        }
        return this;
    }

    public MapUtil<T> addJoinString(String key, String joinString, String splitFlag) {
        String[] split = joinString.split(splitFlag);
        List<String> args = new ArrayList<>();
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                args.add(s);
            }
        }
        self.put(key, (T) args);
        return this;
    }

    public MapUtil<T> addOnlyNotNull(String key, T value) {
        if (value != null) {
            if (value instanceof String && ((String) value).isEmpty()) {
                return this;
            } else if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                return this;
            } else if (value.getClass().isArray() && Array.getLength(value) == 0) {
                return this;
            }
            self.put(key, value);
        }
        return this;
    }

    public MapUtil<T> addOnlyNotNullJoinString(String key, String joinString, String splitFlag) {
        if (joinString == null ||
                joinString.isEmpty() ||
                splitFlag == null ||
                splitFlag.isEmpty() ||
                key == null ||
                key.isEmpty())
            return this;
        String[] split = joinString.split(splitFlag);
        List<String> args = new ArrayList<>();
        for (String s : split) {
            if (s != null && !s.isEmpty()) {
                args.add(s);
            }
        }
        self.put(key, (T) args);
        return this;
    }

    public MapUtil<T> del(String key) {
        self.remove(key);
        return this;
    }

    public MapUtil<T> clone() {
        try {
            @SuppressWarnings("unchecked") MapUtil<T> r = (MapUtil<T>) super.clone();
            r.self = new HashMap<>(self); // 需要进行深拷贝
            return r;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public MapUtil<T> clone(String... keys) {
        MapUtil<T> r;
        try {
            r = (MapUtil<T>) super.clone();
            Set<String> _keys = Arrays.stream(keys).collect(Collectors.toSet());
            List<String> delKeys = r.keySet().stream().filter(e -> !_keys.contains(e)).collect(Collectors.toList());
            for (String delKey : delKeys) {
                r.del(delKey);
            }
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        return r;
    }

    public T get(String key) {
        return self.get(key);
    }

    public Set<String> keySet() {
        return self.keySet();
    }

    @Override
    public String toString() {
        return self.toString();
    }

    public Map<String, T> value() {
        return self;
    }

    public static <T> MapUtil<T> clone(Map<String, T> map) {
        MapUtil<T> r = new MapUtil<>();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            r.add(key, map.get(key));
        }
        return r;
    }

    /**
     * 将Map中的key按照指定格式进行重命名，并返回新的Map对象。
     *
     * @param map1       原始的Map对象
     * @param keyFormats 指定的key重命名格式，例如："oldKey1=newKey1,oldKey2=newKey2"
     * @param <V>        Map中value的类型
     * @return 返回重命名后的新Map对象
     */
    public static <V> Map<String, V> formatKey(Map<String, V> map1, String keyFormats) {
        // 根据指定的key格式创建别名映射表
        Map<String, String> aliasesMap = new HashMap<>();
        String[] split = keyFormats.split(",");
        for (String s : split) {
            String[] split1 = s.split("=");
            aliasesMap.put(split1[0], split1[1]);
        }

        // 获取别名映射表的key集合和原始Map对象的key集合
        Set<String> aliasesKeySet = aliasesMap.keySet();
        Set<String> keySet = map1.keySet();

        // 创建一个新的Map对象，用于存储重命名后的键值对
        Map<String, V> map2 = new HashMap<>();

        // 遍历原始Map对象中的所有键值对，并根据别名映射表将key进行重命名
        for (String key : keySet) {
            if (aliasesKeySet.contains(key)) {
                // 如果别名映射表中包含该key，则将其重命名为指定的新key，并添加到新Map对象中
                map2.put(aliasesMap.get(key), map1.get(key));
            } else {
                // 如果别名映射表中不包含该key，则直接添加到新Map对象中
                map2.put(key, map1.get(key));
            }
        }

        // 返回重命名后的新Map对象
        return map2;
    }

    /**
     * @param keyFormats 指定的key重命名格式，例如："oldKey1=newKey1,oldKey2=newKey2"
     */
    public MapUtil<T> formatKey(String keyFormats) {
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
            if (self.containsKey(oldKey)) {
                self.put(newKey, self.get(oldKey));
                self.remove(oldKey);
            }
        });
        return this;
    }

    public MapUtil<T> debug() {
        log.info("MapUtil: {}", self);
        return this;
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1");
        MapUtil<Object> clone = MapUtil.clone(map);
        System.out.println(clone);
    }

    public MapUtil<T> addAll(MapUtil<T> args) {
        self.putAll(args.value());
        return this;
    }
}
