package org.xht.xdb.orm.dao;

import org.xht.xdb.Xdb;
import org.xht.xdb.orm.dao.vo.MethodParseResult;
import org.xht.xdb.sql.SqlTool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Optional;

public class DynamicMethodInvocationHandler<T> implements InvocationHandler {
    private final BaseDaoImpl<T> target;
    private final DynamicSqlBuilder dynamicSqlBuilder;

    public DynamicMethodInvocationHandler(BaseDaoImpl<T> target, DynamicSqlBuilder dynamicSqlBuilder) {
        this.target = target;
        this.dynamicSqlBuilder = dynamicSqlBuilder;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // 首先检查是否为 Object 类的方法（如 toString, hashCode, equals）
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        // 检查方法是否有具体实现，如果有则直接调用实现
        if (hasConcreteImplementation(method)) {
            // 对于接口默认方法，需要特殊处理
            int modifiers = method.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(modifiers) &&
                    !java.lang.reflect.Modifier.isAbstract(modifiers) &&
                    method.getDeclaringClass().isInterface()) {
                // 这是接口的默认方法，使用MethodHandles来调用
                return invokeDefaultMethod(proxy, method, args);
            } else {
                // 非默认方法，继续在target上调用
                return method.invoke(target, args);
            }
        }

        // 然后检查是否为命名约定方法
        if (MethodNamingParser.isSupportedMethod(methodName)) {
            return handleNamedQueryMethod(methodName, args, method.getReturnType());
        }

        // 否则调用原始方法
        // 直接使用普通反射调用，避免MethodHandles的问题
        return method.invoke(target, args);
    }

    /**
     * 检查方法是否有具体实现（包括接口默认方法）
     */
    private boolean hasConcreteImplementation(Method method) {
        // 如果不是接口方法，则有具体实现
        if (!method.getDeclaringClass().isInterface()) {
            return true;
        }

        // 检查在接口中是否有具体实现
        // 在接口中，如果方法不是抽象的（即有默认实现），则hasConcreteImplementation为true
        // 但在反射层面，我们通过尝试查找该方法是否在接口中有实现来判断
        try {
            // 获取方法所在的接口类
            int modifiers = method.getModifiers();
            // public 且 不是 abstract 的接口方法是默认方法
            return java.lang.reflect.Modifier.isPublic(modifiers) &&
                    !java.lang.reflect.Modifier.isAbstract(modifiers);
        } catch (Exception e) {
            // 忽略异常，继续使用命名约定
        }

        return false;
    }

    /**
     * 处理命名约定方法
     */
    private Object handleNamedQueryMethod(String methodName, Object[] args, Class<?> returnType) {
        Class<?> beanClass = target.getBeanClass();
        MethodParseResult parseResult = MethodNamingParser.parseMethodName(methodName);
        if (parseResult == null) {
            throw new IllegalArgumentException("无法解析方法名: " + methodName);
        }
        SqlTool sqlTool = dynamicSqlBuilder.build(parseResult, args);

        // 根据返回类型执行不同的操作
        String operation = parseResult.getOperation().toLowerCase();

        Object data;
        switch (operation) {
            case "read":
            case "get":
                data = Xdb.sqlTool(sqlTool).executeQuery().firstBean(beanClass);
                break;
            case "find":
                data = Xdb.sqlTool(sqlTool).executeQuery().resultBean(beanClass);
                break;
            case "count":
                long count = Xdb.sqlTool(sqlTool).executeCount();
                if (returnType.isAssignableFrom(Integer.class) || returnType.isAssignableFrom(int.class)) {
                    data = (int) count;
                } else if (returnType.isAssignableFrom(Short.class) || returnType.isAssignableFrom(short.class)) {
                    data = (short) count;
                } else if (returnType.isAssignableFrom(BigInteger.class)) {
                    data = BigInteger.valueOf(count);
                } else {
                    data = count;
                }
                break;
            case "exists":
                data = Xdb.sqlTool(sqlTool).executeCount() > 0;
                break;
            case "delete":
                data = Xdb.sqlTool(sqlTool).executeUpdate();
                break;
            default:
                // 默认执行查询
                data = Xdb.sqlTool(sqlTool).executeQuery().resultBean(beanClass);
                break;
        }
        // returnType 是 optional
        if (returnType.isAssignableFrom(Optional.class)) {
            return Optional.ofNullable(data);
        }
        return data;
    }

    /**
     * 调用接口的默认方法
     */
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        // 在Java 8中，调用接口默认方法需要特殊处理
        // 由于直接使用MethodHandles调用接口默认方法存在权限限制
        // 我们需要采用另一种方式来处理

        // 一种可行的方案是使用一个辅助类来帮助调用默认方法
        // 创建一个DefaultMethodInvoker来处理接口默认方法的调用
        return DefaultMethodInvoker.invokeDefaultMethod(proxy, method, args);
    }
}
