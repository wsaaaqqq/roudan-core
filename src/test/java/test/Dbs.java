package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.jspecify.annotations.NonNull;
import org.xht.rd.RD;
import org.xht.rd.RDConfig;
import org.xht.xdb.Xdb;
import org.xht.xdb.enums.DbType;

import javax.sql.DataSource;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Dbs {

    private DataSource dataSource;
    private String datasourceName;

    public static Dbs h2() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        Xdb.init().addDataSource(dataSource, DbType.H2, "h2");
        Xdb.selectDataSourceByName("h2");
        createTable();
        return new Dbs(dataSource, "h2");
    }

    private static void createTable() {
        RDConfig.setShowSql(false);
        try {
            RD.modify().sql("DROP TABLE T_TEST").execute();
        } catch (Exception ignored) {
        }
        RD.modify()
                .sql("CREATE TABLE T_TEST (\n" +
                        "    ID VARCHAR(32) NOT NULL,\n" +
                        "    NAME VARCHAR(255) DEFAULT NULL,\n" +
                        "    CODE VARCHAR(255) DEFAULT NULL,\n" +
                        "    TYPE VARCHAR(255) DEFAULT NULL,\n" +
                        "    IDX INT DEFAULT NULL ,\n" +
                        "    PRIMARY KEY (ID)\n" +
                        ") ")
                .execute();
        RDConfig.setShowSql(true);
    }


}
