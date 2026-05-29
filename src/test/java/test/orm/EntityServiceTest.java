package test.orm;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.Xdb;
import org.xht.xdb.orm.EntityService;
import org.xht.xdb.orm.EntityServiceImp;
import org.xht.xdb.sql.SqlBuild;
import org.xht.xdb.util.ListUtil;
import org.xht.xdb.vo.Groups;
import org.xht.xdb.vo.Orders;
import org.xht.xdb.vo.PageResult;
import org.xht.xdb.vo.WheresBean;
import test.Dbs;
import test.orm.po.PoJPA;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class EntityServiceTest {
    static {
        // 确保ORM类型设置为JPA，以便正确读取@Table注解
        org.xht.xdb.XdbConfig.setOrmType(org.xht.xdb.enums.OrmType.JPA);
    }

    private final String db = Dbs.h2().getDatasourceName();
    private final EntityServiceImp<PoJPA> dao = new EntityServiceImp<>(PoJPA.class, db);

    public void before() {
        //        XdbConfig.setShowSqlFlagOfArgsInComment(false);
    }

    @Test
    public void test23() {
        String id = null;
        Xdb.sqlPage().sqlSelect("select * ").sqlMain(
                "from t1 left join t2 on t2.id=t1.pid",
                SqlBuild.init().addWhereStart().addIfNotEmpty("and t2.id=:pid", "pid", id)
        ).sqlOrder("order by t1.id ,t2.id").pageIndex(1).pagePerSize(10).debug();
    }


    @Test
    public void delete() {
        List<String> ids = dao.ids(String.class);
        log.info("ids size: {}", ids.size());
        List<PoJPA> pos = dao.listAll();
        PoJPA po;
        if (pos != null && !pos.isEmpty()) {
            po = pos.get(0);

        } else {
            po = new PoJPA();
            po.setId("1");
            dao.save(po);
        }
        log.info("{}", po);
        assertTrue(dao.exist(po));
        dao.delete(po);
        assertFalse(dao.exist(po));
        assertTrue(dao.notExist(po));
        PoJPA test = new PoJPA().setId("test");
        dao.saveOrUpdate(Arrays.asList(test, po), true);
        //        dao.saveOrUpdate(rhzd);
        //        dao.save(rhzd);
        assertTrue(dao.exist(po));
        log.info("{}", dao.getById(po.getId()));
        assertTrue(dao.exist(test));
        log.info("{}", dao.getById("test"));
        dao.delete(test);
        assertFalse(dao.exist(test));
        log.info("{}", dao.getById("test"));

    }

    @Test
    public void exist() {
        PoJPA poJPA = dao.getById("1");
        if (poJPA != null) {
            assertTrue(dao.existId("1"));
            assertTrue(dao.exist(new PoJPA().setId("1")));
            assertTrue(dao.exist(poJPA));
            assertTrue(dao.existId(poJPA.getId()));
        }
    }

    @Test
    public void exist2() {
        dao.saveOrUpdate(new PoJPA().setId("1"), true);
        dao.deleteById("_xxx1");
        assertTrue(dao.existId("1"));
        assertFalse(dao.existId("_xxx1"));
        dao.update(new PoJPA().setId("_xxx1"), false, "1");
        assertFalse(dao.existId("1"));
        assertTrue(dao.existId("_xxx1"));
    }

    @Test
    public void test() {
        dao.list(WheresBean.init(PoJPA.class)
                           .select(PoJPA::getName)
                           .select(PoJPA::getType)
                           .and()
                           .sub(e -> e.or()
                                      .sub(e1 -> e1.startWith(PoJPA::getType, "TYPE_1")
                                                   .sub(e2 -> e2.startWith(PoJPA::getType, "TYPE_2"))))
                           .sub(e -> e.contain(PoJPA::getType, "MBZT"))).forEach(rhzd -> log.info("{}", rhzd));
    }

    @Test
    public void test0() {
        dao.list(WheresBean.init(PoJPA.class)
                           .select(PoJPA::getName)
                           .select(PoJPA::getType)
                           .startWith(PoJPA::getType, "TYPE_")
                           .contain(PoJPA::getType, "MBZT")).forEach(rhzd -> log.info("{}", rhzd));
    }

    @Test
    public void test2() {
        PageResult<PoJPA> rhzdPageResult = dao.sqlPage()
                                              .sqlSelect("select *")
                                              .sqlMain(" from T_TEST")
                                              .sqlOrder("order by idx")
                                              .pageIndex(1)
                                              .pagePerSize(23)
                                              .resultBean(PoJPA.class);
        System.out.println(rhzdPageResult);
    }

    @Test
    public void test3() {
        List<PoJPA> poJPAS = dao.listAll();
        Orders<PoJPA> orders =
                Orders.of(PoJPA.class).withCN(PoJPA::getType, true, true).withCN(PoJPA::getName, true, true)
                //                .with(Rhzd::getIdx, true,false)
                ;
        ListUtil.sort(poJPAS, orders).forEach(System.out::println);
    }

    @Test
    public void test4() {
        List<PoJPA> poJPAS = dao.listAll();
        TreeMap<PoJPA, List<PoJPA>> group =
                ListUtil.group(poJPAS, Groups.of(PoJPA.class).with(PoJPA::getName).with(PoJPA::getIdx));
        group.forEach((rhzd, list) -> {
            System.out.println(rhzd + "        ----------------------------------" + list.size());
            System.out.println("m1:");
            list.forEach(System.out::println);
            System.out.println("m2:");
            for (PoJPA t : list) {
                System.out.println(t);
            }
        });
    }

    @Test
    public void test5() {
        List<PoJPA> poJPAS = dao.listAll();
        dao.saveOrUpdate(poJPAS, true);
        poJPAS.forEach(e -> log.info("{}", e));
    }

    @Test
    public void test6() {
        List<PoJPA> poJPAS = dao.listAll();
        List<Object> ids = poJPAS.stream().map(PoJPA::getId).collect(Collectors.toList());
        dao.deleteById(ids, 2);
        System.out.println(dao.listAll().size());
        dao.saveOrUpdate(poJPAS, true);
        System.out.println(dao.listAll().size());
        poJPAS.forEach(e -> log.info("{}", e));
    }

    @Test
    public void test7() {
        int max = 100000;
        List<PoJPA> poJPAS = IntStream.range(0, max)
                                      .mapToObj(i -> new PoJPA().setId("test-" + i).setName("test-" + i).setType("test"))
                                      .collect(Collectors.toList());
        System.out.println(dao.ids(String.class).size());
        dao.saveOrUpdate(poJPAS, true);
        System.out.println(dao.ids(String.class).size());
    }

    @Test
    public void test8() {
        List<String> ids = ids();
        List<Object> deleteIds = ids.stream().filter(id -> id.startsWith("test")).collect(Collectors.toList());
        System.out.println(deleteIds.size());
        dao.deleteById(deleteIds, 10000);
        ids();
    }

    private List<String> ids() {
        List<String> ids = dao.ids(String.class);
        log.info("{}", ids.size());
        return ids;
    }

    @Test
    public void of() {
        Assertions.assertNotNull(EntityService.of(PoJPA.class, db));
    }

    @Test
    public void datasource() {
        Assertions.assertNotNull(EntityService.of(PoJPA.class, db).datasource(db));
    }

    @Test
    public void saveOrUpdate() {
        PoJPA p1 = new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1);
        EntityService.of(PoJPA.class, db).saveOrUpdate(p1);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
    }

    @Test
    public void testSaveOrUpdate() {
        PoJPA p1 = new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1);
        EntityService.of(PoJPA.class, db).saveOrUpdate(p1);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
        EntityService.of(PoJPA.class, db).deleteById("1");
        Assertions.assertNull(EntityService.of(PoJPA.class, db).getById("1"));
        EntityService.of(PoJPA.class, db)
                     .saveOrUpdate(new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1));
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
    }

    @Test
    public void testSaveOrUpdate1() {
        PoJPA p1 = new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1);
        PoJPA p2 = new PoJPA().setId("2").setName("2").setCode("2").setType("2").setIdx(2);
        List<PoJPA> list = Arrays.asList(p1, p2);
        EntityService.of(PoJPA.class, db).saveOrUpdate(list, true);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
        EntityService.of(PoJPA.class, db).deleteById("1");
        Assertions.assertNull(EntityService.of(PoJPA.class, db).getById("1"));
        EntityService.of(PoJPA.class, db).saveOrUpdate(list, true);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
    }

    @Test
    public void testSaveOrUpdate2() {
        PoJPA p1 = new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1);
        PoJPA p2 = new PoJPA().setId("2").setName("2").setCode("2").setType("2").setIdx(2);
        List<PoJPA> list = Arrays.asList(p1, p2);
        EntityService.of(PoJPA.class, db).saveOrUpdate(list, true);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
        EntityService.of(PoJPA.class, db).deleteById("1");
        Assertions.assertNull(EntityService.of(PoJPA.class, db).getById("1"));
        EntityService.of(PoJPA.class, db).saveOrUpdate(list, true);
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getId());
        p1.setType("111");
        p1.setCode(null);
        EntityService.of(PoJPA.class, db).saveOrUpdate(p1, true);
        Assertions.assertEquals("111", EntityService.of(PoJPA.class, db).getById("1").getType());
        Assertions.assertEquals("1", EntityService.of(PoJPA.class, db).getById("1").getCode());
    }

    @Test
    public void testSaveOrUpdate3() {
        Function<Integer, PoJPA> createPo = (i) -> {
            String _i = i.toString();
            return new PoJPA().setId(_i).setName(_i).setCode(_i).setType(_i).setIdx(i);
        };
        List<PoJPA> list = IntStream.range(1, 10000).mapToObj(createPo::apply).collect(Collectors.toList());
        EntityService.of(PoJPA.class, db).saveOrUpdate(list, true);
    }

    @Test
    public void testSaveOrUpdate4() {
        Function<Integer, PoJPA> createPo = (i) -> {
            String _i = i.toString();
            return new PoJPA().setId(_i).setName(_i).setCode(_i).setType(_i).setIdx(i);
        };
        List<PoJPA> list = IntStream.range(1, 1000000).mapToObj(createPo::apply).collect(Collectors.toList());
        EntityService.of(PoJPA.class, db).delete(list, 1000);
    }

    @Test
    public void save() {
        PoJPA p1 = new PoJPA().setId("1").setName("1").setCode("1").setType("1").setIdx(1);
        EntityService.of(PoJPA.class, db).delete(p1);
        Assertions.assertNull(EntityService.of(PoJPA.class, db).getById("1"));
        EntityService.of(PoJPA.class, db).save(p1);
        Assertions.assertNotNull(EntityService.of(PoJPA.class, db).getById("1"));
        EntityService.of(PoJPA.class, db).update(p1);
        Assertions.assertNotNull(EntityService.of(PoJPA.class, db).getById("1"));
    }

    @Test
    public void testSave() {
    }

    @Test
    public void update() {
    }

    @Test
    public void testUpdate() {
    }

    @Test
    public void testUpdate1() {
    }

    @Test
    public void testUpdate2() {
    }

    @Test
    public void testDelete() {
    }

    @Test
    public void testDelete1() {
    }

    @Test
    public void deleteById() {
    }

    @Test
    public void testDeleteById() {
    }

    @Test
    public void existId() {
    }

    @Test
    public void testExist() {
    }

    @Test
    public void notExistId() {
    }

    @Test
    public void notExist() {
    }

    @Test
    public void getByIds() {
    }

    @Test
    public void testGetByIds() {
    }

    @Test
    public void testGetByIds1() {
    }

    @Test
    public void testGetByIds2() {
    }

    @Test
    public void testGetByIds3() {
    }

    @Test
    public void testGetByIds4() {
    }

    @Test
    public void infos() {
    }

    @Test
    public void testInfos() {
    }

    @Test
    public void getByIdOpt() {
    }

    @Test
    public void getById() {
    }

    @Test
    public void testGetById() {
    }

    @Test
    public void testIds() {
    }

    @Test
    public void testIds1() {
    }

    @Test
    public void testIds2() {
    }

    @Test
    public void asListAll() {
    }

    @Test
    public void list() {
    }

    @Test
    public void testList() {
    }

    @Test
    public void sql() {
    }

    @Test
    public void sqlPage() {
    }

    @Test
    public void testList1() {
    }

    @Test
    public void testList2() {
    }

    @Test
    public void page() {
    }

    @Test
    public void testPage() {
    }

    @Test
    public void testPage1() {
    }

    @Test
    public void testPage2() {
    }

    @Test
    public void count() {
    }

    @Test
    public void testCount() {
    }

    @Test
    public void testCount1() {
    }
}
