package org.xht.xdb.util;

import lombok.SneakyThrows;
import org.xht.xdb.vo.Groups;
import org.xht.xdb.vo.Orders;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class ListUtil {

    public static List<Object[]> listMap2Object(List<Map<String, Object>> maps, String keyOrders) {
        List<Object[]> ret = new ArrayList<>();
        if (keyOrders != null) {
            String[] keys = keyOrders.split(",");
            int len = keys.length;
            for (Map<String, Object> map : maps) {
                Object[] obj = new Object[len];
                if (map != null) {
                    for (int i = 0; i < len; i++) {
                        obj[i] = map.get(keys[i]);
                    }
                }
                ret.add(obj);
            }
        }
        return ret;
    }

    public static <R, T> List<T> batchAndMapping(Collection<R> list, int batchSize,
            Function<Collection<R>, T> mapping
    ) {
        return IntStream
                .range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> mapping.apply(list
                                                     .stream()
                                                     .skip((long) i * batchSize)
                                                     .limit(batchSize)
                                                     .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public static <R, T> List<T> batchAndFlatMapping(Collection<R> list, int batchSize,
            Function<Collection<R>, Collection<T>> mapping
    ) {
        return batchAndMapping(list, batchSize, mapping)
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static <R> void batchAndConsume(Collection<R> rows, int batchSize, Consumer<Collection<R>> batchTask) {
        if (rows == null || rows.isEmpty() || batchSize < 1) {
            return;
        }
        int totalSize = rows.size();
        int fromIndex = 0;
        while (fromIndex < totalSize) {
            List<R> task = rows.stream().skip(fromIndex).limit(batchSize).collect(Collectors.toList());
            batchTask.accept(task);
            fromIndex = Math.min(fromIndex + batchSize, totalSize);
        }
    }

    public static <T> List<List<T>> batchList(List<T> list, int batchSize) {
        return IntStream
                .range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.stream().skip((long) i * batchSize).limit(batchSize).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static <T> List<Collection<T>> batchCollection(Collection<T> list, int batchSize) {
        return IntStream
                .range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.stream().skip((long) i * batchSize).limit(batchSize).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public static <T, F> List<T> oderBy(Collection<T> list, Orders<T> orders) {
        return sort(list, orders);
    }

    public static <T, F> List<T> sort(Collection<T> list, Orders<T> orders) {
        if (list == null || list.isEmpty())
            return new ArrayList<>();
        return list.stream().sorted(orders.value()).collect(Collectors.toList());
    }

    public static <T, U extends Comparable<? super U>> TreeMap<T, List<T>> group(Collection<T> list, Groups<T> groups) {
        TreeMap<T, List<T>> map = new TreeMap<>(groups.value());
        if (list == null || list.isEmpty())
            return map;
        for (T t : list) {
            List<T> orDefault = map.getOrDefault(t, new ArrayList<>());
            orDefault.add(t);
            map.put(t, orDefault);
        }
        return map;
    }

    public static <T, U extends Comparable<? super U>> TreeMap<T, Set<T>> groupSet(Collection<T> list,
            Groups<T> groups
    ) {
        return groupSet(list, groups, new HashSet<>());
    }

    public static <T, U extends Comparable<? super U>> TreeMap<T, Set<T>> groupSet(Collection<T> list, Groups<T> groups,
            Set<T> set
    ) {
        TreeMap<T, Set<T>> map = new TreeMap<>(groups.value());
        if (list == null || list.isEmpty())
            return map;
        for (T t : list) {
            Set<T> orDefault = map.getOrDefault(t, set);
            orDefault.add(t);
            map.put(t, orDefault);
        }
        return map;
    }

    @SuppressWarnings("unused")
    public static <T> List<Set<T>> batchSet(Set<T> list, int batchSize) {
        return IntStream
                .range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.stream().skip((long) i * batchSize).limit(batchSize).collect(Collectors.toSet()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @SafeVarargs
    public static <R> List<R> as(R... args) {
        List<R> list = new ArrayList<>();
        Collections.addAll(list, args);
        return list;
    }

    @SneakyThrows
    @SafeVarargs
    public static <R> Collection<R> addAll(Collection<R> list, R... args) {
        if (list == null)
            list = new ArrayList<>();
        Collections.addAll(list, args);
        return list;
    }

    public static <R> String joinToString(List<R> list) {
        return joinToString(list, " ", "null");
    }

    public static <R> String joinToString(List<R> list, CharSequence s, String textIfNull) {
        List<String> collect = list
                .stream()
                .map(e -> Optional.ofNullable(e).map(Object::toString).orElse(textIfNull == null ? "null" : textIfNull))
                .collect(Collectors.toList())
                ;
        return String.join(s, collect);
    }

    public static Object joinToString(Object[] objects) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, objects);
        return "'" + joinToString(list, "' , '", "null") + "'";
    }
}
