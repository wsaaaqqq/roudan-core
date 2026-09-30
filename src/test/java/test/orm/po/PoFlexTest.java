package test.orm.po;

import org.junit.jupiter.api.*;
import org.xht.rr.RR;
import org.xht.xdb.Xdb;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.dao.BaseDao;
import test.support.H2TestScope;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PoFlexTest {
    private H2TestScope scope;
    private BaseDao<PoFlex> dao;

    @BeforeEach
    void setUp() throws Exception {
        scope = new H2TestScope(OrmType.MYBATIS_FLEX);
        Xdb.sql("CREATE TABLE T_TEST (ID VARCHAR(32) PRIMARY KEY, NAME VARCHAR(255), "
                + "CODE VARCHAR(255), TYPE VARCHAR(4000), IDX INT)").executeUpdate();
        dao = RR.dao().baseDao(PoFlex.class, scope.getDatasourceName());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (scope != null) scope.close();
    }

    @Test
    void singleSaveAndUpdateKeepNestedValues() {
        PoFlex po = complex("one");
        dao.save(po);
        assertEquals(po, dao.getById("one"));
        po.setType(Collections.singletonList(complex("nested")));
        dao.update(po);
        assertEquals(po, dao.getById("one"));
    }

    @Test
    void saveOrUpdateCoversEmptyAndNonEmptyTables() {
        PoFlex first = complex("one");
        dao.saveOrUpdate(first);
        assertEquals(first, dao.getById("one"));
        first.setType(Collections.singletonList(complex("changed")));
        PoFlex second = complex("two");
        dao.saveOrUpdate(Arrays.asList(first, second), 1, false);
        assertEquals(first, dao.getById("one"));
        assertEquals(second, dao.getById("two"));
    }

    @Test
    void batchSaveAndUpdatePreserveNullAndNestedLists() {
        PoFlex first = complex("one");
        PoFlex second = complex("two").setType(null);
        dao.save(Arrays.asList(first, second), 1);
        assertEquals(first, dao.getById("one"));
        assertEquals(second, dao.getById("two"));
        first.setType(null);
        second.setType(Collections.singletonList(complex("nested")));
        dao.update(Arrays.asList(first, second), 1, false);
        assertEquals(first, dao.getById("one"));
        assertEquals(second, dao.getById("two"));
    }

    private static PoFlex complex(String id) {
        PoFlex po = new PoFlex();
        po.setId(id);
        po.setName("3");
        po.setCode("3");
        po.setType(Arrays.asList(new PoFlex().setId("1")
                        .setName("1")
                        .setCode("1")
                        .setType(Arrays.asList(new PoFlex().setId("1").setName("1").setCode("1"))),
                new PoFlex().setId("2")
                        .setName("2")
                        .setCode("2")
                        .setType(Arrays.asList(new PoFlex().setId("2").setName("2").setCode("2")))));
        po.setIdx(3);
        return po;
    }
}
