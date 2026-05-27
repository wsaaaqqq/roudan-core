package org.xht.xdb.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@SuppressWarnings("unused")
@Slf4j
public class DebugProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T> T getDebugProxy(Class<T> tClass) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            log.info("DebugProxyFactory ------------------ start ---------------------------");
            Class<?> declaringClass = method.getDeclaringClass();
            log.info("Class: {}", declaringClass.getName());
            log.info("Method: {}", method.getName());
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    log.info("args {} : {}", i, args[i]);
                }
            }
            Constructor<MethodHandles.Lookup> constructor = ReflectUtil.getConstructor(MethodHandles.Lookup.class,
                    Class.class);
            constructor.setAccessible(true);
            Object ret = constructor.newInstance(declaringClass)
                    .in(declaringClass)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
            log.info("Ret: {}", ret);
            log.info("DebugProxyFactory ------------------  end ---------------------------");
            return ret;
        };
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(),
                new Class[]{tClass}
                , invocationHandler);
    }

}
