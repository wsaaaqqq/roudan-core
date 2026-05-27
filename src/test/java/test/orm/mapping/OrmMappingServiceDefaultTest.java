package test.orm.mapping;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.mapping.OrmMappingServiceDefault;
import test.orm.po.PoFlex;

public class OrmMappingServiceDefaultTest {

    OrmMappingServiceDefault ormMappingServiceDefault = new OrmMappingServiceDefault();

    @Test
    public void getTableNameByBeanClass() {
        Assertions.assertEquals("PO_FLEX", ormMappingServiceDefault.getTableNameByBeanClass(PoFlex.class));
    }

    @Test
    public void getColNameByBeanClass() {
        Assertions.assertEquals("ID", ormMappingServiceDefault.getColNameByBeanClass(PoFlex.class, "id"));
    }

    @Test
    public void getIdFieldNameByBeanClass() {
        Assertions.assertEquals("id", ormMappingServiceDefault.getIdFieldNameByBeanClass(PoFlex.class));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol() {
        Assertions.assertTrue(ormMappingServiceDefault.isIgnoreCol(new PoFlex(), PoFlex.class.getDeclaredField("serialVersionUID")));
    }
}
