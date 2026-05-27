package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;

@Slf4j
public class ExecuteTimeHelp {

    private static final ThreadLocal<Long> executeStartTime = new ThreadLocal<>();

    public static void executeStart() {
        executeStartTime.set(System.currentTimeMillis());
    }

    public static void debug() {
        String msg;
        if (XdbConfig.isShowSqlExecuteTime()) {
            msg = String.format("xdb execute time: %d ms", System.currentTimeMillis() - executeStartTime.get());
        } else {
            return;
        }
        if (XdbConfig.isShowSqlUseSystemOut()) {
            System.out.println(msg);
        } else {
            log.info("\n{}", msg);
        }
    }
}
