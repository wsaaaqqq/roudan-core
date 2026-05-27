package test.orm.mapping;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.mapping.OrmMappingServiceMybatisFlex;
import test.orm.po.PoFlex;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OrmMappingServiceMybatisFlexTest {
    OrmMappingServiceMybatisFlex ormMappingServiceMybatisFlex = new OrmMappingServiceMybatisFlex();
    @Test
    public void getTableNameByBeanClass() {
        assertEquals("T_TEST", ormMappingServiceMybatisFlex.getTableNameByBeanClass(PoFlex.class));
    }

    @Test
    public void getColNameByBeanClass() {
        assertEquals("ID", ormMappingServiceMybatisFlex.getColNameByBeanClass(PoFlex.class, "id"));
    }

    @Test
    public void getIdFieldNameByBeanClass() {
        assertEquals("id", ormMappingServiceMybatisFlex.getIdFieldNameByBeanClass(PoFlex.class));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol() {
        Assertions.assertFalse(ormMappingServiceMybatisFlex.isIgnoreCol(new PoFlex(), PoFlex.class.getDeclaredField("id")));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol2() {
        Assertions.assertTrue(ormMappingServiceMybatisFlex.isIgnoreCol(new PoFlex(), PoFlex.class.getDeclaredField("serialVersionUID")));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol3() {
        Field field = null;
        Assertions.assertTrue(ormMappingServiceMybatisFlex.isIgnoreCol(new PoFlex(), field));
    }
}
