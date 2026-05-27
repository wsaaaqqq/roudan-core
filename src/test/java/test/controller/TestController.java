package test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.xht.xdb.Xdb;
import org.xht.xdb.vo.Row;
import test.orm.po.PoJPA;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {
    @Resource
    @Lazy
    private TestController self;

    @GetMapping("/test/delete")
    @Transactional
    public long delete() {
        List<Row> rows = query();
        Xdb.table("EXT_HN_T_JY_ZD").delete().rows(rows).execute();
        return count();
    }

    @GetMapping("/test/query")
    public List<Row> query() {
        return Xdb.sql("select * from EXT_HN_T_JY_ZD where id like '-%'").executeQuery()
                .resultRow();
    }

    private long count() {
        return Xdb.sql("select count(1) from EXT_HN_T_JY_ZD where id like '-%'").executeCount();
    }

    @GetMapping("/test/error/{error}")
//    @Transactional(transactionManager = "transactionManager")
    @Transactional
    public List<Row> test(@PathVariable boolean error) {
        List<PoJPA> beans = initData(10);
        Xdb.table("EXT_HN_T_JY_ZD")
                .save()
//                .rowBean(new Rhzd().setId("-1").setCode("-1"))
                .rowsBean(beans)
                .execute();
        Row zd = Xdb.table("EXT_HN_T_JY_ZD")
                .info()
                .id("-1").execute();
        System.out.println(zd);
        if (error) {
            @SuppressWarnings({"divzero", "NumericOverflow"})
            int i = 1 / 0;
        }
        return query();
    }

    @SuppressWarnings("SameParameterValue")
    private static List<PoJPA> initData(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> new PoJPA().setId("-" + i).setCode(String.valueOf(i)))
                .collect(Collectors.toList());
    }


}
