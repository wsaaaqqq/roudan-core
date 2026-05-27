package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;

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
public class XdbSql {

    /**
     * <pre>
     * 功能：预处理sql
     * 1、sql文件中的参数使用冒号占位符，如" :id "," :name "
     * 2、冒号占位符前后必须留有空格
     * </pre>
     *
     * @param sql sql语句
     * @return SqlTool
     */
    public SqlTool sql(String sql) {
        SqlTool sqlTool = getSqlTool();
        sqlTool.sql(sql);
        return sqlTool;
    }

    /**
     * <pre>
     * 功能：预处理sql
     * 1、sql文件中的参数使用冒号占位符，如" :id "," :name "
     * 2、冒号占位符前后必须留有空格
     * </pre>
     *
     * @return {@link SqlPageTool }
     */
    public SqlPageTool sqlPage() {
        return new SqlPageTool();
    }

    /**
     * <pre>
     * 功能：预处理sql
     * 1、sqlFileClass和sqlFileName需在同一目录层级
     * 2、sql文件中的参数使用冒号占位符，如" :id "," :name "
     * 3、冒号占位符前后必须留有空格
     * 4、注释语句以1行为单位，1行内的所有参数占位符如果都在sqlArgs中传入，则自动放开此行语句
     * </pre>
     *
     * @param sqlFileClass 与sql文件同目录的任意clazz（方便通过clazz获取类加载路径，从而找到sql文件）
     * @param sqlFileName  sql文件名
     * @return SqlTool
     */
    public <T> SqlTool sqlFile(Class<T> sqlFileClass, String sqlFileName) {
        SqlTool sqlTool = getSqlTool();
        sqlTool.sqlFile(sqlFileClass, sqlFileName);
        return sqlTool;
    }

    private SqlTool getSqlTool() {
        return new SqlTool();
    }

    /**
     * <pre>
     * 功能：预处理sql
     * 1、sqlFileClass和sqlFileName需在同一目录层级
     * 2、sql文件中的参数使用冒号占位符，如" :id "," :name "
     * 3、冒号占位符前后必须留有空格
     * 4、注释语句以1行为单位，1行内的所有参数占位符如果都在sqlArgs中传入，则自动放开此行语句
     * </pre>
     *
     * @param sqlFileRelativePath sql文件相对路径,根目录默认位于xdb.sqlDir配置的目录下，未配置时默认为usr.dir对应目录
     * @return SqlTool
     */
    public SqlTool sqlFile(String sqlFileRelativePath) {
        SqlTool sqlTool = getSqlTool();
        sqlTool.sqlFile(sqlFileRelativePath);
        return sqlTool;
    }

    public SqlTable table(String tableName) {
        return new SqlTable(tableName);
    }

}
