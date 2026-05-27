package test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Dbs {

    private DataSource dataSource;
    private String datasourceName;

    public static Dbs h2() {
        return new Dbs(RDTest.getDataSource(), "h2");
    }


}
