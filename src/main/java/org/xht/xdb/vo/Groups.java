package org.xht.xdb.vo;

import lombok.Getter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
@Getter
public class Groups<T> {
    private final List<Object> getters = new ArrayList<>();

    public Groups<T> with(Function<? super T, Object> getter) {
        getters.add(getter);
        return this;
    }

    public static <T> Groups<T> of(Class<T> tClass) {
        return new Groups<>();
    }

    @SuppressWarnings("unchecked")
    public <U extends Comparable<? super U>> Comparator<T> value() {
        if (getters.isEmpty()) return (o1, o2) -> Collator.getInstance().compare(o1, o2);
        Comparator<T> comparator = null;
        for (Object getter : getters) {
            if (comparator == null) {
                comparator = Comparator.comparing((Function<? super T, U>) getter,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            } else {
                comparator = comparator.thenComparing((Function<? super T, U>) getter,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            }
        }
        return comparator;
    }
}
