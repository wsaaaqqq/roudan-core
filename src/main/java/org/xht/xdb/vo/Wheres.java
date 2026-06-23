package org.xht.xdb.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.xht.xdb.util.MapUtil;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Data
@NoArgsConstructor
public class Wheres {

    private List<Wheres> parts = new ArrayList<>();
    private List<Wheres> orders = new ArrayList<>();
    private MapUtil<Object> args = new MapUtil<>();
    private Number pageIndex;
    private Number pageSize;
    private boolean and = true;
    private String sqlPart = "";

    public MapUtil<Object> getArgs() {
        if (parts != null && !parts.isEmpty()) {
            for (Wheres part : parts) {
                args.addAll(part.getArgs());
            }
        }
        return args;
    }

    public static Wheres init() {
        return new Wheres();
    }

    public Wheres orderByAsc(String order, boolean nullsLast) {
        orders.add(new Wheres(order + " asc " + (nullsLast ? " nulls last " : "")));
        return this;
    }

    public Wheres orderByDesc(String colName, boolean nullsLast) {
        orders.add(new Wheres(colName + " desc " + (nullsLast ? " nulls last " : "")));
        return this;
    }

    public Wheres and() {
        this.and = true;
        return this;
    }

    public Wheres or() {
        this.and = false;
        return this;
    }

    public Wheres sub(Consumer<Wheres> consumer) {
        Wheres sub = new Wheres();
        consumer.accept(sub);
        parts.add(sub);
        return this;
    }

    private Wheres(String sqlPart) {
        this.sqlPart = sqlPart;
    }

    public void debug() {
        System.out.println("getWhereSql: \n" + getWhereSql());
        System.out.println("args: \n" + getArgs());
    }

    public String getWhereSql() {
        if (parts == null || parts.isEmpty()) return "";
        StringBuilder whereSql = new StringBuilder(" where \n");
        String join = getWhereSqlPart(parts, and);
        whereSql.append(join);
        String where = whereSql.toString();
        String orders = getOrdersSql();
        return where + orders;
    }

    private String getOrdersSql() {
        if (orders == null || orders.isEmpty()) {
            return "";
        }
        return " order by " + orders.stream().map(wheres -> wheres.sqlPart).collect(Collectors.joining(", "));
    }

    private String getWhereSqlPart(List<Wheres> parts, boolean and) {
        if (parts == null || parts.isEmpty()) return "";
        String delimiter;
        if (and) {
            delimiter = " and ";
        } else {
            delimiter = " or ";
        }
        List<String> list = new ArrayList<>();
        for (Wheres w : parts) {
            List<Wheres> subPart = w.getParts();
            if (subPart == null || subPart.isEmpty()) {
                list.add(w.getSqlPart());
            } else if (subPart.size() == 1) {
                list.add(getWhereSqlPart(subPart, w.and));
            } else {
                list.add(String.format(" ( %s ) \n ", getWhereSqlPart(subPart, w.and)));
            }
        }
        return String.join(delimiter, list);
    }

