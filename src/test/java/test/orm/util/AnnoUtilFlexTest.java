package test.orm.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.util.AnnoUtil;
import test.orm.po.PoFlex;

import java.util.Objects;

public class AnnoUtilFlexTest {

    @Test
    public void getClassAnnoValue() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValue(new PoFlex(), "com.mybatisflex.annotation.Table", "value")
        );
    }

    @Test
    public void getClassAnnoValueByBeanClass() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValueByBeanClass(PoFlex.class, "com.mybatisflex.annotation.Table", "value")
        );
    }

    @Test
    public void testGetClassAnnoValueByBeanClass() {
        Assertions.assertEquals(
                "T_TEST",
                AnnoUtil.getClassAnnoValueByBeanClass("test.orm.po.PoFlex", "com.mybatisflex.annotation.Table", "value")
        );
    }

    @Test
    public void getFieldsByBeanClass() {
        Assertions.assertFalse(Objects
                                   .requireNonNull(AnnoUtil.getFieldsByBeanClass(PoFlex.class, "com.mybatisflex.annotation.Id"))
                                   .isEmpty());
    }

    @SneakyThrows
    @Test
    public void getAnnoValueOfField() {
        Assertions.assertEquals(
                "ID",
                AnnoUtil.getAnnoValueOfField(PoFlex.class.getDeclaredField("id"), "com.mybatisflex.annotation.Column", "value")
        );
    }

    @Test
    public void getField() {
        Assertions.assertNotNull(AnnoUtil.getField(new PoFlex(), "com.mybatisflex.annotation.Id"));
    }

    @Test
    public void testGetField() {
        Assertions.assertNotNull(AnnoUtil.getField("test.orm.po.PoFlex", "com.mybatisflex.annotation.Id"));
    }

    @Test
    public void getFieldByBeanClass() {
        Assertions.assertNotNull(AnnoUtil.getFieldByBeanClass(PoFlex.class, "com.mybatisflex.annotation.Id"));
    }

    @SneakyThrows
    @Test
    public void isFieldAnnotated() {
        Assertions.assertFalse(AnnoUtil.isFieldAnnotated(PoFlex.class.getDeclaredField("name"), "com.mybatisflex.annotation.Id"));
    }
    @SneakyThrows
    @Test
    public void isFieldAnnotated2() {
        Assertions.assertFalse(AnnoUtil.isFieldAnnotated(PoFlex.class.getDeclaredField("name"), "com.mybatisflex.annotation.Id"));
    }
    @SneakyThrows
    @Test
    public void isFieldAnnotated3() {
        Assertions.assertFalse(AnnoUtil.isFieldAnnotated(PoFlex.class.getDeclaredField("name"), "com.mybatisflex.annotation.Column"));
    }
}
