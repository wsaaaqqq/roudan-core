package test.orm.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import test.orm.po.PoFlex;
import test.orm.po.PoJPA;
import test.orm.po.PoJimmer;
import test.orm.po.PoJimmerFactory;

public class OrmAnnoUtilTest {
    @Test
    public void getTableName() {
//        XdbConfig.setOrmType(OrmType.JPA);
//        Assertions.assertEquals("T_TEST", OrmAnnoUtil.getTableName(new PoJPA()));
//        XdbConfig.setOrmType(OrmType.MYBATIS_FLEX);
//        Assertions.assertEquals("T_TEST", OrmAnnoUtil.getTableName(new PoFlex()));
        XdbConfig.setOrmType(OrmType.JIMMER);
        PoJimmer t = PoJimmerFactory.create();
        Assertions.assertEquals("T_TEST", OrmAnnoUtil.getTableName(t));
    }

    @Test
    public void getTableNameByBeanClass() {
    }

    @Test
    public void getColName() {
    }

    @Test
    public void getTClass() {
    }

    @Test
    public void getColNameByBeanClass() {
    }

    @Test
    public void getColNameByGetter() {
    }

    @Test
    public void getIdColName() {
    }

    @Test
    public void getIdColNameByBeanClass() {
    }

    @Test
    public void isIgnoreCol() {
    }

    @Test
    public void isNotIgnoreCol() {
    }
}
