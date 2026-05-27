package test.orm.mapping;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.mapping.OrmMappingServiceJpa;
import test.orm.po.PoFlex;
import test.orm.po.PoJPA;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OrmMappingServiceJpaTest {
    private final OrmMappingServiceJpa ormServiceJpa = new OrmMappingServiceJpa();
    @Test
    public void getTableNameByBeanClass() {
        assertEquals("T_TEST", ormServiceJpa.getTableNameByBeanClass(PoJPA.class));
    }

    @Test
    public void getColNameByBeanClass() {
        assertEquals("ID", ormServiceJpa.getColNameByBeanClass(PoJPA.class, "id"));
    }

    @Test
    public void getIdFieldNameByBeanClass() {
        assertEquals("id", ormServiceJpa.getIdFieldNameByBeanClass(PoJPA.class));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol() {
        Assertions.assertFalse(ormServiceJpa.isIgnoreCol(new PoJPA(), PoJPA.class.getDeclaredField("id")));
    }
    @SneakyThrows
    @Test
    public void isIgnoreCol2() {
        Assertions.assertTrue(ormServiceJpa.isIgnoreCol(new PoFlex(), PoFlex.class.getDeclaredField("serialVersionUID")));
    }
    @SneakyThrows
    @Test
    public void isIgnoreCol3() {
        Field field = null;
        Assertions.assertTrue(ormServiceJpa.isIgnoreCol(new PoJPA(), field));
    }
}
