package org.xht.xdb.orm.dao;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.EntityServiceImp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用Dao实现类，支持命名约定方法
 *
 * @param <T> 实体类型
 */
@SuppressWarnings("unchecked")
@Slf4j
public class BaseDaoImpl<T> extends EntityServiceImp<T> implements BaseDao<T> {

    public BaseDaoImpl(Class<T> entityClass, String datasource) {
        super(entityClass, datasource);
    }

    /**
     * 创建支持命名约定方法的Dao代理实例
     */
    @SuppressWarnings("unchecked")
    public static <T, S extends BaseDao<T>> S createProxy(Class<S> daoInterface, Class<T> entityClass,
            String datasource
    ) {
        BaseDaoImpl<T> target = new BaseDaoImpl<>(entityClass, datasource);
        InvocationHandler handler = new DynamicMethodInvocationHandler<>(target, new DynamicSqlBuilder(entityClass));
        Object o =
                Proxy.newProxyInstance(entityClass.getClassLoader(), new Class[]{daoInterface, BaseDao.class}, handler);
        return (S) o;
    }

    public static <T, S extends BaseDao<T>> S createProxy(Class<S> daoInterface, String datasource) {
        Class<T> entityClass = getEntityClass(daoInterface);
        BaseDaoImpl<T> target = new BaseDaoImpl<>(entityClass, datasource);
        InvocationHandler handler = new DynamicMethodInvocationHandler<>(target, new DynamicSqlBuilder(entityClass));
        Object o =
                Proxy.newProxyInstance(entityClass.getClassLoader(), new Class[]{daoInterface, BaseDao.class}, handler);
        return (S) o;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getEntityClass(Class<?> daoInterface) {
        Class<?> entityClass = resolveEntityClass(daoInterface, new HashMap<>());
        if (entityClass != null) {
            return (Class<T>) entityClass;
        }
        throw new IllegalArgumentException("无法从DAO接口中解析出实体类型T");
    }

    private static Class<?> resolveEntityClass(Type type, Map<TypeVariable<?>, Type> inheritedBindings) {
        // 每条继承分支维护自己的绑定，最终只读取 BaseDao<T> 的 T。
        Map<TypeVariable<?>, Type> bindings = new HashMap<>(inheritedBindings);
        Class<?> rawClass;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            rawClass = (Class<?>) parameterizedType.getRawType();
            TypeVariable<?>[] variables = rawClass.getTypeParameters();
            Type[] arguments = parameterizedType.getActualTypeArguments();
            for (int i = 0; i < variables.length; i++) {
                bindings.put(variables[i], resolveType(arguments[i], inheritedBindings));
            }
        } else if (type instanceof Class) {
            rawClass = (Class<?>) type;
        } else {
            return null;
        }

        if (rawClass == BaseDao.class) {
            Type entityType = resolveType(rawClass.getTypeParameters()[0], bindings);
            if (entityType instanceof ParameterizedType) {
                entityType = ((ParameterizedType) entityType).getRawType();
            }
            return entityType instanceof Class ? (Class<?>) entityType : null;
        }
        for (Type parent : rawClass.getGenericInterfaces()) {
            Class<?> result = resolveEntityClass(parent, bindings);
            if (result != null) {
                return result;
            }
        }
        Type superclass = rawClass.getGenericSuperclass();
        return superclass == null ? null : resolveEntityClass(superclass, bindings);
    }

    private static Type resolveType(Type type, Map<TypeVariable<?>, Type> bindings) {
        while (type instanceof TypeVariable) {
            Type resolved = bindings.get(type);
            if (resolved == null || resolved.equals(type)) {
                break;
            }
            type = resolved;
        }
        return type;
    }

}
