package org.xht.xdb.orm.cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cascade {
    boolean enabled() default true;

    String datasource() default "";

    boolean cascadeSave() default true;

    boolean cascadeUpdate() default true;

    boolean cascadeDelete() default true;
}
