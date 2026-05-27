package test.sql;

import org.junit.jupiter.api.Test;
import org.xht.xdb.sql.SqlBuild;

public class SqlBuildTest {

    @Test
    public void init() {
        String id = "3";
        String id2 = "2";
        SqlBuild s = SqlBuild.init();
        s.add("select * from EXT_HN_T_JY_ZD where 1=1 ");
        s.addIfNotNull("and id=:id", "id", id);
        s.addIfNotEmpty("and id=:id1", "id1", id);
        s.addIf("or id=:id2", "id2", id2, "and id is null");
        s.addIfNotNull("or id=:id3", null);
        s.addIfNotNull("or id=:id4", "123");
        s.addIfNotEmpty("or id=:id5", "");

    }
}
