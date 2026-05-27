package org.xht.xdb.util;

import lombok.extern.slf4j.*;
import org.xht.xdb.vo.DataCompareResult;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Slf4j
public class MergeBeanUtil<T> {
    private List<T> dataOld = new ArrayList<>();
    private List<T> dataNew = new ArrayList<>();
    private Set<Function<T, ?>> keyColumns;
    private boolean computeInsert = true;
    private boolean computeUpdate = true;
    private boolean computeDelete = true;

    public DataCompareResult<T> merge() {
        if (keyColumns == null || keyColumns.isEmpty())
            throw new RuntimeException("keyColumns is empty");
        dataOld = Optional.ofNullable(dataOld).orElse(new ArrayList<>());
        dataNew = Optional.ofNullable(dataNew).orElse(new ArrayList<>());
        Map<Set<Object>, T> keyMapOld = toKeyMap(dataOld);
        List<T> dataForInsert = dataForInsertOrUpdate(keyMapOld, dataNew, true, computeInsert);
        List<T> dataForUpdate = dataForInsertOrUpdate(keyMapOld, dataNew, false, computeUpdate);
        List<T> dataForDelete = dataForDelete(dataOld, dataNew);
        return new DataCompareResult<>(dataForInsert, dataForUpdate, dataForDelete);
    }

    private List<T> dataForDelete(List<T> dataOld, List<T> rowsNew) {
        List<T> dataForDelete = new ArrayList<>();
        if (dataOld == null || dataOld.isEmpty())
            return dataForDelete;
        if (computeDelete) {
            Map<Set<Object>, T> keyMapNew = toKeyMap(rowsNew);
            dataForDelete = dataOld
                    .stream()
                    .filter(T -> !keyMapNew.containsKey(keyColumnValuesFunction(T)))
                    .collect(Collectors.toList());
        }
        return dataForDelete;
    }

    private List<T> dataForInsertOrUpdate(Map<Set<Object>, T> rowsOldMap, List<T> rowsNew, boolean forInsert,
            boolean compute
    ) {
        List<T> dataForInsertOrUpdate = new ArrayList<>();
        Predicate<T> predicate;
        if (forInsert) {
            predicate = row -> !rowsOldMap.containsKey(keyColumnValuesFunction(row));
        } else {
            predicate = row -> rowsOldMap.containsKey(keyColumnValuesFunction(row));
        }
        if (compute) {
            dataForInsertOrUpdate = rowsNew.stream().filter(predicate).collect(Collectors.toList());
        }
        return dataForInsertOrUpdate;
    }

    private Map<Set<Object>, T> toKeyMap(List<T> data) {
        Map<Set<Object>, T> keyMap = new HashMap<>();
        for (T row : data) {
            keyMap.put(keyColumnValuesFunction(row), row);
        }
        return keyMap;
    }

    private Set<Object> keyColumnValuesFunction(T row) {
        Set<Object> keyColumnValues = new HashSet<>();
        for (Function<T, ?> keyColumn : keyColumns) {
            Object value = keyColumn.apply(row);
            keyColumnValues.add(value);
        }
        return keyColumnValues;
    }

    public static <T> MergeBeanUtil<T> init(@SuppressWarnings("unused") Class<T> beanClass) {
        return new MergeBeanUtil<>();
    }

    public MergeBeanUtil<T> dataOld(List<T> dataOld) {
        this.dataOld = dataOld;
        return this;
    }

    public MergeBeanUtil<T> dataNew(List<T> dataNew) {
        this.dataNew = dataNew;
        return this;
    }

    public MergeBeanUtil<T> computeInsert(boolean computeInsert) {
        this.computeInsert = computeInsert;
        return this;
    }

    public MergeBeanUtil<T> computeUpdate(boolean computeUpdate) {
        this.computeUpdate = computeUpdate;
        return this;
    }

    public MergeBeanUtil<T> computeDelete(boolean computeDelete) {
        this.computeDelete = computeDelete;
        return this;
    }

    @SafeVarargs
    public final MergeBeanUtil<T> keyColumns(Function<T, ?>... keyColumns) {
        this.keyColumns = new HashSet<>(Arrays.asList(keyColumns));
        return this;
    }

    public DataCompareResult<T> debug() {
        DataCompareResult<T> merger = merge();
        int topN = 3;
        Function<List<T>, String> top =
                rows -> rows.stream().limit(topN).map(T::toString).collect(Collectors.joining("\n"));
        BiConsumer<List<T>, String> logi = (rows, type) -> {
            String end = rows.size() > 3 ? "\n...\n" : "\n";
            log.info("\n------------ {}[{}] ------------\n{}{}", type, rows.size(), top.apply(rows), end);
        };
        logi.accept(merger.getDataForInsert(), "insert");
        logi.accept(merger.getDataForDelete(), "delete");
        logi.accept(merger.getDataForUpdate(), "update");
        return merger;
    }
}
