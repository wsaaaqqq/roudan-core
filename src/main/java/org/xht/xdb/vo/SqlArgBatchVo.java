package org.xht.xdb.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SqlArgBatchVo {
    private String sql;
    private List<Object[]> parameters;

    public void debug() {
        log.info("sql: {}", this.sql);
        for (Object[] parameter : this.parameters) {
            log.info("row: {}", Arrays.toString(parameter));
        }
    }
}
