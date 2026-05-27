package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.rr.RR;
import org.xht.xdb.vo.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RRTest {

    @BeforeEach
    public void setUp() {
        //        RRConfig.setIgnorePackagesForDebug(ps -> ps.clear());
        Dbs.h2();
    }


    @Test
    public void testInsert() {
        RR.modify().sql("delete from t_test").execute();
        RR.namedModify()
          .sql("insert into t_test (id, name, idx) values (:id, :name, :idx)")
          .argsBatch(rows -> IntStream.range(0, 10000).mapToObj(i -> new HashMap<String, Object>() {{
              put("id", "id" + i);
              put("name", "name" + i + "@example.com");
              put("idx", i);
          }}).forEach(rows::add))
          .executeBatch(100);
        print();
    }

    private static void print() {
        List<Row> rows = RR.query().sql("select * from t_test").executeQuery().resultRow();
        System.out.println("count: " + rows.size() + ", first: " + rows.get(0));
    }

    @Test
    public void testInsert2() {
        RR.modify().sql("delete from t_test").execute();
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"John Doe", "john.doe@example.com", 30});
        rows.add(new Object[]{"Jane Smith", "jane.smith@example.com", 25});
        rows.add(new Object[]{"Mike Johnson", "mike.johnson@example.com", 35});
        RR.modify().sql("insert into t_test (id, name, idx) values (?, ?, ?)").argsBatch(rows).executeBatch(100);
        print();
    }

    @Test
    public void testInsert3() {
        RR.modify().sql("delete from t_test").execute();
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(new HashMap<String, Object>() {{
            put("id", "John Doe");
            put("name", "john.doe@example.com");
            put("idx", 30);
        }});
        rows.add(new HashMap<String, Object>() {{
            put("id", "Jane Smith");
            put("name", "jane.smith@example.com");
            put("idx", 25);
        }});
        rows.add(new HashMap<String, Object>() {{
            put("id", "Mike Johnson");
            put("name", "mike.johnson@example.com");
            put("idx", 35);
        }});
        RR.namedModify()
          .sql("insert into t_test (id, name, idx) values (:id, :name, :idx)")
          .argsBatch(rows)
          .executeBatch(100);
        print();
    }

    @Test
    public void testInsert4() {
        RR.modify().sql("delete from t_test").execute();
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"John Doe", "john.doe@example.com", 30});
        rows.add(new Object[]{"Jane Smith", "jane.smith@example.com", 25});
        rows.add(new Object[]{"Mike Johnson", "mike.johnson@example.com", 35});
        RR.modify().sql("insert into t_test (id, name, idx) values (?, ?, ?)").argsBatch(rows).executeBatch(100);
        print();
    }

}
