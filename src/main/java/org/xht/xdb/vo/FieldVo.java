package org.xht.xdb.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.xht.xdb.function.SetFieldValueFunction;

import java.lang.reflect.Field;

@Data
@NoArgsConstructor
public class FieldVo {
    private Field field;

    public FieldVo(Field field) {
        this.field = field;
    }

    private SetFieldValueFunction setFieldValueFunction;
}
