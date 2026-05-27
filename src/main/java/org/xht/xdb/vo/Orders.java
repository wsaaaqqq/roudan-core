package org.xht.xdb.vo;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Orders<T> {
    private Comparator<T> comparator;

    public <U extends Comparable<? super U>> Orders<T> with(Function<? super T, U> getter, boolean asc,
                                                            boolean nullsLast) {
        Comparator<U> _nullsLast = nullsLast ? Comparator.nullsLast(Comparator.naturalOrder()) : Comparator.nullsFirst(
                Comparator.naturalOrder());
        if (!asc) {
            _nullsLast = _nullsLast.reversed();
        }
        if (comparator == null) {
            comparator = Comparator.comparing(getter, _nullsLast);
        } else {
            comparator = comparator.thenComparing(getter, _nullsLast);
        }
        return this;
    }

    public Orders<T> withCN(Function<? super T, String> getter, boolean asc, boolean nullsLast) {
        Comparator<String> _nullsLast;
        if (asc) {
            if (nullsLast) {
                _nullsLast = Comparator.nullsLast(Collator.getInstance(Locale.CHINA));
            } else {
                _nullsLast = Comparator.nullsFirst(Collator.getInstance(Locale.CHINA));
            }
        } else {
            if (nullsLast) {
                _nullsLast = Comparator.nullsLast(Collator.getInstance(Locale.CHINA).reversed());
            } else {
                _nullsLast = Comparator.nullsFirst(Collator.getInstance(Locale.CHINA).reversed());
            }
        }

        if (comparator == null) {
            comparator = Comparator.comparing(getter, _nullsLast);
        } else {
            comparator = comparator.thenComparing(getter, _nullsLast);
        }
        return this;
    }

    public static <T> Orders<T> of(Class<T> tClass) {
        return new Orders<>();
    }

    public Comparator<T> value() {
        return this.comparator;
    }

}
