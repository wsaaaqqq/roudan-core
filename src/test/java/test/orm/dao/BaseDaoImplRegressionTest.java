package test.orm.dao;

import org.junit.jupiter.api.Test;
import org.xht.xdb.orm.dao.BaseDao;
import org.xht.xdb.orm.dao.BaseDaoImpl;

import static org.junit.jupiter.api.Assertions.*;

public class BaseDaoImplRegressionTest {
    public static class Entity {}
    public static class EntityBox<T> {}
    public interface Marker {}
    public interface Other<K> {}
    public interface Direct extends BaseDao<Entity> {}
    public interface CommonDao<K, E> extends BaseDao<E> {}
    public interface CommonEntityDao extends CommonDao<Long, Entity> {}
    public interface Middle<A, B> extends CommonDao<B, A> {}
    public interface Bound extends Middle<Entity, String> {}
    public interface Leaf extends Bound {}
    public interface Multiple extends Marker, Other<Integer>, Leaf {}
    public interface Diamond extends Direct, Multiple {}
    public interface GenericEntity extends BaseDao<EntityBox<Entity>> {}
    public interface Unbound<E> extends BaseDao<E> {}
    public interface Raw extends BaseDao {}

    @Test
    public void resolvesDirectEntity() {
        assertSame(Entity.class, BaseDaoImpl.createProxy(Direct.class, null).getBeanClass());
    }

    @Test
    public void resolvesSecondParameterRatherThanKey() {
        assertSame(Entity.class, BaseDaoImpl.createProxy(CommonEntityDao.class, null).getBeanClass());
    }

    @Test
    public void carriesReorderedBindingsAcrossMultipleLevels() {
        assertSame(Entity.class, BaseDaoImpl.createProxy(Leaf.class, null).getBeanClass());
    }

    @Test
    public void skipsUnrelatedInterfacesAndHandlesDiamond() {
        assertSame(Entity.class, BaseDaoImpl.createProxy(Multiple.class, null).getBeanClass());
        assertSame(Entity.class, BaseDaoImpl.createProxy(Diamond.class, null).getBeanClass());
    }

    @Test
    public void preservesParameterizedEntityRawClassSupport() {
        assertSame(EntityBox.class, BaseDaoImpl.createProxy(GenericEntity.class, null).getBeanClass());
    }

    @Test
    public void rejectsUnresolvedAndRawEntities() {
        assertThrows(IllegalArgumentException.class, () -> BaseDaoImpl.createProxy(Unbound.class, null));
        assertThrows(IllegalArgumentException.class, () -> BaseDaoImpl.createProxy(Raw.class, null));
    }

    @Test
    public void explicitEntityOverloadStillWorks() {
        assertSame(Entity.class, BaseDaoImpl.createProxy(Direct.class, Entity.class, null).getBeanClass());
    }
}
