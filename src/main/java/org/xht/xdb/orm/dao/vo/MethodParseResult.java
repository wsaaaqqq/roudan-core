package org.xht.xdb.orm.dao.vo;


import lombok.Getter;

import java.util.List;

/**
 * 方法解析结果类
 */
@Getter
public class MethodParseResult {
    private final String operation;
    private final List<MethodField> fields;

    public MethodParseResult(String operation, List<MethodField> fields) {
        this.operation = operation;
        this.fields = fields;
    }
}
