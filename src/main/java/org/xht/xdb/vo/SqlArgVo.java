package org.xht.xdb.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlArgVo {
    private String sql;
    private Object[] parameters;
}
