package org.xht.xdb.function;

import java.lang.reflect.Field;

public interface SetFieldValueFunction {
    <T> void apply(T target, Field field, Object value);
}
