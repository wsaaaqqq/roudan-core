package org.xht.xdb.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.function.SetFieldValueFunction;
import org.xht.xdb.function.impl.*;
import org.xht.xdb.vo.FieldVo;
import org.xht.xdb.vo.Row;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

/**
 * @author xht
 */
@Slf4j
public class ResultSetUtil {

    /**
     * 显示结果集
     *
     * @param resultSet resultSet
     * @throws SQLException 异常
     */
    public static void out(ResultSet resultSet) throws SQLException {
        List<Row> listRow = toListRow(resultSet);
        for (Row row : listRow) {
            log.info("{}", row);
        }
    }

    /**
     * resultSet to list key一般为“XX_ID”
     *
     * @param resultSet resultSet
     * @return list
     * @throws SQLException SQLException
     */
    public static List<Object[]> toListArray(ResultSet resultSet) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toArray(resultSet, colCount));
        }
        return rows;
    }

    /**
     *
     * key为原始key
     *
     * @param resultSet resultSet
     * @return list
     * @throws SQLException SQLException
     */
    public static List<Map<String, Object>> toList(ResultSet resultSet) throws SQLException {
        return toListOrigin(resultSet);
    }


    public static List<Map<String, Object>> toListOrigin(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toMapOriginKey(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    public static List<Map<String, Object>> toListUpperCase(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toMapUpperCase(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    /**
     * resultSet to list key一般为“XX_ID”
     *
     * @param resultSet resultSet
     * @return list
     * @throws SQLException SQLException
     */
    public static List<Row> toListRow(ResultSet resultSet) throws SQLException {
        return toListRowUpperCase(resultSet);
    }

    public static List<Row> toListRowOrigin(ResultSet resultSet) throws SQLException {
        List<Row> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toRowOriginKey(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    public static List<Row> toListRowCamel(ResultSet resultSet) throws SQLException {
        List<Row> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toRowCamel(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    public static List<Row> toListRowLowerCase(ResultSet resultSet) throws SQLException {
        List<Row> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toRowLowerCase(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    public static List<Row> toListRowUpperCase(ResultSet resultSet) throws SQLException {
        List<Row> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toRowUpperCase(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    /**
     * resultSet to List<T>
     *
     * @param resultSet      resultSet
     * @param rowTransformer 行转换器
     * @return list
     * @throws SQLException SQLException
     */
    public static <T> List<T> to(ResultSet resultSet, RowTransformer<T> rowTransformer) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        List<T> rows = new ArrayList<>();
        while (resultSet.next()) {
            rows.add(rowTransformer.transform(resultSet, colCount, resultSetMetaData));
        }
        return rows;
    }

    /**
     * resultSet to list 驼峰形式
     *
     * @param resultSet resultSet
     * @return List
     * @throws SQLException SQLException
     */
    public static List<Map<String, Object>> toListCamp(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toMapCamel(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }

    public static List<Map<String, Object>> toListLowerCase(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        while (resultSet.next()) {
            rows.add(toMapLowerCase(resultSet, resultSetMetaData, colCount));
        }
        return rows;
    }


    public static Map<String, Object> toMap(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount,
            boolean toCamel, Boolean toUpperCase
    ) throws SQLException {
        if (toCamel)
            return toMapCamel(resultSet, resultSetMetaData, colCount);
        if (toUpperCase == null)
            return toMapOriginKey(resultSet, resultSetMetaData, colCount);
        return toUpperCase ? toMapUpperCase(resultSet, resultSetMetaData, colCount) :
                toMapLowerCase(resultSet, resultSetMetaData, colCount);
    }

    public static Map<String, Object> toMapOriginKey(ResultSet resultSet, ResultSetMetaData resultSetMetaData,
            int colCount
    ) throws SQLException {
        Map<String, Object> row = new HashMap<>(colCount - 1);
        String key;
        for (int i = 1; i < colCount; i++) {
            key = getColumnName(resultSetMetaData, i);
            addKey(resultSet, i, row, key);
        }
        return row;
    }

    public static Map<String, Object> toMapUpperCase(ResultSet resultSet, ResultSetMetaData resultSetMetaData,
            int colCount
    ) throws SQLException {
        Map<String, Object> row = new HashMap<>(colCount - 1);
        String key;
        for (int i = 1; i < colCount; i++) {
            key = getColumnName(resultSetMetaData, i).toUpperCase(Locale.ROOT);
            addKey(resultSet, i, row, key);
        }
        return row;
    }

    public static Map<String, Object> toMapLowerCase(ResultSet resultSet, ResultSetMetaData resultSetMetaData,
            int colCount
    ) throws SQLException {
        Map<String, Object> row = new HashMap<>(colCount - 1);
        String key;
        for (int i = 1; i < colCount; i++) {
            key = getColumnName(resultSetMetaData, i).toLowerCase(Locale.ROOT);
            addKey(resultSet, i, row, key);
        }
        return row;
    }

    public static Map<String, Object> toMapCamel(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount)
            throws SQLException {
        Map<String, Object> row = new HashMap<>(colCount - 1);
        String key;
        for (int i = 1; i < colCount; i++) {
            key = Underline2CamelUtil.underline2Camel(getColumnName(resultSetMetaData, i), true);
            addKey(resultSet, i, row, key);
        }
        return row;
    }

    private static void addKey(ResultSet resultSet, int i, Map<String, Object> row, String key) throws SQLException {
        Object object = resultSet.getObject(i);
        if (object != null) {
            if (object instanceof Clob) {
                row.put(key, ClobUtil.toString((Clob) object));
            } else if (object instanceof Blob) {
                row.put(key, BlobUtil.toBinaryString((Blob) object));
            } else {
                row.put(key, object);
            }
        } else {
            row.put(key, null);
        }
    }

    @SuppressWarnings("unused")
    public static Row toRow(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount, Boolean toCamel,
            Boolean toUpperCase
    ) throws SQLException {
        if (toCamel)
            return toRowCamel(resultSet, resultSetMetaData, colCount);
        if (toUpperCase == null)
            return toRowOriginKey(resultSet, resultSetMetaData, colCount);
        return toUpperCase ? toRowUpperCase(resultSet, resultSetMetaData, colCount) :
                toRowLowerCase(resultSet, resultSetMetaData, colCount);
    }

    public static Row toRowCamel(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount)
            throws SQLException {
        Row row = new Row(colCount - 1);
        for (int i = 1; i < colCount; i++) {
            addKey(resultSet, i, row, Underline2CamelUtil.underline2Camel(getColumnName(resultSetMetaData, i), true));
        }
        return row;
    }

    public static Row toRowUpperCase(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount)
            throws SQLException {
        Row row = new Row(colCount - 1);
        for (int i = 1; i < colCount; i++) {
            addKey(resultSet, i, row, getColumnName(resultSetMetaData, i).toUpperCase(Locale.ROOT));
        }
        return row;
    }

    public static Row toRowLowerCase(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount)
            throws SQLException {
        Row row = new Row(colCount - 1);
        for (int i = 1; i < colCount; i++) {
            addKey(resultSet, i, row, getColumnName(resultSetMetaData, i).toLowerCase(Locale.ROOT));
        }
        return row;
    }

    public static Row toRowOriginKey(ResultSet resultSet, ResultSetMetaData resultSetMetaData, int colCount)
            throws SQLException {
        Row row = new Row(colCount - 1);
        for (int i = 1; i < colCount; i++) {
            addKey(resultSet, i, row, getColumnName(resultSetMetaData, i));
        }
        return row;
    }

    public static Object[] toArray(ResultSet resultSet, int colCount) throws SQLException {
        Object[] row = new Object[colCount - 1];
        for (int i = 1; i < colCount; i++) {
            row[i - 1] = resultSet.getObject(i);
        }
        return row;
    }

    public static <T> List<T> toListBean(ResultSet resultSet, Class<T> beanClass)
            throws SQLException {
        List<T> beans = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        while (resultSet.next()) {
            addBean(resultSet, beanClass, beans, sortedFields, colCount);
        }
        return beans;
    }

    public static <V extends Comparable<V>> TreeMap<V, V> toTreeMap(ResultSet resultSet, Class<V> beanClass)
            throws SQLException {
        TreeMap<V, V> beans = new TreeMap<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        while (resultSet.next()) {
            V bean = toBean(resultSet, beanClass, sortedFields, colCount);
            beans.put(bean, bean);
        }
        return beans;
    }

    public static <V> TreeMap<V, V> toTreeMap(ResultSet resultSet, Class<V> beanClass, Comparator<? super V> comparator)
            throws SQLException {
        TreeMap<V, V> beans = new TreeMap<>(comparator);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        if (Map.class.isAssignableFrom(beanClass)) {
            while (resultSet.next()) {
                @SuppressWarnings("unchecked") V bean = (V) toMapUpperCase(resultSet, resultSetMetaData, colCount);
                beans.put(bean, bean);
            }
        } else {
            while (resultSet.next()) {
                V bean = toBean(resultSet, beanClass, sortedFields, colCount);
                beans.put(bean, bean);
            }
        }
        return beans;
    }

    public static void main(String[] args) {

    }

    public static <K extends Comparable<K>, V> TreeMap<K, V> toTreeMap(ResultSet resultSet, Class<V> beanClass,
            Function<V, K> keyFunction
    ) throws SQLException {
        TreeMap<K, V> beans = new TreeMap<>();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        while (resultSet.next()) {
            V bean = toBean(resultSet, beanClass, sortedFields, colCount);
            K key = keyFunction.apply(bean);
            beans.put(key, bean);
        }
        return beans;
    }

    public static <K, V> TreeMap<K, V> toTreeMap(ResultSet resultSet, Class<V> beanClass, Function<V, K> keyFunction,
            Comparator<? super K> comparator
    ) throws SQLException {
        TreeMap<K, V> beans = new TreeMap<>(comparator);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        while (resultSet.next()) {
            V bean = toBean(resultSet, beanClass, sortedFields, colCount);
            K key = keyFunction.apply(bean);
            beans.put(key, bean);
        }
        return beans;
    }

    /**
     * same as: {@link #firstRowOrigin(ResultSet)}
     */
    public static Map<String, Object> first(ResultSet resultSet) throws SQLException {
        Map<String, Object> map = null;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            map = toMapOriginKey(resultSet, resultSetMetaData, colCount);
        }
        return map;
    }

    public static Row firstRow(ResultSet resultSet) throws SQLException {
        Row map = null;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            map = toRowUpperCase(resultSet, resultSetMetaData, colCount);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T> T firstBean(ResultSet resultSet, Class<T> beanClass)
            throws SQLException {
        T bean = null;
        if (resultSet != null && resultSet.next()) {
            if (String.class.equals(beanClass))
                return (T) resultSet.getString(1);
            if (Long.class.equals(beanClass))
                return (T) Long.valueOf(resultSet.getLong(1));
            if (Integer.class.equals(beanClass))
                return (T) Integer.valueOf(resultSet.getInt(1));
            if (Double.class.equals(beanClass))
                return (T) Double.valueOf(resultSet.getDouble(1));
            if (Float.class.equals(beanClass))
                return (T) Float.valueOf(resultSet.getFloat(1));
            if (BigInteger.class.equals(beanClass))
                return (T) Optional.ofNullable(resultSet.getObject(1))
                                   .map(Object::toString)
                                   .map(BigInteger::new)
                                   .orElse(null);
            if (BigDecimal.class.equals(beanClass))
                return (T) resultSet.getBigDecimal(1);
            if (Timestamp.class.equals(beanClass))
                return (T) resultSet.getTimestamp(1);
            //复杂对象
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount() + 1;
            FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
            bean = toBean(resultSet, beanClass, sortedFields, colCount);
        }
        return bean;
    }

    public static List<Map<String, Object>> limit(ResultSet resultSet, int start, int count, int colCount,
            ResultSetMetaData resultSetMetaData
    ) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        int c = 0;
        if (start > 0) {
            int i = 0;
            while (c < count && resultSet.next()) {
                if (i < start) {
                    i++;
                } else {
                    rows.add(toMapUpperCase(resultSet, resultSetMetaData, colCount));
                    c++;
                }
            }
        } else {
            while (c < count && resultSet.next()) {
                rows.add(toMapUpperCase(resultSet, resultSetMetaData, colCount));
                c++;
            }
        }
        return rows;
    }

    public static List<Row> limitRow(ResultSet resultSet, int start, int count, int colCount,
            ResultSetMetaData resultSetMetaData
    ) throws SQLException {
        List<Row> rows = new ArrayList<>();
        int c = 0;
        if (start > 0) {
            int i = 0;
            while (c < count && resultSet.next()) {
                if (i < start) {
                    i++;
                } else {
                    rows.add(toRowUpperCase(resultSet, resultSetMetaData, colCount));
                    c++;
                }
            }
        } else {
            while (c < count && resultSet.next()) {
                rows.add(toRowUpperCase(resultSet, resultSetMetaData, colCount));
                c++;
            }
        }
        return rows;
    }

    /**
     * start:0  count:10，从第1条记录开始（包含start），取10条记录
     * start:1  count:2，从第2条记录开始（包含start），取2条记录
     */
    public static List<Map<String, Object>> limit(ResultSet resultSet, int start, int count) throws SQLException {
        if (start < 0 || count < 0) {
            throw new RuntimeException("start不能小于0，并且不能小于end");
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        return limit(resultSet, start, count, colCount, resultSetMetaData);
    }

    public static List<Object[]> limitObjectArray(ResultSet resultSet, int start, int count, int colCount)
            throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        int c = 0;
        if (start > 0) {
            int rowIdx = 0;
            while (c < count && resultSet.next()) {
                if (rowIdx < start) {
                    rowIdx++;
                } else {
                    rows.add(toArray(resultSet, colCount));
                    c++;
                }
            }
        } else {
            while (c < count && resultSet.next()) {
                rows.add(toArray(resultSet, colCount));
                c++;
            }
        }
        return rows;
    }

    /**
     * start:0  count:10，从第1条记录开始（包含start），取10条记录
     * start:1  count:2，从第2条记录开始（包含start），取2条记录
     */
    public static List<Object[]> limitObjectArray(ResultSet resultSet, int start, int count) throws SQLException {
        if (start < 0 || count < 0) {
            throw new RuntimeException("start不能小于0，并且不能小于end");
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        return limitObjectArray(resultSet, start, count, colCount);
    }

    public static <T> List<T> limitBean(ResultSet resultSet, Class<T> beanClass, int start, int count, int colCount,
            ResultSetMetaData resultSetMetaData
    ) throws SQLException {
        List<T> beans = new ArrayList<>();
        FieldVo[] sortedFields = sortedFieldVo(resultSetMetaData, colCount, beanClass);
        int c = 0;
        if (start > 0) {
            int rowIdx = 0;
            while (c < count && resultSet.next()) {
                if (rowIdx < start) {
                    rowIdx++;
                } else {
                    addBean(resultSet, beanClass, beans, sortedFields, colCount);
                    c++;
                }
            }
        } else {
            while (c < count && resultSet.next()) {
                addBean(resultSet, beanClass, beans, sortedFields, colCount);
                c++;
            }
        }
        return beans;
    }

    public static <T> List<T> limitBean(ResultSet resultSet, Class<T> beanClass, int start, int count)
            throws SQLException {
        if (start < 0 || count < 0) {
            throw new RuntimeException("start不能小于0，并且不能小于end");
        }
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        return limitBean(resultSet, beanClass, start, count, colCount, resultSetMetaData);
    }

    public static <T> void addBean(ResultSet resultSet, Class<T> beanClass, List<T> beans, FieldVo[] sortedFields,
            int colCount
    ) throws SQLException {
        T bean = toBean(resultSet, beanClass, sortedFields, colCount);
        beans.add(bean);
    }

    public static <T> @NonNull T toBean(ResultSet resultSet, Class<T> beanClass, FieldVo[] sortedFields, int colCount)
            throws SQLException {
        T bean;
        try {
            bean = beanClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int i = 1; i < colCount; i++) {
            FieldVo fieldVo = sortedFields[i - 1];
            Object value = resultSet.getObject(i);
            if (value == null)
                continue;
            setFieldValue(fieldVo, bean, value);
        }
        return bean;
    }

    private static final SetStringField setStringField = new SetStringField();
    private static final SetIntegerField setIntegerField = new SetIntegerField();
    private static final SetLongField setLongField = new SetLongField();
    private static final SetDoubleField setDoubleField = new SetDoubleField();
    private static final SetFloatField setFloatField = new SetFloatField();
    private static final SetBigDecimalField setBigDecimalField = new SetBigDecimalField();
    private static final SetBigIntegerField setBigIntegerField = new SetBigIntegerField();
    private static final SetBooleanField setBooleanField = new SetBooleanField();
    private static final SetTimestampField setTimestampField = new SetTimestampField();
    private static final SetLocalDateTimeField setLocalDateTimeField = new SetLocalDateTimeField();
    private static final SetLocalDateField setLocalDateField = new SetLocalDateField();
    private static final SetLocalTimeField setLocalTimeField = new SetLocalTimeField();
    private static final SetCharacterField setCharacterField = new SetCharacterField();
    private static final SetByteArrayField setByteArrayField = new SetByteArrayField();
    private static final SetShortField setShortField = new SetShortField();
    private static final SetFieldWithValueDefault setFieldWithValueDefault = new SetFieldWithValueDefault();


    public static <T> void setFieldValue(FieldVo fieldVo, T bean, Object value) {
        Field field = fieldVo.getField();
        if (field == null)
            return;
        try {
            Class<?> type = field.getType();
            SetFieldValueFunction setFieldValueFunction = fieldVo.getSetFieldValueFunction();
            if (setFieldValueFunction == null) {
                String simpleName = type.getSimpleName();
                switch (simpleName) {
                    case "Integer":
                        setFieldValueFunction = setIntegerField;
                        break;
                    case "BigDecimal":
                        setFieldValueFunction = setBigDecimalField;
                        break;
                    case "Boolean":
                        setFieldValueFunction = setBooleanField;
                        break;
                    case "Long":
                        setFieldValueFunction = setLongField;
                        break;
                    case "BigInteger":
                        setFieldValueFunction = setBigIntegerField;
                        break;
                    case "Double":
                        setFieldValueFunction = setDoubleField;
                        break;
                    case "Float":
                        setFieldValueFunction = setFloatField;
                        break;
                    case "String":
                        setFieldValueFunction = setStringField;
                        break;
                    case "Character":
                        setFieldValueFunction = setCharacterField;
                        break;
                    case "byte[]":
                        setFieldValueFunction = setByteArrayField;
                        break;
                    case "LocalDateTime":
                        setFieldValueFunction = setLocalDateTimeField;
                        break;
                    case "LocalDate":
                        setFieldValueFunction = setLocalDateField;
                        break;
                    case "LocalTime":
                        setFieldValueFunction = setLocalTimeField;
                        break;
                    case "Timestamp":
                        setFieldValueFunction = setTimestampField;
                        break;
                    case "Short":
                        setFieldValueFunction = setShortField;
                        break;
                    default:
                        setFieldValueFunction = setFieldWithValueDefault;
                }
            }
            setFieldValueFunction.apply(bean, field, value);
            fieldVo.setSetFieldValueFunction(setFieldValueFunction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> FieldVo[] sortedFieldVo(ResultSetMetaData resultSetMetaData, int columnCount, Class<T> beanClass)
            throws SQLException {
        Map<String, Field> allFields = allFields(beanClass);
        FieldVo[] fieldVos = new FieldVo[columnCount];
        for (int i = 1; i < columnCount; i++) {
            String key = getColumnName(resultSetMetaData, i);
            Field field = getField(allFields, key, beanClass);
            if (field != null) {
                field.setAccessible(true);
            }
            FieldVo fieldVo = new FieldVo(field);
            fieldVos[i - 1] = fieldVo;
        }
        return fieldVos;
    }

    private static String getColumnName(ResultSetMetaData resultSetMetaData, int i) throws SQLException {
        String key = resultSetMetaData.getColumnLabel(i);
        if (key == null || key.isEmpty()) {
            key = resultSetMetaData.getColumnName(i);
        }
        return key;
    }

    public static <T> Field getField(Map<String, Field> allFields, String key, Class<T> clazz) {
        Field field = allFields.get(key);
        if (field == null) {
            field = allFields.get(key.toLowerCase(Locale.ROOT));
            if (field == null) {
                field = allFields.get(key.toUpperCase(Locale.ROOT));
                if (field == null) {
                    field = allFields.get(Underline2CamelUtil.underline2Camel(key, true));
                    if (field == null) {
                        field = allFields.get(Underline2CamelUtil.underline2Camel(key, false));
                        if (field == null) {
                            log.trace("{} has not field: {}", clazz.getName(), key);
                        }
                    }

                }
            }
        }
        return field;
    }

    public static Map<String, Field> allFields(Class<?> clazz) {
        Map<String, Field> allFields = new HashMap<>();
        while (clazz != null) {
            Field[] fields = ReflectUtil.getFields(clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                allFields.put(fieldName, field);
                Annotation[] annotationsField = field.getAnnotations();
                for (Annotation annotation : annotationsField) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.getName().equals("javax.persistence.Column")) {
                        try {
                            Method method = annotationType.getDeclaredMethod("name");
                            method.setAccessible(true);
                            String name = String.valueOf(method.invoke(annotation));
                            allFields.put(name, field);
                        } catch (Exception e) {
                            log.error("{}", fieldName, e);
                        }
                    }
                }
            }
            //如果entity有父类的情况
            clazz = clazz.getSuperclass();
        }
        return allFields;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> List<T> toListBeanFirstColumn(ResultSet resultSet, Class<T> ignoredBeanClass) {
        List<T> beans = new ArrayList<>();
        while (resultSet.next()) {
            Object value = resultSet.getObject(1);
            if (value == null) {
                beans.add(null);
            } else {
                if (ignoredBeanClass == String.class) {
                    beans.add((T) SetStringField.parse(value));
                } else if (ignoredBeanClass == Integer.class) {
                    beans.add((T) SetIntegerField.parse(value));
                } else if (ignoredBeanClass == Long.class) {
                    beans.add((T) SetLongField.parse(value));
                } else if (ignoredBeanClass == Double.class) {
                    beans.add((T) SetDoubleField.parse(value));
                } else if (ignoredBeanClass == Float.class) {
                    beans.add((T) SetFloatField.parse(value));
                } else if (ignoredBeanClass == BigDecimal.class) {
                    beans.add((T) SetBigDecimalField.parse(value));
                } else if (ignoredBeanClass == BigInteger.class) {
                    beans.add((T) SetIntegerField.parse(value));
                } else if (ignoredBeanClass == Boolean.class) {
                    beans.add((T) SetBooleanField.parse(value));
                } else if (ignoredBeanClass == Timestamp.class) {
                    beans.add((T) SetTimestampField.parse(value));
                } else if (ignoredBeanClass == LocalDateTime.class) {
                    beans.add((T) SetLocalDateTimeField.parse(value));
                } else if (ignoredBeanClass == LocalDate.class) {
                    beans.add((T) SetLocalDateField.parse(value));
                } else if (ignoredBeanClass == LocalTime.class) {
                    beans.add((T) SetLocalTimeField.parse(value));
                } else if (ignoredBeanClass == Character.class) {
                    beans.add((T) SetCharacterField.parse(value));
                } else if (ignoredBeanClass == byte[].class) {
                    beans.add((T) SetByteArrayField.parse(value));
                } else if (ignoredBeanClass == Short.class) {
                    beans.add((T) SetShortField.parse(value));
                } else {
                    beans.add((T) value);
                }
            }
        }
        return beans;
    }

    public static Object[] firstArray(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            return toArray(resultSet, colCount);
        }
        return null;
    }

    public static Row firstRowUpperCase(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            return toRowUpperCase(resultSet, resultSetMetaData, colCount);
        }
        return null;
    }

    public static Row firstRowLowerCase(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            return toRowLowerCase(resultSet, resultSetMetaData, colCount);
        }
        return null;
    }

    public static Row firstRowCamel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            return toRowCamel(resultSet, resultSetMetaData, colCount);
        }
        return null;
    }

    public static Row firstRowOrigin(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int colCount = resultSetMetaData.getColumnCount() + 1;
        if (resultSet.next()) {
            return toRowOriginKey(resultSet, resultSetMetaData, colCount);
        }
        return null;
    }
}
