package test.orm.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import test.Dbs;

import java.util.Arrays;

@Slf4j
public class DaoTest {
    private final Dao dao = new Dao();

    @BeforeEach
    public void before() {
        Dbs.h2();
        XdbConfig.setOrmType(OrmType.JPA);
        //        DDLExecutor.createTable(PoJPA.class, ""h2"");
    }

    @Test
    public void testSave() {
        dao.delete(EE.cc(1));
        dao.delete(EE.cc(2));
        try {
            dao.save(EE.cc(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //        dao.save(EE.cc(2));
    }

    @Test
    public void testSaveBatch() {
        dao.delete(EE.cc(1));
        //        dao.deleteById("id_2");
        dao.saveOrUpdate(Arrays.asList(EE.cc(1), EE.cc(2), EE.cc(3)), true);
    }

}
