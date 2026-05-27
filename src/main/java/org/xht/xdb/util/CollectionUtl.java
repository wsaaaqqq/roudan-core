package org.xht.xdb.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CollectionUtl {
    public static <T> void split(Collection<T> list, int batchSize, Consumer<List<T>> consumer) {
        int i = 0;
        List<T> batchList = new ArrayList<>();
        for (T t : list) {
            if (i < batchSize) {
                batchList.add(t);
                i++;
            } else {
                consumer.accept(batchList);
                batchList = new ArrayList<>();
                batchList.add(t);
                i = 1;
            }
        }
        if (!batchList.isEmpty()) {
            consumer.accept(batchList);
        }
    }

    public static <T> List<List<T>> split(Collection<T> list, int batchSize) {
        List<List<T>> all = new ArrayList<>();
        split(list, batchSize, (all::add));
        return all;
    }
}
