package org.xht.xdb.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
@NoArgsConstructor
@Slf4j
public class PageResult<T> {
    private long count = 0;
    @NonNull
    private List<T> items;

    public PageResult(Long count, List<T> items) {
        this.count = Optional.ofNullable(count).orElse(0L);
        this.items = items == null ? new ArrayList<>() : items;
    }

    public PageResult<T> debug() {
        items.forEach(item -> log.info("{}", item));
        log.info("count: {}", count);
        return this;
    }

    public Row as(String countKey, String itemsKey) {
        return Row.init().set(countKey, count).set(itemsKey, items);
    }

    public <V> V as(BiFunction<Long, List<T>, V> function) {
        return function.apply(this.count, this.items);
    }

    public <V> V as(Function<PageResult<T>, V> function) {
        return function.apply(this);
    }
}
