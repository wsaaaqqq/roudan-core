package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        DataSource dataSource1 = RDTest.getDataSource();
        Xdb.init().addDataSource(dataSource1, DbType.H2, "h2");
        Xdb.selectDataSourceByName("h2");
        RDTest.createTable();
        return new Dbs(dataSource1, "h2");
    }


}