    public String key(String colName) {
        return colName + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    public Wheres between(String colName, Object start, Object end, boolean containEqual) {
        return between(colName, start, end, containEqual, notEmpty(start), notEmpty(end));
    }

    public Wheres between(String colName, Object start, Object end, boolean containEqual, Supplier<Boolean> conditionStart, Supplier<Boolean> conditionEnd) {
        return between(colName, start, end, containEqual, conditionStart.get(), conditionEnd.get());
    }

    public Wheres between(String colName, Object start, Object end, boolean containEqual, boolean conditionStart, boolean conditionEnd) {
        String ge = containEqual ? ">=" : ">";
        String le = containEqual ? "<=" : "<";
        if (conditionStart) {
            String key = key(colName);
            parts.add(new Wheres(colName + " " + ge + " :" + key + " \n "));
            args.add(key, start);
        }
        if (conditionEnd) {
            String key = key(colName);
            parts.add(new Wheres(colName + " " + le + " :" + key + " \n "));
            args.add(key, end);
        }
        return this;
    }

    public Wheres contain(String colName, Object value) {
        return contain(colName, value, notEmpty(value));
    }

    public Wheres anyColContain(Object value, String... colNames) {
        return anyColContain(value, notEmpty(value), colNames);
    }

    public Wheres contain(String colName, Object value, Supplier<Boolean> condition) {
        return contain(colName, value, condition.get());
    }

    public Wheres anyColContain(Object value, Supplier<Boolean> condition, String... colNames) {
        return anyColContain(value, condition.get(), colNames);
    }

    public Wheres contain(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " like '%' || :" + key + " || '%' \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres anyColContain(Object value, boolean condition, String... colNames) {
        if (condition && colNames.length > 0) {
            Wheres sub = new Wheres();
            sub.or();
            for (String colName : colNames) {
                String key = key(colName);
                sub.parts.add(new Wheres(colName + " like '%' || :" + key + " || '%' "));
                sub.args.add(key, value);
            }
            parts.add(sub);
        }
        return this;
    }

    public Wheres endWith(String colName, Object value) {
        return endWith(colName, value, notEmpty(value));
    }

    public static boolean notEmpty(Object value) {
        return !isEmptyValue(value);
    }

    public static boolean isEmptyValue(Object value) {
        if (value == null) return true;
        if (value instanceof String) return ((String) value).isEmpty();
        if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
        if (value instanceof Map) return ((Map<?, ?>) value).isEmpty();
        if (value instanceof StringBuilder) return ((StringBuilder) value).length() == 0;
        if (value instanceof StringBuffer) return ((StringBuffer) value).length() == 0;
        if (value.getClass().isArray()) return Array.getLength(value) == 0;
        return false; // 非空值
    }

    public Wheres endWith(String colName, Object value, Supplier<Boolean> condition) {
        return endWith(colName, value, condition.get());
    }

    public Wheres endWith(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " like '%' || :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres eq(String colName, Object value) {
        return eq(colName, value, notEmpty(value));
    }

    public Wheres eq(String colName, Object value, Supplier<Boolean> condition) {
        return eq(colName, value, condition.get());
    }

    public Wheres eq(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " = :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres ne(String colName, Object value) {
        return ne(colName, value, notEmpty(value));
    }

    public Wheres ne(String colName, Object value, Supplier<Boolean> condition) {
        return ne(colName, value, condition.get());
    }

    public Wheres ne(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " <> :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres ge(String colName, Object value) {
        return ge(colName, value, notEmpty(value));
    }

    public Wheres ge(String colName, Object value, Supplier<Boolean> condition) {
        return ge(colName, value, condition.get());
    }

    public Wheres ge(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " >= :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres gt(String colName, Object value) {
        return gt(colName, value, notEmpty(value));
    }

    public Wheres gt(String colName, Object value, Supplier<Boolean> condition) {
        return gt(colName, value, condition.get());
    }

    public Wheres gt(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " > :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Wheres in(String colName, Collection value) {
        return in(colName, value, notEmpty(value));
    }

    public Wheres inJoinString(String colName, String joinString) {
        return inJoinString(colName, joinString, ",");
    }

    public Wheres inJoinString(String colName, String joinString, String split) {
        if (joinString == null || joinString.isEmpty()) return this;
        if (split == null || split.isEmpty()) return this;
        String[] arr = joinString.split(split);
        if (arr.length > 1) {
            return in(colName, arr, true);
        } else if (arr.length == 1) {
            return eq(colName, arr[0]);
        }
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Wheres in(String colName, Collection value, Supplier<Boolean> condition) {
        return in(colName, value, condition.get());
    }

    @SuppressWarnings("rawtypes")
    public Wheres in(String colName, Collection value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " in ( :" + key + " ) \n"));
            args.add(key, value);
        }
        return this;
    }

    public <T> Wheres in(String colName, T[] value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " in ( :" + key + " ) \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres isNull(String colName) {
        return isNull(colName, true);
    }

    public Wheres isNull(String colName, Supplier<Boolean> condition) {
        return isNull(colName, condition.get());
    }

    public Wheres isNull(String colName, boolean condition) {
        if (condition) {
            parts.add(new Wheres(colName + " is null \n"));
        }
        return this;
    }

    public Wheres le(String colName, Object value) {
        return le(colName, value, notEmpty(value));
    }

    public Wheres le(String colName, Object value, Supplier<Boolean> condition) {
        return le(colName, value, condition.get());
    }

    public Wheres le(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " <= :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres lt(String colName, Object value) {
        return lt(colName, value, notEmpty(value));
    }

    public Wheres lt(String colName, Object value, Supplier<Boolean> condition) {
        return lt(colName, value, condition.get());
    }

    public Wheres lt(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " < :" + key + " \n"));
            args.add(key, value);
        }
        return this;
    }

    @SuppressWarnings("rawtypes")
    public Wheres notIn(String colName, Collection value) {
        return notIn(colName, value, notEmpty(value));
    }

    @SuppressWarnings("rawtypes")
    public Wheres notIn(String colName, Collection value, Supplier<Boolean> condition) {
        return notIn(colName, value, condition.get());
    }

    @SuppressWarnings("rawtypes")
    public Wheres notIn(String colName, Collection value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " not in ( :" + key + " ) \n"));
            args.add(key, value);
        }
        return this;
    }

    public Wheres notNull(String colName) {
        return notNull(colName, true);
    }

    public Wheres notNull(String colName, Supplier<Boolean> condition) {
        return notNull(colName, condition.get());
    }

    public Wheres notNull(String colName, boolean condition) {
        if (condition) {
            parts.add(new Wheres(colName + " is not null \n"));
        }
        return this;
    }

    /**
     * 第几页
     *
     * @param pageIndex 序号从1起始
     * @return {@link Wheres }
     */
    public Wheres pageIndex(Number pageIndex) {
        this.pageIndex = pageIndex;
        return this;
    }

    public Wheres pageSize(Number pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Wheres startWith(String colName, Object value) {
        return startWith(colName, value, notEmpty(value));
    }

    public Wheres startWith(String colName, Object value, Supplier<Boolean> condition) {
        return startWith(colName, value, condition.get());
    }

    public Wheres startWith(String colName, Object value, boolean condition) {
        if (condition) {
            String key = key(colName);
            parts.add(new Wheres(colName + " like :" + key + " || '%' \n"));
            args.add(key, value);
        }
        return this;
    }

}


