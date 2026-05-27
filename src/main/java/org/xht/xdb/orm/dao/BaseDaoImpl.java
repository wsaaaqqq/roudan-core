package org.xht.xdb.orm.dao;

import cn.hutool.core.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.EntityServiceImp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

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
        // 获取所有泛型接口
        Type[] genericInterfaces = daoInterface.getGenericInterfaces();

        for (Type type : genericInterfaces) {
            // 检查是否为参数化类型（Java 8 需拆分类型判断与变量声明）
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                // 获取原始类型（即BaseDao.class）
                Type rawType = parameterizedType.getRawType();

                // 确认是BaseDao的实现（Java 8 需拆分类型判断与变量声明）
                if (rawType instanceof Class) {
                    Class<?> rawClass = (Class<?>) rawType;
                    if (BaseDao.class.isAssignableFrom(rawClass)) {
                        // 获取泛型实际参数（即T）
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length > 0) {
                            Type entityType = actualTypeArguments[0];

                            // 处理T本身是泛型的情况（如BaseDao<List<User>>）
                            if (entityType instanceof ParameterizedType) {
                                entityType = ((ParameterizedType) entityType).getRawType();
                            }

                            // 转换为Class对象（Java 8 需拆分类型判断与强制转换）
                            if (entityType instanceof Class) {
                                return (Class<T>) entityType;
                            }
                        }
                    }
                }
            }
        }

        // 若未找到，尝试递归查找父接口（处理多层继承）
        Class<?>[] interfaces = daoInterface.getInterfaces();
        for (Class<?> inter : interfaces) {
            Class<T> result = getEntityClass(inter);
            if (result != null) {
                return result;
            }
        }

        throw new IllegalArgumentException("无法从DAO接口中解析出实体类型T");
    }

}
