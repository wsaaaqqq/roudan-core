package test.util;

import org.junit.jupiter.api.Test;
import org.xht.xdb.function.SetFieldValueFunction;
import org.xht.xdb.function.impl.SetFieldWithValueDefault;
import org.xht.xdb.util.ResultSetUtil;
import org.xht.xdb.vo.FieldVo;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetUtilRegressionTest {
    @Test
    void scalarTokensConvertConsistentlyAndPreserveNull() throws Exception {
        Object[][] cases = {
                {int.class, " 42 ", 42}, {java.lang.Integer.class, 42L, 42},
                {long.class, "42", 42L}, {Long.class, 42, 42L},
                {double.class, " 2.5 ", 2.5d}, {Double.class, 2.5f, 2.5d},
                {float.class, " 2.5 ", 2.5f}, {Float.class, 2.5d, 2.5f},
                {short.class, "12", (short) 12}, {Short.class, 12L, (short) 12},
                {byte.class, " -12 ", (byte) -12}, {Byte.class, 12L, (byte) 12},
                {boolean.class, "yes", true}, {Boolean.class, 0, false},
                {char.class, " Z ", 'Z'}, {Character.class, 'Q', 'Q'},
                {String.class, 42, "42"},
                {BigDecimal.class, "123.45", new BigDecimal("123.45")},
                {BigInteger.class, "123456789012345678901234567890", new BigInteger("123456789012345678901234567890")},
                {Timestamp.class, Timestamp.valueOf("2026-09-10 12:34:56"), Timestamp.valueOf("2026-09-10 12:34:56")},
                {LocalDateTime.class, "2026-09-10 12:34:56", LocalDateTime.of(2026, 9, 10, 12, 34, 56)},
                {LocalDate.class, java.sql.Date.valueOf("2026-09-10"), LocalDate.of(2026, 9, 10)},
                {LocalTime.class, java.sql.Time.valueOf("12:34:56"), LocalTime.of(12, 34, 56)},
                {byte[].class, new byte[]{0, -1, 127}, new byte[]{0, -1, 127}}
        };
        for (Object[] entry : cases) {
            Class<?> type = (Class<?>) entry[0];
            List<?> list = ResultSetUtil.toListBeanFirstColumn(rows(entry[1], null), type);
            assertEquals(2, list.size(), type.getName());
            assertValue(entry[2], list.get(0));
            assertNull(list.get(1), type.getName());
            assertValue(entry[2], ResultSetUtil.firstBean(rows(entry[1]), type));
            assertNull(ResultSetUtil.firstBean(rows((Object) null), type), type.getName());
            assertNull(ResultSetUtil.firstBean(rows(), type), type.getName());
        }
        assertNull(ResultSetUtil.firstBean(null, Byte.class));
    }

    @Test
    void primitiveAndWrapperFieldsUseConversionsAndReuseCache() throws Exception {
        PrimitiveBean bean = new PrimitiveBean();
        Object[][] cases = {
                {"i", "42", 42}, {"wi", "42", 42}, {"l", "42", 42L}, {"wl", "42", 42L},
                {"d", "2.5", 2.5d}, {"wd", "2.5", 2.5d}, {"f", "2.5", 2.5f}, {"wf", "2.5", 2.5f},
                {"s", "12", (short) 12}, {"ws", "12", (short) 12},
                {"b", 12L, (byte) 12}, {"wb", "12", (byte) 12},
                {"bool", "yes", true}, {"wbool", "yes", true}, {"c", "Z", 'Z'}, {"wc", "Z", 'Z'}
        };
        for (Object[] entry : cases) {
            Field field = PrimitiveBean.class.getField((String) entry[0]);
            FieldVo vo = new FieldVo(field);
            ResultSetUtil.setFieldValue(vo, bean, entry[1]);
            assertEquals(entry[2], field.get(bean));
            SetFieldValueFunction cached = vo.getSetFieldValueFunction();
            assertNotNull(cached);
            assertFalse(cached instanceof SetFieldWithValueDefault);
            ResultSetUtil.setFieldValue(vo, bean, entry[1]);
            assertSame(cached, vo.getSetFieldValueFunction());
        }
        FieldVo custom = new FieldVo(PrimitiveBean.class.getField("i"));
        SetFieldValueFunction override = new SetFieldValueFunction() {
            @Override
            public <T> void apply(T target, Field field, Object value) {
                ((PrimitiveBean) target).i = 99;
            }
        };
        custom.setSetFieldValueFunction(override);
        ResultSetUtil.setFieldValue(custom, bean, "42");
        assertEquals(99, bean.i);
        assertSame(override, custom.getSetFieldValueFunction());
        ResultSetUtil.setFieldValue(new FieldVo(null), bean, "ignored");
    }

    @Test
    void sameSimpleNameAndUnknownTypesKeepDefaultBehavior() throws Exception {
        CollisionBean bean = new CollisionBean();
        Integer value = new Integer();
        FieldVo vo = new FieldVo(CollisionBean.class.getField("value"));
        ResultSetUtil.setFieldValue(vo, bean, value);
        assertSame(value, bean.value);
        assertTrue(vo.getSetFieldValueFunction() instanceof SetFieldWithValueDefault);
        assertSame(value, ResultSetUtil.toListBeanFirstColumn(rows(value), Integer.class).get(0));
        assertSame(value, ResultSetUtil.firstBean(entityRows("value", value), CollisionBean.class).value);
        assertNotNull(ResultSetUtil.firstBean(entityRows("missing", 1), Integer.class));
    }

    @Test
    void entityMappingStillSkipsNullAndMapsPrimitiveFields() throws Exception {
        PrimitiveBean bean = ResultSetUtil.firstBean(entityRows("i", "42"), PrimitiveBean.class);
        assertEquals(42, bean.i);
        List<PrimitiveBean> list = ResultSetUtil.toListBean(entityRows("i", "43", null), PrimitiveBean.class);
        assertEquals(43, list.get(0).i);
        assertEquals(7, list.get(1).i);
    }

    private static void assertValue(Object expected, Object actual) {
        if (expected instanceof byte[]) {
            assertArrayEquals((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    // Scalar proxies deliberately reject metadata access: scalars must never enter entity reflection.
    private static ResultSet rows(Object... values) {
        return resultSet(null, values);
    }

    private static ResultSet entityRows(String label, Object... values) {
        ResultSetMetaData metadata = (ResultSetMetaData) Proxy.newProxyInstance(
                ResultSetUtilRegressionTest.class.getClassLoader(), new Class<?>[]{ResultSetMetaData.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getColumnCount": return 1;
                        case "getColumnLabel": return label;
                        default: throw new AssertionError(method.getName());
                    }
                });
        return resultSet(metadata, values);
    }

    private static ResultSet resultSet(ResultSetMetaData metadata, Object[] values) {
        int[] index = {-1};
        return (ResultSet) Proxy.newProxyInstance(ResultSetUtilRegressionTest.class.getClassLoader(),
                new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "next": return ++index[0] < values.length;
                        case "getObject":
                            assertEquals(1, args[0]);
                            return values[index[0]];
                        case "getMetaData":
                            assertNotNull(metadata, "Scalar conversion must not request entity metadata");
                            return metadata;
                        default: throw new AssertionError(method.getName());
                    }
                });
    }

    public static class PrimitiveBean {
        public int i = 7;
        public java.lang.Integer wi;
        public long l;
        public Long wl;
        public double d;
        public Double wd;
        public float f;
        public Float wf;
        public short s;
        public Short ws;
        public byte b;
        public Byte wb;
        public boolean bool;
        public Boolean wbool;
        public char c;
        public Character wc;
    }

    public static class Integer {
        public Object marker;
    }

    public static class CollisionBean {
        public Integer value;
    }
}
