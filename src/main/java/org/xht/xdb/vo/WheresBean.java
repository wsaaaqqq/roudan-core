package org.xht.xdb.vo;

import lombok.Data;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.util.SerializableFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Data
public class WheresBean<T> {

    private Wheres wheres = Wheres.init();
    private final Class<T> beanClass;
    private final List<SerializableFunction<T, ?>> selectList = new ArrayList<>();

    public WheresBean(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public static <T> WheresBean<T> init(Class<T> beanClass) {
        return new WheresBean<>(beanClass);
    }

    public WheresBean<T> and() {
        wheres.and();
        return this;
    }

    public WheresBean<T> or() {
        wheres.or();
        return this;
    }

    public WheresBean<T> sub(Consumer<WheresBean<T>> consumer) {
        WheresBean<T> sub = new WheresBean<>(beanClass);
        consumer.accept(sub);
        wheres.getParts().add(sub.wheres);
        return this;
    }

    public <R> WheresBean<T> select(SerializableFunction<T, R> getter) {
        this.selectList.add(getter);
        return this;
    }

    public String getWhereSql() {
        return wheres.getWhereSql();
    }

    public <R> WheresBean<T> between(SerializableFunction<T, R> getter, Object start, Object end,
                                     boolean includeBoundaries
    ) {
        wheres.between(getColName(getter), start, end, includeBoundaries);
        return this;
    }

    public <R> WheresBean<T> between(SerializableFunction<T, R> getter, Object start, Object end,
                                     boolean includeBoundaries, Supplier<Boolean> conditionStart, Supplier<Boolean> conditionEnd
    ) {
        wheres.between(getColName(getter), start, end, includeBoundaries, conditionStart, conditionEnd);
        return this;
    }

    public <R> WheresBean<T> between(SerializableFunction<T, R> getter, Object start, Object end,
                                     boolean includeBoundaries, boolean conditionStart, boolean conditionEnd
    ) {
        wheres.between(getColName(getter), start, end, includeBoundaries, conditionStart, conditionEnd);
        return this;
    }

    public <R> WheresBean<T> contain(SerializableFunction<T, R> getter, Object value) {
        wheres.contain(getColName(getter), value);
        return this;
    }

    @SafeVarargs
    public final <R> WheresBean<T> anyColContain(Object value, SerializableFunction<T, R>... getters) {
        anyColContain(value, Wheres.notEmpty(value), getters);
        return this;
    }

    public <R> WheresBean<T> contain(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.contain(getColName(getter), value, condition);
        return this;
    }

    @SafeVarargs
    public final <R> WheresBean<T> anyColContain(Object value, Supplier<Boolean> condition,
                                                 SerializableFunction<T, R>... getters
    ) {
        anyColContain(value, condition.get(), getters);
        return this;
    }

    public <R> WheresBean<T> contain(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.contain(getColName(getter), value, condition);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    @SafeVarargs
    public final <R> WheresBean<T> anyColContain(Object value, boolean condition,
                                                 SerializableFunction<T, R>... getters
    ) {
        String[] array = Arrays.stream(getters).map(this::getColName).toArray(String[]::new);
        wheres.anyColContain(value, condition, array);
        return this;
    }

    public <R> WheresBean<T> endWith(SerializableFunction<T, R> getter, Object value) {
        wheres.endWith(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> endWith(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.endWith(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> endWith(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.endWith(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> eq(SerializableFunction<T, R> getter, Object value) {
        wheres.eq(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> eq(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.eq(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> eq(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.eq(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> ne(SerializableFunction<T, R> getter, Object value) {
        wheres.ne(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> ne(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.ne(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> ne(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.ne(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> ge(SerializableFunction<T, R> getter, Object value) {
        wheres.ge(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> ge(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.ge(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> ge(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.ge(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> gt(SerializableFunction<T, R> getter, Object value) {
        wheres.gt(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> gt(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.gt(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> gt(SerializableFunction<T, R> getter, Object value, boolean condition) {
        wheres.gt(getColName(getter), value, condition);
        return this;
    }

    private <R> String getColName(SerializableFunction<T, R> getter) {
        return OrmAnnoUtil.getColNameByGetter(this.beanClass, getter);
    }

    @SuppressWarnings("rawtypes")
    public <R> WheresBean<T> in(SerializableFunction<T, R> getter, Collection value) {
        wheres.in(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> inJoinString(SerializableFunction<T, R> getter, String joinString) {
        wheres.inJoinString(getColName(getter), joinString);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public <R> WheresBean<T> in(SerializableFunction<T, R> getter, Collection value, Supplier<Boolean> condition) {
        wheres.in(getColName(getter), value, condition);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public <R> WheresBean<T> in(SerializableFunction<T, R> getter, Collection value, boolean condition) {
        wheres.in(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> isNull(SerializableFunction<T, R> getter) {
        wheres.isNull(getColName(getter));
        return this;
    }

    public <R> WheresBean<T> isNull(SerializableFunction<T, R> getter, Supplier<Boolean> condition) {
        wheres.isNull(getColName(getter), condition);
        return this;
    }

    public <R> WheresBean<T> isNull(SerializableFunction<T, R> getter, boolean condition) {
        wheres.isNull(getColName(getter), condition);
        return this;
    }

    public <R> WheresBean<T> le(SerializableFunction<T, R> getter, Object value) {
        wheres.le(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> le(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.le(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> lt(SerializableFunction<T, R> getter, Object value) {
        wheres.lt(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> lt(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.lt(getColName(getter), value, condition);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public <R> WheresBean<T> notIn(SerializableFunction<T, R> getter, Collection value) {
        wheres.notIn(getColName(getter), value);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public <R> WheresBean<T> notIn(SerializableFunction<T, R> getter, Collection value, Supplier<Boolean> condition) {
        wheres.notIn(getColName(getter), value, condition);
        return this;
    }

    public <R> WheresBean<T> notNull(SerializableFunction<T, R> getter) {
        wheres.notNull(getColName(getter));
        return this;
    }

    public <R> WheresBean<T> notNull(SerializableFunction<T, R> getter, Supplier<Boolean> condition) {
        wheres.notNull(getColName(getter), condition);
        return this;
    }

    /**
     * 第几页
     *
     * @param pageIndex 序号从1起始
     * @return {@link WheresBean }<{@link T }>
     */
    public WheresBean<T> pageIndex(Number pageIndex) {
        wheres.pageIndex(pageIndex);
        return this;
    }

    public WheresBean<T> pageSize(Number pageSize) {
        wheres.pageSize(pageSize);
        return this;
    }

    public <R> WheresBean<T> startWith(SerializableFunction<T, R> getter, Object value) {
        wheres.startWith(getColName(getter), value);
        return this;
    }

    public <R> WheresBean<T> startWith(SerializableFunction<T, R> getter, Object value, Supplier<Boolean> condition) {
        wheres.startWith(getColName(getter), value, condition);
        return this;
    }

    public MapUtil<?> getArgs() {
        return wheres.getArgs();
    }

    public Number getPageIndex() {
        return wheres.getPageIndex();
    }

    public Number getPageSize() {
        return wheres.getPageSize();
    }
}


