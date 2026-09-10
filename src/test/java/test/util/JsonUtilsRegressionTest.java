package test.util;

import org.junit.jupiter.api.Test;
import org.xht.xdb.function.impl.SetBooleanField;
import org.xht.xdb.util.JsonUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsRegressionTest {
    public static class Fields {
        public Date date;
        public java.sql.Date sqlDate;
        public java.sql.Time time;
        public Timestamp timestamp;
        public LocalDate localDate;
        public LocalTime localTime;
        public LocalDateTime localDateTime;
        public StringBuilder builder;
        public StringBuffer buffer;
        public AtomicInteger atomicInteger;
        public AtomicLong atomicLong;
        public boolean flag;
        public Boolean boxedFlag;
        public byte b;
        public short s;
        public int i;
        public long l;
        public float f;
        public double d;
        public char c;
        public BigDecimal decimal;
        public BigInteger integer;
        public Number number;
        public CharSequence sequence;
        public CustomNumber custom;
        public List<Integer> list;
        public Collection<Integer> collection;
        public Set<Integer> set;
        public SortedSet<Integer> sorted;
        public NavigableSet<Integer> navigable;
        public Queue<Integer> queue;
        public Deque<Integer> deque;
        public LinkedList<Integer> linked;
        public HashSet<Integer> hash;
        public TreeSet<Integer> tree;
        public ArrayDeque<Integer> arrayDeque;
        public PriorityQueue<Integer> priority;
        public Vector<Integer> vector;
        public CustomList<Integer> customList;
        public ArrayBlockingQueue<Integer> bounded;
        public SpecialSet<Integer> special;
        public BrokenList<Integer> broken;
        public SortedSet<Bean> unsortable;
        public List<Bean> beans;
        public int[] array;
        public Bean bean;
        public String text;
        public Object object;
        public Mode mode;
    }

    public enum Mode { ONE }
    public static class Bean { public int value; }
    public static class CustomNumber extends Number {
        public int value;
        public int intValue() { return value; }
        public long longValue() { return value; }
        public float floatValue() { return value; }
        public double doubleValue() { return value; }
    }
    public static class CustomList<E> extends ArrayList<E> { }
    public interface SpecialSet<E> extends Set<E> { }
    public static class BrokenList<E> extends ArrayList<E> {
        public BrokenList() { throw new IllegalStateException("broken constructor"); }
    }

    private Field field(String name) throws Exception { return Fields.class.getField(name); }

    private Object assign(String name, String text) throws Exception {
        Field field = field(name);
        Object result = JsonUtils.toBeanCompatible(text, field);
        Fields bean = new Fields();
        field.set(bean, result);
        return field.get(bean);
    }

    private void roundTrip(String name, Object value) throws Exception {
        Fields bean = new Fields();
        Field field = field(name);
        field.set(bean, value);
        Object restored = assign(name, JsonUtils.toJsonString(bean, field));
        assertEquals(value.getClass(), restored.getClass(), name);
        if (value instanceof AtomicInteger || value instanceof AtomicLong || value instanceof CharSequence) {
            assertEquals(value.toString(), restored.toString(), name);
        } else {
            assertEquals(value, restored, name);
        }
    }

    @Test
    void scalarRoundTripsPreserveDeclaredTypesAndPrecision() throws Exception {
        roundTrip("date", new Date(1720000000000L));
        roundTrip("sqlDate", java.sql.Date.valueOf("2026-09-10"));
        roundTrip("time", java.sql.Time.valueOf("12:34:56"));
        roundTrip("timestamp", Timestamp.valueOf("2026-09-10 12:34:56.123456789"));
        roundTrip("localDate", LocalDate.of(2026, 9, 10));
        roundTrip("localTime", LocalTime.of(12, 34, 56, 123456789));
        roundTrip("localDateTime", LocalDateTime.of(2026, 9, 10, 12, 34, 56, 123456789));
        roundTrip("builder", new StringBuilder("a\"b\\c\n中文"));
        roundTrip("buffer", new StringBuffer("abc"));
        roundTrip("atomicInteger", new AtomicInteger(123));
        roundTrip("atomicLong", new AtomicLong(Long.MAX_VALUE));
    }

    @Test
    void dateKeepsLegacyTextFormatAndParsesStrictly() throws Exception {
        Fields bean = new Fields();
        bean.date = new Date(1720000000123L);
        String text = JsonUtils.toJsonString(bean, field("date"));
        assertEquals(bean.date.toString(), text);
        assertEquals(new Date(1720000000000L), assign("date", text));
        assertEquals(Date.from(Instant.parse("2026-09-10T12:34:56Z")),
                assign("date", "Thu Sep 10 12:34:56 UTC 2026"));
        assertEquals(Date.from(Instant.parse("2026-09-10T04:34:56Z")),
                assign("date", "Thu Sep 10 12:34:56 GMT+08:00 2026"));
        for (String invalid : new String[]{"", "invalid", "Thu Sep 31 12:34:56 UTC 2026",
                "Thu Sep 10 25:34:56 UTC 2026", "Fri Sep 10 12:34:56 UTC 2026",
                "Thu Sep 10 12:34:56 UTC 2026 trailing"}) {
            assertThrows(IllegalArgumentException.class, () -> assign("date", invalid), invalid);
        }
    }

    @Test
    void primitiveAndNumericRoundTrips() throws Exception {
        roundTrip("b", (byte) 12);
        roundTrip("s", (short) 123);
        roundTrip("i", 1234);
        roundTrip("l", Long.MAX_VALUE);
        roundTrip("f", 1.25F);
        roundTrip("d", 1.25D);
        roundTrip("c", '中');
        roundTrip("decimal", new BigDecimal("123456789.123456789"));
        roundTrip("integer", new BigInteger("123456789012345678901234567890"));
    }

    @Test
    void booleanSemanticsMatchFieldSetter() throws Exception {
        for (String text : new String[]{"1", "true", "TRUE", "t", "T", "yes", "YeS", "Y", "y", "  yes  ",
                "0", "false", "F", "no", "N", "", "2", "anything"}) {
            assertEquals(SetBooleanField.parse(text), assign("flag", text), text);
            assertEquals(SetBooleanField.parse(text), assign("boxedFlag", text), text);
        }
    }

    @Test
    void unsupportedScalarSupertypesAndSubtypesUseBeanPath() throws Exception {
        assertTrue(JsonUtils.isCompatible(field("number")));
        assertTrue(JsonUtils.isCompatible(field("sequence")));
        assertTrue(JsonUtils.isCompatible(field("custom")));
        CustomNumber number = (CustomNumber) assign("custom", "{\"value\":7}");
        assertEquals(7, number.intValue());
    }

    @Test
    void collectionResultsAreAssignableAndKeepElements() throws Exception {
        for (String name : new String[]{"list", "collection", "set", "sorted", "navigable", "queue", "deque",
                "linked", "hash", "tree", "arrayDeque", "priority", "vector", "customList"}) {
            Collection<?> result = (Collection<?>) assign(name, "[3,1,3,2]");
            assertTrue(result.containsAll(Arrays.asList(1, 2, 3)), name);
            assertEquals(result instanceof Set ? 3 : 4, result.size(), name);
            assertTrue(((Collection<?>) assign(name, "[]")).isEmpty(), name);
            // 再次解析覆盖缓存路径。
            assertNotSame(result, assign(name, "[3,1,3,2]"));
        }
        assertEquals(Arrays.asList(1, 2, 3), new ArrayList<>((SortedSet<?>) assign("sorted", "[3,1,2]")));
        Deque<?> deque = (Deque<?>) assign("deque", "[3,1,2]");
        assertEquals(3, deque.removeFirst());
        assertEquals(2, deque.removeLast());
        assertEquals(3, ((Queue<?>) assign("queue", "[3,1,2]")).remove());
        assertEquals(1, ((PriorityQueue<?>) assign("priority", "[3,1,2]")).remove());
    }

    @Test
    void unconstructibleCollectionsFailWithTargetType() throws Exception {
        for (String name : new String[]{"bounded", "special", "broken"}) {
            Field field = field(name);
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> JsonUtils.toBeanCompatible("[]", field));
            assertTrue(error.getMessage().contains(field.getType().getName()));
            if (!name.equals("special")) assertNotNull(error.getCause());
        }
    }

    @Test
    void invalidElementsAndScalarsFailExplicitly() throws Exception {
        Field sorted = field("unsortable");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toBeanCompatible("[{\"value\":1}]", sorted));
        assertTrue(error.getMessage().contains(SortedSet.class.getName()));
        assertNotNull(error.getCause());
        assertThrows(NumberFormatException.class, () -> assign("atomicInteger", "abc"));
        assertThrows(RuntimeException.class, () -> assign("localDate", "invalid"));
    }

    @Test
    void jacksonFallbackPreservesCollectionTypesAndBeanElements() throws Exception {
        URL classes = JsonUtils.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{classes}, JsonUtils.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("cn.hutool.json.")) throw new ClassNotFoundException(name);
                if (name.equals(JsonUtils.class.getName()) || name.startsWith(JsonUtils.class.getName() + "$")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) type = findClass(name);
                        if (resolve) resolveClass(type);
                        return type;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            Class<?> isolated = Class.forName(JsonUtils.class.getName(), true, loader);
            Field hutool = isolated.getDeclaredField("HUTOOL_TO_LIST");
            hutool.setAccessible(true);
            assertNull(hutool.get(null));
            Method parse = isolated.getMethod("toBeanCompatible", String.class, Field.class);
            for (String name : new String[]{"sorted", "queue", "deque", "linked", "customList"}) {
                Object result = parse.invoke(null, "[3,1,2]", field(name));
                field(name).set(new Fields(), result);
                assertTrue(((Collection<?>) result).containsAll(Arrays.asList(1, 2, 3)));
            }
            List<?> beans = (List<?>) parse.invoke(null, "[{\"value\":7}]", field("beans"));
            assertEquals(7, ((Bean) beans.get(0)).value);
            assertEquals(7, ((Bean) parse.invoke(null, "{\"value\":7}", field("bean"))).value);
            Fields bean = new Fields();
            bean.beans = Collections.singletonList((Bean) beans.get(0));
            String json = (String) isolated.getMethod("toJsonString", Object.class, Field.class)
                    .invoke(null, bean, field("beans"));
            assertEquals(7, ((Bean) ((List<?>) parse.invoke(null, json, field("beans"))).get(0)).value);
        }
    }

    @Test
    void existingBeanArrayEnumAndRawTextPathsRemainUsable() throws Exception {
        assertEquals(7, ((Bean) assign("bean", "{\"value\":7}")).value);
        assertEquals(7, ((Bean) ((List<?>) assign("beans", "[{\"value\":7}]")).get(0)).value);
        assertArrayEquals(new int[]{1, 2}, (int[]) assign("array", "[1,2]"));
        assertEquals(Mode.ONE, assign("mode", "ONE"));
        assertEquals("raw", assign("text", "raw"));
        assertEquals("raw", assign("object", "raw"));
        assertNull(JsonUtils.toBeanCompatible(null, field("date")));
        assertNull(JsonUtils.toJsonString(new Fields(), field("date")));
    }
}
