package org.xht.xdb.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.XdbConfig;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
public class CloseUtil {

    @SneakyThrows
    public static void close(Connection closeable) {
        if (closeable == null) return;
        if (XdbConfig.isUseSpringTransaction()) {
            SpringConnectionUtil.colseConnectionBySpring(closeable);
        } else {
            closeable.close();
        }
    }

    @SneakyThrows
    public static void close(Connection closeable, boolean[] autoCloseConnection) {
        if (closeable == null) return;
        if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
            close(closeable);
        }
    }

    @SneakyThrows
    public static void close(Connection closeable, Boolean autoCloseConnection) {
        if (closeable == null) return;
        if (autoCloseConnection) {
            close(closeable);
        }
    }

    @SneakyThrows
    public static void close(ResultSet closeable) {
        if (closeable == null) return;
        closeable.close();
    }

    @SneakyThrows
    public static void close(Statement closeable) {
        if (closeable == null) return;
        closeable.close();
    }

    @SneakyThrows
    public static void close(InputStream closeable) {
        if (closeable == null) return;
        closeable.close();
    }

    @SneakyThrows
    public static void close(BufferedReader closeable) {
        if (closeable == null) return;
        closeable.close();
    }

    public static void closeNotThrow(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    public static void close(ResultSet closeable, boolean... autoCloseConnection) {
        if (closeable == null) return;
        if (autoCloseConnection == null || autoCloseConnection.length == 0 || autoCloseConnection[0]) {
            close(closeable);
        }
    }
}
