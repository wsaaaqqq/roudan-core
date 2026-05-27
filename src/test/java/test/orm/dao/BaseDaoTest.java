package test.orm.dao;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.rr.RR;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.dao.BaseDao;
import test.Dbs;
import test.orm.po.PoJPA;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class BaseDaoTest {

    private BaseDao<PoJPA> dao;

    @BeforeEach
    public void setUp() {
        Dbs.h2();
        XdbConfig.setShowSql(false);
        XdbConfig.setOrmType(OrmType.JPA);
        dao = RR.dao().baseDao(PoJPA.class, "h2");
        List<PoJPA> all = dao.listAll();
        dao.delete(all, 100);
        List<PoJPA> pos = IntStream.range(1, 101).mapToObj(BaseDaoTest::init).collect(Collectors.toList());
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
        testEntity.setCode("bm" + i);
        testEntity.setType("type" + i);
        return testEntity;
    }

    @Test
    public void find_by_name() {
        List<PoJPA> result = dao.listAll();
        System.out.println(result.size());
        Assertions.assertEquals(100, result.size());
    }

}
