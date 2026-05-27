package org.xht.xdb.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class CommitUtil {
    @SneakyThrows
    public static void commit(Connection conn) {
        if (conn == null) return;
        if (XdbConfig.isUseSpringTransaction()) return;
        conn.commit();
    }

    public static void commit(boolean autoCommit, Connection conn) throws SQLException {
        if (autoCommit && !conn.getAutoCommit()) {
            commit(conn);
        }
    }
}
