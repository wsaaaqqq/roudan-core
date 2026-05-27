package test.orm.dao;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.rr.RR;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import test.Dbs;
import test.orm.po.PoJPA;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class XXDaoTest {

    private XXDao dao;

    @BeforeEach
    public void setUp() {
        Dbs.h2();
        XdbConfig.setShowSql(false);
        XdbConfig.setOrmType(OrmType.JPA);
        //        dao = BaseDaoImpl.createProxy(XXDao.class, PoJPA.class, "h2");
        dao = RR.dao().of(XXDao.class);
        List<PoJPA> all = dao.listAll();
        dao.delete(all, 100);
        List<PoJPA> pos = IntStream.range(1, 101).mapToObj(XXDaoTest::init).collect(Collectors.toList());
        dao.save(pos, 100);
        System.out.println(pos.get(0));
        System.out.println(pos.get(pos.size() - 1));
        XdbConfig.setShowSql(true);
    }

    @NotNull
    private static PoJPA init(int i) {
        PoJPA testEntity = new PoJPA();
        testEntity.setId("id" + i);
        testEntity.setName("name" + i);
        testEntity.setIdx(i);
        testEntity.setCode("code" + i);
        testEntity.setType("type" + i);
        return testEntity;
    }

    @Test
    public void find_by_name() {
        List<PoJPA> result = dao.find_by_name("name1");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void count_by_name() {
        Long count = dao.count_by_name("name1");
        Assertions.assertEquals(Long.valueOf(1), count);
    }

    @Test
    public void find_by_name_like() {
        List<PoJPA> result = dao.find_by_name_like("0");
        Assertions.assertEquals(10, result.size());
        result.stream().map(PoJPA::getName).forEach(System.out::println);
    }

    @Test
    public void exists_by_name() {
        Assertions.assertTrue(dao.exists_by_name("name1"));
        dao.delete_by_name("name1");
        Assertions.assertFalse(dao.exists_by_name("name1"));
    }

    @Test
    public void test_default_method() {
        Assertions.assertEquals(2, dao.count_by_name_or_name("name1", "name2"));
        dao.delete_by_name_or_id("name1", "id2");
        Assertions.assertEquals(2, dao.count_by_name_or_name("name1", "name2"));
    }

    @Test
    public void find_by_name_start_with() {
        Assertions.assertEquals(12, dao.find_by_name_start_with("name1").size());
        Assertions.assertEquals(1, dao.find_by_name_start_with("name99").size());
    }

    @Test
    public void find_by_name_end_with() {
        List<PoJPA> result = dao.find_by_name_end_with("99");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void find_by_name_contain() {
        Assertions.assertEquals(0, dao.find_by_name_contain("middle").size());
        Assertions.assertEquals(1, dao.find_by_name_contain("99").size());
    }

    @Test
    public void find_by_id() {
        Assertions.assertTrue(dao.find_by_id("test16").isPresent());
    }

    @Test
    public void find_by_idx_gt() {
        Assertions.assertEquals(93, dao.find_by_idx_gt(7).size());
    }

    @Test
    public void find_by_idx_lt() {
        Assertions.assertEquals(7, dao.find_by_idx_lt(8).size());
    }

    @Test
    public void find_by_idx_gte() {
        Assertions.assertEquals(91, dao.find_by_idx_gte(10).size());
    }

    @Test
    public void find_by_idx_lte() {
        Assertions.assertEquals(5, dao.find_by_idx_lte(5).size());
    }

    @Test
    public void find_by_name_not() {
        Assertions.assertFalse(dao.find_by_name_not("greater_than_test").isEmpty());
    }

    @Test
    public void find_by_name_in() {
        List<String> nameList = Arrays.asList("name1", "name2", "name3");
        List<PoJPA> result = dao.find_by_name_in(nameList);
        Assertions.assertEquals(3, result.size());
        result.stream().map(PoJPA::getName).forEach(System.out::println);
    }

    @Test
    public void find_by_idx_between() {
        Assertions.assertEquals(3, dao.find_by_idx_between(Arrays.asList(1, 3)).size());
    }

    @Test
    public void find_by_name_or_type() {
        Assertions.assertTrue(dao.find_by_name_or_type("non_existent", "test").isEmpty());
        Assertions.assertFalse(dao.find_by_name_or_type("name1", "type1").isEmpty());
    }

    @Test
    public void find_by_idx_and_name_like() {
        List<PoJPA> result = dao.find_by_idx_and_name_like(10, "10");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("id10", result.get(0).getId());
        Assertions.assertEquals(Integer.valueOf(10), result.get(0).getIdx());
        Assertions.assertTrue(result.get(0).getName().contains("10"));
    }

    @Test
    public void find_by_idx_gt_and_name_start_with() {
        List<PoJPA> list = dao.find_by_idx_gt_and_name_start_with(11, "name1");
        Assertions.assertEquals(9, list.size());
        list.stream().map(PoJPA::getName).forEach(System.out::println);
    }
}
