package org.xht.xdb.util;


import org.xht.xdb.vo.Row;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupUtil {

    @SafeVarargs
    public static <T> Row group(Collection<T> list, Function<T, Object>... getters) {
        return group(list, "%.2f%%", getters);
    }

    public static <T, K> Map<K, T> groupByKey(Collection<T> list, Function<T, K> key) {
        Map<K, T> data = new HashMap<>();
        list.forEach(item -> {
            K k = key.apply(item);
            if (k != null) {
                data.put(k, item);
            }
        });
        return data;
    }

    public static <T, K> Map<K, List<T>> groupBy(Collection<T> list, Function<T, K> key) {
        Map<K, List<T>> data = new HashMap<>();
        list.forEach(item -> {
            K k = key.apply(item);
            if (k != null) {
                List<T> v = data.get(k);
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(item);
                data.put(k, v);
            }
        });
        return data;
    }

    /**
     * 对list，按顺序依次进行多层分组，并统计每层分组的数量和百分比
     * {
     * getter1:{
     * "数量": 60,
     * "百分比": "60.00%",
     * getter2: {
     * "数量": 10,
     * "百分比": "16.67%"
     * }
     * }
     * }
     *
     * @param list          数据列表
     * @param percentFormat 百分比格式化模板，如 "%.2f%%"
     * @param getters       分组字段获取器，按顺序依次分组
     * @param <T>           数据类型
     * @return 分组统计结果
     */
    @SafeVarargs
    public static <T> Row group(Collection<T> list, String percentFormat, Function<T, Object>... getters) {
        Row result = new Row();
        if (list == null || list.isEmpty() || getters == null || getters.length == 0) {
            return result;
        }

        int total = list.size();
        Map<Object, List<T>> grouped = list.stream()
                .collect(Collectors.groupingBy(item -> {
                    Object key = getters[0].apply(item);
                    return key == null ? "null" : key;
                }));

        for (Map.Entry<Object, List<T>> entry : grouped.entrySet()) {
            String key = String.valueOf(entry.getKey());
            List<T> subList = entry.getValue();
            int count = subList.size();
            double pct = (double) count / total * 100;

            Row node = new Row();
            node.set("数量", count);
            node.set("百分比", String.format(percentFormat, pct));

            if (getters.length > 1) {
                Row subResult = group(subList, percentFormat,
                        Arrays.copyOfRange(getters, 1, getters.length));
                subResult.forEach(node::set);
            }

            result.set(key, node);
        }

        return result;
    }
}
