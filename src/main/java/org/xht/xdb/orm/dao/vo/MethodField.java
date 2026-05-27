package org.xht.xdb.orm.dao.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 方法字段类
 */
@Getter
public class MethodField {
    private final String fieldName;
    private final String condition;
    @Setter
    private String nextConnector = "AND"; // 默认连接符

    public MethodField(String fieldName, String condition) {
        this.fieldName = fieldName;
        this.condition = condition;
    }

}
