package test.cascade;

import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.cascade.CascadeQueryService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class CascadeQueryServiceRegressionTest {
    static class Child { Marker marker = new Marker(); }
    static class Marker { boolean filled; }
    static class Nested { List<List<Child>> children; }
    static class Box<T> { Marker marker = new Marker(); }
    static class Boxes { List<Box<String>> boxes = new ArrayList<>(); }
    static class Raw { List items = Collections.singletonList("x"); }
    static class Wildcard { List<?> items = Collections.singletonList("x"); }
    static class Variable<T> { List<T> items; }
    static class Values { List<Object> values; }
    static class CollectionsBean {
        SortedSet<String> sorted = new TreeSet<>(Arrays.asList("b", "a"));
        NavigableSet<String> navigable = new TreeSet<>(Arrays.asList("b", "a"));
        Queue<String> queue = new LinkedList<>(Arrays.asList("b", "a"));
        Deque<String> deque = new LinkedList<>(Arrays.asList("b", "a"));
    }
    interface CustomList<E> extends List<E> { }
    public static class NoDefaultList<E> extends ArrayList<E> {
        public NoDefaultList(int size) { super(size); }
    }

    private <T> T fill(CascadeQueryService service, T bean) {
        Class<T> type = (Class<T>) bean.getClass();
        service.register(type, type, value -> value);
        return service.query(bean, type);
    }

    @Test
    void nestedCollectionsRecurseIntoElementsWithoutReflectingContainers() {
        CascadeQueryService service = new CascadeQueryService().register(Marker.class, m -> m.filled = true);
        Child child = new Child();
        Nested bean = new Nested();
        bean.children = Collections.singletonList(Collections.singletonList(child));
        assertSame(bean, fill(service, bean));
        assertTrue(child.marker.filled);
    }

    @Test
    void parameterizedElementUsesRawClassConverter() {
        AtomicInteger calls = new AtomicInteger();
        CascadeQueryService service = new CascadeQueryService()
                .register(Box.class, box -> calls.incrementAndGet())
                .register(Marker.class, m -> m.filled = true);
        Boxes bean = new Boxes();
        bean.boxes.add(new Box<>());
        fill(service, bean);
        assertEquals(1, calls.get());
        assertTrue(bean.boxes.get(0).marker.filled);
    }

    @Test
    void unresolvedTypesReportFieldAndTypeRatherThanClassCastException() {
        Variable<String> variable = new Variable<>();
        variable.items = Collections.singletonList("x");
        for (Object bean : Arrays.asList(new Raw(), new Wildcard(), variable)) {
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> fill(new CascadeQueryService(), bean));
            assertTrue(error.getMessage().contains(".items"));
            assertTrue(error.getCause() instanceof IllegalArgumentException);
            assertTrue(error.getCause().getMessage().contains("Cannot resolve collection element type"));
            assertTrue(error.getCause().getMessage().contains("items"));
        }
    }

    @Test
    void fallbackCollectionsAreAssignableToEverySupportedDeclaration() {
        CascadeQueryService service = new CascadeQueryService();
        for (Class type : Arrays.asList(Collection.class, List.class, Set.class, SortedSet.class,
                NavigableSet.class, Queue.class, Deque.class, AbstractList.class, AbstractSet.class)) {
            Collection result = service.queryCollection(null, type, String.class);
            assertTrue(type.isInstance(result), type.getName());
            assertTrue(result.isEmpty());
        }
        assertInstanceOf(LinkedHashSet.class, service.queryCollection(null, LinkedHashSet.class, String.class));
        for (Class type : Arrays.asList(CustomList.class, NoDefaultList.class)) {
            RuntimeException error = assertThrows(RuntimeException.class,
                    () -> service.queryCollection(null, type, String.class));
            assertTrue(error.getMessage().contains("Unsupported collection type: " + type.getName()));
        }
    }

    @Test
    void elementConversionAssignsSortedAndQueueFields() {
        CascadeQueryService service = new CascadeQueryService().register(String.class, String.class, String::toUpperCase);
        CollectionsBean bean = fill(service, new CollectionsBean());
        assertEquals(Arrays.asList("A", "B"), new ArrayList<>(bean.sorted));
        assertEquals("B", bean.navigable.last());
        assertEquals(Arrays.asList("B", "A"), new ArrayList<>(bean.queue));
        assertEquals("A", bean.deque.peekLast());
    }

    @Test
    void basicJdkValuesRemainTerminalAndExplicitConvertersStillRun() {
        Values bean = new Values();
        bean.values = Arrays.asList("x", 1, BigDecimal.ONE, BigInteger.TEN, new Date(),
                Calendar.getInstance(), UUID.randomUUID(), LocalDate.now(), Instant.now(),
                Duration.ofSeconds(1), Period.ofDays(1), ZoneId.systemDefault(), Thread.State.NEW, String.class);
        CascadeQueryService service = new CascadeQueryService();
        assertSame(bean, fill(service, bean));
        for (Object value : bean.values) {
            assertSame(value, fill(service, value));
        }
        service.register(BigDecimal.class, BigDecimal.class, value -> value.add(BigDecimal.ONE));
        assertEquals(BigDecimal.TEN.add(BigDecimal.ONE), service.query(BigDecimal.TEN, BigDecimal.class));
    }

    @Test
    void collectionConverterHasPriorityOverElementConverter() {
        AtomicInteger elementCalls = new AtomicInteger();
        CascadeQueryService service = new CascadeQueryService()
                .register(Box.class, box -> elementCalls.incrementAndGet());
        Box<String> replacement = new Box<>();
        service.registerCollectionConverter(ArrayList.class, List.class, Box.class,
                list -> Collections.singletonList(replacement));
        service.register(Marker.class, marker -> marker.filled = true);
        Boxes bean = new Boxes();
        bean.boxes.add(new Box<>());
        fill(service, bean);
        assertSame(replacement, bean.boxes.get(0));
        assertTrue(replacement.marker.filled);
        assertEquals(0, elementCalls.get());
    }

    @Test
    void directCollectionConverterHasPriorityAndRecursesIntoResult() {
        CascadeQueryService service = new CascadeQueryService();
        Child child = new Child();
        service.register(List.class, List.class, list -> Collections.singletonList(child));
        service.register(Marker.class, marker -> marker.filled = true);
        Nested bean = new Nested();
        bean.children = new ArrayList<>();
        // 普通转换器按运行时源类型匹配，而非声明类型。
        service.register(ArrayList.class, List.class, list -> Collections.singletonList(Collections.singletonList(child)));
        fill(service, bean);
        assertTrue(child.marker.filled);
    }

    @Test
    void selfReferencingCollectionsAndRepeatedQueriesAreSafe() {
        Values bean = new Values();
        bean.values = new ArrayList<>();
        bean.values.add(bean.values);
        Child child = new Child();
        bean.values.add(child);
        AtomicInteger calls = new AtomicInteger();
        CascadeQueryService service = new CascadeQueryService().register(Marker.class, marker -> calls.incrementAndGet());
        fill(service, bean);
        fill(service, bean);
        assertEquals(2, calls.get());
    }
}
