package test.orm.util;

import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.util.OrmAnnoUtil;

import static org.junit.jupiter.api.Assertions.assertSame;

class OrmAnnoUtilRegressionTest {
    static class Entity { }
    static class GenericEntity<T> { }
    interface View<T> { }

    @Test
    void anonymousEntityResolvesSuperclass() {
        assertSame(Entity.class, OrmAnnoUtil.getTClass(new Entity() { }));
        assertSame(GenericEntity.class, OrmAnnoUtil.getTClass(new GenericEntity<String>() { }));
    }

    @Test
    void anonymousInterfacesResolveRawClass() {
        assertSame(View.class, OrmAnnoUtil.getTClass(new View<String>() { }));
        assertSame(Runnable.class, OrmAnnoUtil.getTClass(new Runnable() {
            @Override
            public void run() { }
        }));
    }

    @Test
    void anonymousObjectWithoutInterfacesIsSafe() {
        Object object = new Object() { };
        assertSame(object.getClass(), OrmAnnoUtil.getTClass(object));
    }

    @Test
    void ordinaryInstancesAndClassArgumentsKeepTheirTypes() {
        assertSame(Entity.class, OrmAnnoUtil.getTClass(new Entity()));
        assertSame(Entity.class, OrmAnnoUtil.getTClass(Entity.class));
    }
}
