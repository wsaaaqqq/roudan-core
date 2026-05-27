package org.xht.xdb.util;


import org.xht.xdb.enums.DbType;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Locale;
import java.util.regex.Pattern;

public class DbTypeUtil {

    private DbTypeUtil() {
    }

    public static DbType getDbType(DataSource dataSource) {
        String jdbcUrl = getJdbcUrl(dataSource);

        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            return parseDbType(jdbcUrl);
        }

        throw new IllegalStateException("Can not get dataSource jdbcUrl: " + dataSource.getClass().getName());
    }

    public static String getJdbcUrl(DataSource dataSource) {
        String[] methodNames = new String[]{"getUrl", "getJdbcUrl"};
        for (String methodName : methodNames) {
            try {
                Method method = dataSource.getClass().getMethod(methodName);
                return (String) method.invoke(dataSource);
            } catch (Exception e) {
                //ignore
            }
        }
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (Exception e) {
            throw new RuntimeException("获取jdbc url失败");
        }
    }


    public static DbType parseDbType(String jdbcUrl) {
        jdbcUrl = jdbcUrl.toLowerCase(Locale.ROOT);
        if (jdbcUrl.contains(":mysql:") || jdbcUrl.contains(":cobar:")) {
            return DbType.MYSQL;
        } else if (jdbcUrl.contains(":mariadb:")) {
            return DbType.MARIADB;
        } else if (jdbcUrl.contains(":oracle:")) {
            return DbType.ORACLE;
        } else if (jdbcUrl.contains(":sqlserver2012:")) {
            return DbType.SQLSERVER;
        } else if (jdbcUrl.contains(":sqlserver:") || jdbcUrl.contains(":microsoft:")) {
            return DbType.SQLSERVER_2005;
        } else if (jdbcUrl.contains(":postgresql:")) {
            return DbType.POSTGRE_SQL;
        } else if (jdbcUrl.contains(":hsqldb:")) {
            return DbType.HSQL;
        } else if (jdbcUrl.contains(":db2:")) {
            return DbType.DB2;
        } else if (jdbcUrl.contains(":sqlite:")) {
            return DbType.SQLITE;
        } else if (jdbcUrl.contains(":h2:")) {
            return DbType.H2;
        } else if (isMatchedRegex(":dm\\d*:", jdbcUrl)) {
            return DbType.DAMENG;
        } else if (jdbcUrl.contains(":xugu:")) {
            return DbType.XUGU;
        } else if (isMatchedRegex(":kingbase\\d*:", jdbcUrl)) {
            return DbType.KINGBASE;
        } else if (jdbcUrl.contains(":phoenix:")) {
            return DbType.PHOENIX;
        } else if (jdbcUrl.contains(":zenith:")) {
            return DbType.GAUSS;
        } else if (jdbcUrl.contains(":gbase:")) {
            return DbType.GBASE;
        } else if (jdbcUrl.contains(":gbasedbt-sqli:") || jdbcUrl.contains(":informix-sqli:")) {
            return DbType.GBASE_8S;
        } else if (jdbcUrl.contains(":ch:") || jdbcUrl.contains(":clickhouse:")) {
            return DbType.CLICK_HOUSE;
        } else if (jdbcUrl.contains(":oscar:")) {
            return DbType.OSCAR;
        } else if (jdbcUrl.contains(":sybase:")) {
            return DbType.SYBASE;
        } else if (jdbcUrl.contains(":oceanbase:")) {
            return DbType.OCEAN_BASE;
        } else if (jdbcUrl.contains(":highgo:")) {
            return DbType.HIGH_GO;
        } else if (jdbcUrl.contains(":cubrid:")) {
            return DbType.CUBRID;
        } else if (jdbcUrl.contains(":goldilocks:")) {
            return DbType.GOLDILOCKS;
        } else if (jdbcUrl.contains(":csiidb:")) {
            return DbType.CSIIDB;
        } else if (jdbcUrl.contains(":sap:")) {
            return DbType.SAP_HANA;
        } else if (jdbcUrl.contains(":impala:")) {
            return DbType.IMPALA;
        } else if (jdbcUrl.contains(":vertica:")) {
            return DbType.VERTICA;
        } else if (jdbcUrl.contains(":xcloud:")) {
            return DbType.XCloud;
        } else if (jdbcUrl.contains(":firebirdsql:")) {
            return DbType.FIREBIRD;
        } else if (jdbcUrl.contains(":redshift:")) {
            return DbType.REDSHIFT;
        } else if (jdbcUrl.contains(":opengauss:")) {
            return DbType.OPENGAUSS;
        } else if (jdbcUrl.contains(":taos:") || jdbcUrl.contains(":taos-rs:")) {
            return DbType.TDENGINE;
        } else if (jdbcUrl.contains(":informix")) {
            return DbType.INFORMIX;
        } else if (jdbcUrl.contains(":sinodb")) {
            return DbType.SINODB;
        } else if (jdbcUrl.contains(":uxdb:")) {
            return DbType.UXDB;
        } else if (jdbcUrl.contains(":greenplum:")) {
            return DbType.GREENPLUM;
        } else if (jdbcUrl.contains(":lealone:")) {
            return DbType.LEALONE;
        } else if (jdbcUrl.contains(":hive2:")) {
            return DbType.HIVE;
        } else {
            return DbType.OTHER;
        }
    }

    public static boolean isMatchedRegex(String regex, String jdbcUrl) {
        if (null == jdbcUrl) {
            return false;
        }
        return Pattern.compile(regex).matcher(jdbcUrl).find();
    }

}
