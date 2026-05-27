package test.orm.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.util.AnnoUtil;
import test.orm.po.PoJPA;

import java.util.Objects;

public class AnnoUtilJPATest {

    @Test
    public void getClassAnnoValue() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValue(new PoJPA(), "javax.persistence.Table", "name")
        );
    }

    @Test
    public void getClassAnnoValueByBeanClass() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValueByBeanClass(PoJPA.class, "javax.persistence.Table", "name")
        );
    }

    @Test
    public void testGetClassAnnoValueByBeanClass() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValueByBeanClass("test.orm.po.PoJPA", "javax.persistence.Table", "name")
        );
    }

    @Test
    public void getFieldsByBeanClass() {
        Assertions.assertFalse(Objects
                                   .requireNonNull(AnnoUtil.getFieldsByBeanClass(PoJPA.class, "javax.persistence.Id"))
                                   .isEmpty());
    }

    @SneakyThrows
    @Test
    public void getAnnoValueOfField() {
        Assertions.assertEquals(
                "ID",
                AnnoUtil.getAnnoValueOfField(PoJPA.class.getDeclaredField("id"), "javax.persistence.Column", "name")
        );
    }

    @Test
    public void getField() {
        Assertions.assertNotNull(AnnoUtil.getField(new PoJPA(), "javax.persistence.Id"));
    }

    @Test
    public void testGetField() {
        Assertions.assertNotNull(AnnoUtil.getField("test.orm.po.PoJPA", "javax.persistence.Id"));
    }

    @Test
    public void getFieldByBeanClass() {
        Assertions.assertNotNull(AnnoUtil.getFieldByBeanClass(PoJPA.class, "javax.persistence.Id"));
    }

    @SneakyThrows
    @Test
    public void isFieldAnnotated() {
        Assertions.assertTrue(AnnoUtil.isFieldAnnotated(PoJPA.class.getDeclaredField("id"), "javax.persistence.Id"));
    }
}
