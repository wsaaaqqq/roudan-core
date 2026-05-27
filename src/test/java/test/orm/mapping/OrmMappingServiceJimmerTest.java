package test.orm.mapping;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.mapping.OrmMappingServiceJimmer;
import test.orm.po.PoFlex;
import test.orm.po.PoJimmer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OrmMappingServiceJimmerTest {
    OrmMappingServiceJimmer ormMappingServiceJimmer = new OrmMappingServiceJimmer();
    @Test
    public void getTableNameByBeanClass() {
        assertEquals("T_TEST", ormMappingServiceJimmer.getTableNameByBeanClass(PoJimmer.class));
    }

    @Test
    public void getColNameByBeanClass() {
        assertEquals("ID", ormMappingServiceJimmer.getColNameByBeanClass(PoJimmer.class, "id"));
    }

    @Test
    public void getIdFieldNameByBeanClass() {
        assertEquals("id", ormMappingServiceJimmer.getIdFieldNameByBeanClass(PoJimmer.class));
    }

    @SneakyThrows
    @Test
    public void isIgnoreCol() {
        Assertions.assertFalse(ormMappingServiceJimmer.isIgnoreCol(getBean(), PoJimmer.class.getDeclaredMethod("id")));
        Assertions.assertFalse(ormMappingServiceJimmer.isIgnoreCol(getBean(), PoJimmer.class.getDeclaredMethod("name")));
        Assertions.assertTrue(ormMappingServiceJimmer.isIgnoreCol(new PoFlex(), (Field)null));
        Assertions.assertTrue(ormMappingServiceJimmer.isIgnoreCol(new PoFlex(), (Method) null));
    }


    private static PoJimmer getBean() {
        return new PoJimmer() {

            @Override
            public String id() {
                return "";
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String code() {
                return "";
            }

            @Override
            public String type() {
                return "";
            }

            @Override
            public Integer idx() {
                return 0;
            }
        };
    }
}
