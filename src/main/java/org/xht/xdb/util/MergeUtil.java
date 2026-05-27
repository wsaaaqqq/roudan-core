package org.xht.xdb.util;

import lombok.extern.slf4j.*;
import org.xht.xdb.vo.DataCompareResult;
import org.xht.xdb.vo.Row;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
public class MergeUtil {
    private List<Row> dataOld = new ArrayList<>();
    private List<Row> dataNew = new ArrayList<>();
    private Set<String> keyColumns;
    private boolean computeInsert = true;
    private boolean computeUpdate = true;
    private boolean computeDelete = true;

    public DataCompareResult<Row> merge() {
        if (keyColumns == null || keyColumns.isEmpty())
            throw new RuntimeException("keyColumns is empty");
        dataOld = Optional.ofNullable(dataOld).orElse(new ArrayList<>());
        dataNew = Optional.ofNullable(dataNew).orElse(new ArrayList<>());
        Map<Set<Object>, Row> keyMapOld = toKeyMap(dataOld);
        List<Row> dataForInsert = dataForInsertOrUpdate(keyMapOld, dataNew, true, computeInsert);
        List<Row> dataForUpdate = dataForInsertOrUpdate(keyMapOld, dataNew, false, computeUpdate);
        List<Row> dataForDelete = dataForDelete(dataOld, dataNew);
        return new DataCompareResult<>(dataForInsert, dataForUpdate, dataForDelete);
    }

    private List<Row> dataForDelete(List<Row> rowsOld, List<Row> rowsNew) {
        List<Row> dataForDelete = new ArrayList<>();
        if (rowsOld == null || rowsOld.isEmpty())
            return dataForDelete;
        if (computeDelete) {
            Map<Set<Object>, Row> keyMapNew = toKeyMap(rowsNew);
            dataForDelete = rowsOld
                    .stream()
                    .filter(row -> !keyMapNew.containsKey(keyColumnValuesFunction(row)))
                    .collect(Collectors.toList());
        }
        return dataForDelete;
    }

    private List<Row> dataForInsertOrUpdate(Map<Set<Object>, Row> rowsOldMap, List<Row> rowsNew, boolean forInsert,
            boolean compute
    ) {
        List<Row> dataForInsertOrUpdate = new ArrayList<>();
        Predicate<Row> rowPredicate;
        if (forInsert) {
            rowPredicate = row -> !rowsOldMap.containsKey(keyColumnValuesFunction(row));
        } else {
            rowPredicate = row -> rowsOldMap.containsKey(keyColumnValuesFunction(row));
        }
        if (compute) {
            dataForInsertOrUpdate = rowsNew.stream().filter(rowPredicate).collect(Collectors.toList());
        }
        return dataForInsertOrUpdate;
    }

    private Map<Set<Object>, Row> toKeyMap(List<Row> data) {
        Map<Set<Object>, Row> keyMap = new HashMap<>();
        for (Row row : data) {
            keyMap.put(keyColumnValuesFunction(row), row);
        }
        return keyMap;
    }

    private Set<Object> keyColumnValuesFunction(Row row) {
        Set<Object> keyColumnValues = new HashSet<>();
        for (String keyColumn : keyColumns) {
            Object value = row.get(keyColumn);
            keyColumnValues.add(value);
        }
        return keyColumnValues;
    }

    public static MergeUtil init() {
        return new MergeUtil();
    }

    public static <T> MergeBeanUtil<T> init(Class<T> beanClass) {
        return MergeBeanUtil.init(beanClass);
    }

    public MergeUtil dataOld(List<Row> dataOld) {
        this.dataOld = dataOld;
        return this;
    }

    public MergeUtil dataNew(List<Row> dataNew) {
        this.dataNew = dataNew;
        return this;
    }

    public MergeUtil computeInsert(boolean computeInsert) {
        this.computeInsert = computeInsert;
        return this;
    }

    public MergeUtil computeUpdate(boolean computeUpdate) {
        this.computeUpdate = computeUpdate;
        return this;
    }

    public MergeUtil computeDelete(boolean computeDelete) {
        this.computeDelete = computeDelete;
        return this;
    }

    public MergeUtil keyColumns(String... keyColumns) {
        this.keyColumns = new HashSet<>(Arrays.asList(keyColumns));
        return this;
    }

    public DataCompareResult<Row> debug() {
        DataCompareResult<Row> merger = merge();
        int topN = 3;
        Function<List<Row>, String> top =
                rows -> rows.stream().limit(topN).map(Row::toString).collect(Collectors.joining("\n"));
        BiConsumer<List<Row>, String> logi = (rows, type) -> {
            String end = rows.size() > 3 ? "\n...\n" : "\n";
            log.info("\n------------ {}[{}] ------------\n{}{}", type, rows.size(), top.apply(rows), end);
        };
        logi.accept(merger.getDataForInsert(), "insert");
        logi.accept(merger.getDataForDelete(), "delete");
        logi.accept(merger.getDataForUpdate(), "update");
        return merger;
    }
}
