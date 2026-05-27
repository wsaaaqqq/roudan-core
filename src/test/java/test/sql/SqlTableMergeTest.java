package test.sql;

import org.junit.jupiter.api.Test;
import org.xht.xdb.Xdb;
import test.Dbs;

public class SqlTableMergeTest {

    @Test
    public void isNull() {
        Dbs.h2();
    }

    @Test
    public void rowsMap() {
        Dbs.h2();
        Xdb
                .datasource("h2")
                .sql("select 1 type,2 \"flBM\", 3 \"bm\",4 bm from T_TEST limit 1,10 ")
                .executeQuery()
//                .toOriginList()
//                .toCamelList()
//                .toLowerCaseList()
//                .toUpperCaseList()
//                .resultRow()
//                .resultRowOrigin()
//                .resultRowCamel()
//                .resultRowLowerCase()
                .resultRowUpperCase()
                .forEach(System.out::println)
        ;

        //        List<Row> rowsNew = Arrays.asList(
        //                Row.init().set("ID", "roleId11"),
        //                Row.init().set("ID", "roleId31").set("NAME", "roleName31"),
        //                Row.init().set("ID", "roleId2").set("NAME", "roleName2")
        //
        //        );
        //        Xdb
        //                .table("AA_ROLE")
        //                .merge()
        //                .dataOldQueryTableAllData()
        //                .dataNew(rowsNew)
        //                .keyColumns("ID")
        //                .allowDelete(false)
        //                .allowUpdate(true)
        //                .allowInsert(true)
        //                .debug()
        //        ;
    }
}
