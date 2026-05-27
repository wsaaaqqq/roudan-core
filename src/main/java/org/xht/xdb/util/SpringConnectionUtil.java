package org.xht.xdb.util;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.sql.XDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Optional;

@Slf4j
public class SpringConnectionUtil {
    public static Connection getConnectionBySpring(DataSource dataSource) {
        try {
            Class<?> dataSourceUtilsClass = Class.forName("org.springframework.jdbc.datasource.DataSourceUtils");
            java.lang.reflect.Method getConnectionMethod = dataSourceUtilsClass.getMethod("getConnection",
                    DataSource.class);
            return (Connection) getConnectionMethod.invoke(null, dataSource);
        } catch (Exception e) {
            log.error("getConnectionBySpring error: {}", e.getMessage());
        }
        return null;
    }

    public static void colseConnectionBySpring(Connection connection) {
        DataSource dataSource = Optional.ofNullable(Xdb.getXDataSource()).map(XDataSource::getDataSource).orElse(null);
        if (dataSource == null) return;
        try {
            Class<?> dataSourceUtilsClass = Class.forName("org.springframework.jdbc.datasource.DataSourceUtils");
            Method releaseConnectionMethod = dataSourceUtilsClass.getMethod("releaseConnection",
                    Connection.class,
                    DataSource.class);
            releaseConnectionMethod.invoke(null, connection, dataSource);
        } catch (Exception e) {
            log.error("releaseConnectionBySpring error: {}", e.getMessage());
        }
    }
}
