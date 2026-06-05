package org.xht.xdb.util;

import java.util.*;
import java.util.function.Function;

public class BatchUtil {

    public static <R, E> List<R> list(Collection<E> inputs, int batchSize, Function<Collection<E>, Collection<R>> mapping) {
        return collection(inputs, batchSize, mapping, new ArrayList<>(inputs.size()));
    }

    public static <R, E> Set<R> set(Collection<E> inputs, int batchSize, Function<Collection<E>, Collection<R>> mapping) {
        return collection(inputs, batchSize, mapping, new HashSet<>(inputs.size()));
    }

    public static <R, E, C extends Collection<R>> C collection(Collection<E> inputs, int batchSize, Function<Collection<E>, Collection<R>> mapping, C outputs) {
        List<E> batch = new ArrayList<>(batchSize);

        for (E input : inputs) {
            batch.add(input);
            if (batch.size() == batchSize) {
                outputs.addAll(mapping.apply(batch));
                batch = new ArrayList<>(batchSize);  // 新批次
            }
        }
        // 处理最后不足一批的剩余元素
        if (!batch.isEmpty()) {
            outputs.addAll(mapping.apply(batch));
        }
        return outputs;
    }
}
