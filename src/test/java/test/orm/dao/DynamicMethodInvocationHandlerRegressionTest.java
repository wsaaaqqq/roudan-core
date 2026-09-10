package test.orm.dao;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.DbType;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.dao.BaseDao;
import org.xht.xdb.orm.dao.BaseDaoImpl;
import org.xht.xdb.orm.dao.DynamicMethodInvocationHandler;
import org.xht.xdb.orm.dao.DynamicSqlBuilder;
import org.xht.xdb.sql.XDataSource;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicMethodInvocationHandlerRegressionTest {
    private OrmType previousOrmType;
    private Map<String, XDataSource> dataSources;
    private Map<String, XDataSource> previousDataSources;
    private Field defaultDataSourceField;
    private XDataSource previousDefaultDataSource;
    private ThreadLocal<XDataSource> selectedDataSource;
    private XDataSource previousSelectedDataSource;
    private DynamicMethodInvocationHandler<Entity> handler;

    @Table(name = "DAO_RETURN_REGRESSION")
    public static class Entity {
        @Id
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    public interface Queries extends BaseDao<Entity> {
        Object get_by_id(String id);
        Optional<Entity> read_by_id(String id);
        Object find_by_id(String id);
    }
    public interface IntCount { int count_by_id(String id); }
    public interface IntegerCount { Integer count_by_id(String id); }
    public interface ShortCount { short count_by_id(String id); }
    public interface BoxedShortCount { Short count_by_id(String id); }
    public interface LongCount { long count_by_id(String id); }
    public interface BoxedLongCount { Long count_by_id(String id); }
    public interface BigCount { BigInteger count_by_id(String id); }
    public interface NumberCount { Number count_by_id(String id); }
    public interface ObjectCount { Object count_by_id(String id); }
    public interface SerializableCount { Serializable count_by_id(String id); }

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws ReflectiveOperationException {
        // 公共 API 无法恢复空默认数据源或未选择状态，测试内保存原始引用。
        dataSources = (Map<String, XDataSource>) xdbField("dataSources").get(Xdb.init());
        defaultDataSourceField = xdbField("xDataSourceDefault");
        selectedDataSource = (ThreadLocal<XDataSource>) xdbField("xDataSourceSelected").get(Xdb.init());
        previousDefaultDataSource = (XDataSource) defaultDataSourceField.get(Xdb.init());
        previousSelectedDataSource = selectedDataSource.get();
        previousDataSources = new HashMap<>(dataSources);
        previousOrmType = XdbConfig.getOrmType();
        XdbConfig.setOrmType(OrmType.JPA);
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:dao_return_regression;DB_CLOSE_DELAY=-1");
        Xdb.init().addDataSource(source, DbType.H2, "dao-return-regression");
        Xdb.selectDataSourceByName("dao-return-regression");
        Xdb.sql("DROP TABLE IF EXISTS DAO_RETURN_REGRESSION").executeUpdate();
        Xdb.sql("CREATE TABLE DAO_RETURN_REGRESSION (ID VARCHAR(32) PRIMARY KEY)").executeUpdate();
        Xdb.sql("INSERT INTO DAO_RETURN_REGRESSION VALUES ('present')").executeUpdate();
        handler = new DynamicMethodInvocationHandler<>(new BaseDaoImpl<>(Entity.class, "dao-return-regression"),
                new DynamicSqlBuilder(Entity.class));
    }

    @AfterEach
    public void tearDown() throws IllegalAccessException {
        if (previousDataSources == null) {
            return;
        }
        XdbConfig.setOrmType(previousOrmType);
        dataSources.clear();
        dataSources.putAll(previousDataSources);
        defaultDataSourceField.set(Xdb.init(), previousDefaultDataSource);
        if (previousSelectedDataSource == null) {
            selectedDataSource.remove();
        } else {
            selectedDataSource.set(previousSelectedDataSource);
        }
        assertEquals(previousDataSources, dataSources);
        assertSame(previousDefaultDataSource, defaultDataSourceField.get(Xdb.init()));
        assertSame(previousSelectedDataSource, selectedDataSource.get());
        assertSame(previousOrmType, XdbConfig.getOrmType());
    }

    private static Field xdbField(String name) throws NoSuchFieldException {
        Field field = Xdb.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @Test
    public void objectQueriesReturnEntityListAndNullWithoutOptionalWrapping() {
        Queries dao = BaseDaoImpl.createProxy(Queries.class, "dao-return-regression");
        Object entity = dao.get_by_id("present");
        assertInstanceOf(Entity.class, entity);
        assertEquals("present", ((Entity) entity).getId());
        assertNull(dao.get_by_id("missing"));
        Object rows = dao.find_by_id("present");
        assertInstanceOf(List.class, rows);
        assertEquals(1, ((List<?>) rows).size());
    }

    @Test
    public void optionalQueriesWrapPresentAndMissingEntities() {
        Queries dao = BaseDaoImpl.createProxy(Queries.class, "dao-return-regression");
        assertEquals("present", dao.read_by_id("present").get().getId());
        assertEquals(Optional.empty(), dao.read_by_id("missing"));
    }

    @Test
    public void countUsesExactNumericReturnTypes() throws Throwable {
        assertCount(IntCount.class, Integer.valueOf(1));
        assertCount(IntegerCount.class, Integer.valueOf(1));
        assertCount(ShortCount.class, Short.valueOf((short) 1));
        assertCount(BoxedShortCount.class, Short.valueOf((short) 1));
        assertCount(LongCount.class, Long.valueOf(1));
        assertCount(BoxedLongCount.class, Long.valueOf(1));
        assertCount(BigCount.class, BigInteger.ONE);
    }

    @Test
    public void countSupertypesRetainLongResult() throws Throwable {
        assertCount(NumberCount.class, Long.valueOf(1));
        assertCount(ObjectCount.class, Long.valueOf(1));
        assertCount(SerializableCount.class, Long.valueOf(1));
    }

    private void assertCount(Class<?> declaration, Object expected) throws Throwable {
        Object actual = handler.invoke(null, declaration.getMethod("count_by_id", String.class),
                new Object[]{"present"});
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected, actual);
    }
}
