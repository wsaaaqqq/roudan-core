package test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xht.xdb.orm.EntityService;
import org.xht.xdb.vo.PageResult;
import org.xht.xdb.vo.Wheres;
import test.orm.po.PoJPA;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WheresTestController {

    private final EntityService<PoJPA> entityService = EntityService.of(PoJPA.class, "ds1");

    @GetMapping("/test/wheres/page")
    public PageResult<PoJPA> page() {
        return entityService.page(Wheres.init()
                .startWith("type", "TYPE_")
                .endWith("type", "G")
                .pageIndex(1)
                .pageSize(2)
        );
    }

    @GetMapping("/test/wheres/where")
    public List<PoJPA> where() {
        return entityService.list(Wheres.init()
                .startWith("type", "TYPE_")
                .endWith("type", "G")
                .pageIndex(1)
                .pageSize(2)
        );
    }

}
