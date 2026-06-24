package test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xht.rr.RR;
import org.xht.xdb.orm.dao.BaseDao;
import test.orm.po.PoJPA;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RDTest {
    private final BaseDao<PoJPA> dao = RR.dao().baseDao(PoJPA.class);
    private final int DATA_SIZE = 10000;
    private final int BATCH_SIZE = 1000;

    static {
        Dbs.h2();
    }

    @Test
    @Order(2)
    public void saveData() {
        Function<Integer, PoJPA> initPo = (i) -> new PoJPA().setId("id" + i)
                .setName("name" + i)
                .setCode("code" + i)
                .setType("type" + i)
                .setIdx(i);
        List<PoJPA> pos = IntStream.range(0, DATA_SIZE).mapToObj(initPo::apply).collect(Collectors.toList());

        dao.save(pos, BATCH_SIZE);
    }

    @Test
    @Order(3)
    public void updateData() {
        Function<Integer, PoJPA> initPo =
                (i) -> new PoJPA().setId("id" + i).setName("name" + i).setCode("CODE__" + i).setType(null).setIdx(i);
        List<PoJPA> pos = IntStream.range(0, DATA_SIZE).mapToObj(initPo::apply).collect(Collectors.toList());

        dao.update(pos, BATCH_SIZE, true);

        assertEquals(
                DATA_SIZE,
                dao.list(dao.wheres().startWith(PoJPA::getCode, "CODE__").notNull(PoJPA::getType)).size()
        );
    }

    @Test
    @Order(4)
    public void saveOrUpdateData() {
        Function<Integer, PoJPA> initPo =
                (i) -> new PoJPA().setId("id" + i).setName("name" + i).setCode("__CODE__" + i).setType(null).setIdx(i);
        List<PoJPA> pos = IntStream.range(0, DATA_SIZE).mapToObj(initPo::apply).collect(Collectors.toList());

        dao.saveOrUpdate(pos, BATCH_SIZE, false);

        assertEquals(
                DATA_SIZE,
                dao.list(dao.wheres().startWith(PoJPA::getCode, "__CODE__").isNull(PoJPA::getType)).size()
        );
    }

    @Test
    @Order(5)
    public void list() {
        assertEquals(DATA_SIZE, dao.listAll().size());
        List<PoJPA> list = dao.list(dao.wheres()
                .ge(PoJPA::getIdx, 10)
                .le(PoJPA::getIdx, 3000)
                .endWith(PoJPA::getId, "0")
                .contain(PoJPA::getName, "me"));
        assertFalse(list.isEmpty());
        assertFalse(list.stream()
                .noneMatch(i -> i.getId().endsWith("0") &&
                        i.getName().contains("me") &&
                        i.getIdx() >= 10 &&
                        i.getIdx() <= 3000));

    }

    @Test
    @Order(6)
    public void ids() {
        List<String> idsAll = dao.ids(String.class);
        assertEquals(DATA_SIZE, idsAll.size());

        Set<String> idsAll2 = dao.ids(String.class, new HashSet<>());
        assertEquals(DATA_SIZE, idsAll2.size());
    }

    @Test
    @Order(7)
    public void getByIds() {
        List<String> idsAll = IntStream.range(0, DATA_SIZE).mapToObj(i -> "id" + i).collect(Collectors.toList());
        //根据全部主键直接获取全部数据,框架会自动分配次查询并汇总，无需开发人员担心sql in查询时参数过多问题
        List<PoJPA> getByIds = dao.getByIds(idsAll, BATCH_SIZE);
        assertEquals(DATA_SIZE, getByIds.size());
        System.out.println("ids: " + getByIds.stream().map(PoJPA::getId).collect(Collectors.joining(",")));
    }

    @Test
    @Order(8)
    public void delete() {
        List<String> idsAll = IntStream.range(0, DATA_SIZE).mapToObj(i -> "id" + i).collect(Collectors.toList());
        //根据全部主键直接获取全部数据,框架会自动分配次查询并汇总，无需开发人员担心sql in查询时参数过多问题
        dao.deleteById(idsAll, BATCH_SIZE);
        assertEquals(0, dao.listAll().size());
    }

}
