package org.xht.rd;

import lombok.extern.slf4j.Slf4j;
import org.xht.rr.impl.*;
import org.xht.xdb.Xdb;
import org.xht.xdb.sql.XdbSql;

import java.sql.Connection;
import java.util.function.Consumer;

/**
 * Xdb是jdbc工具类，封装了常用的jdbc操作。
 * <pre>
 * Example：
 *  BasicDataSource dataSource = new BasicDataSource();
 *  dataSource.setUrl("jdbc:oracle:thin:@192.168.9.66:1521:orcl");
 *  dataSource.setUsername("xxx");
 *  dataSource.setPassword("xxx");
 *  dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
 *
 *  Xdb.init()
 *     .addDataSourceDefault(dataSource,DbType.ORACLE)
 *  Xdb.sql("insert into TEST_A (id,name,p_id) values( '2' , '1' ,'1')")
 *     .executeUpdate()
 *  Xdb.sqlFile(Test.class, "files/sql/device.sql")
 *     .sqlArgs(MapUtil.init().add("name",Arrays.asList("2",3l)))
 *     .executeQuery()
 *
 *  Connection conn = Xdb.getConnection();
 *  conn.setSavepoint("save1");
 *  Xdb.setConnection(conn);
 *  Savepoint savePoint = conn.setSavepoint();
 *  try{
 *     Xdb.sql("insert table1").executeUpdate();
 *     Xdb.sql("update table2").executeUpdate();
 *     conn.commit();
 *  }catch (Exception e){
 *     conn.rollback(savePoint);
 *  }
 *
 *
 * </pre>
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@Slf4j
public class RD {
    private static final RD self = new RD();

    public static RD dataSourceConfig(Consumer<RRDataSourceConfig> dataSourceConfig) {
        dataSourceConfig.accept(RRDataSourceConfig.INSTANCE);
        return self;
    }

    public static XdbSql datasourceDefault() {
        return Xdb.selectDataSourceDefault();
    }


    public static XdbSql datasource(String dataSourceName) {
        return Xdb.selectDataSourceByName(dataSourceName);
    }

    public static Connection getConnection() {
        return Xdb.getConnection();
    }

    public static RD setConnection(Connection connection) {
        Xdb.setConnection(connection);
        return RD.self;
    }

    public static RRSqlNamedQuery namedQuery() {
        return new RRSqlNamedQuery();
    }

    public static RRSqlQuery query() {
        return new RRSqlQuery();
    }

    public static RRSqlNamedUpdate namedModify() {
        return new RRSqlNamedUpdate();
    }

    public static RRSqlUpdate modify() {
        return new RRSqlUpdate();
    }

    public static RRDao dao(){
        return RRDao.INSTANCE;
    }
}
