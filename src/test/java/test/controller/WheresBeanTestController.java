package test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xht.xdb.orm.EntityService;
import org.xht.xdb.vo.PageResult;
import org.xht.xdb.vo.WheresBean;
import test.orm.po.PoJPA;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WheresBeanTestController {

    private final EntityService<PoJPA> entityService = EntityService.of(PoJPA.class, "ds1");

    @GetMapping("/test/wheres/bean/page")
    public PageResult<PoJPA> page() {
        return entityService.page(WheresBean.init(PoJPA.class)
                .startWith(PoJPA::getType, "TYPE_")
                .endWith(PoJPA::getType, "ZT")
                .ge(PoJPA::getIdx, 2)
                .between(PoJPA::getIdx, 2, 4, false)
                .pageIndex(1)
                .pageSize(100)
        );
    }

    @GetMapping("/test/wheres/bean/where")
    public List<PoJPA> where() {
        return entityService.list(WheresBean.init(PoJPA.class)
                .startWith(PoJPA::getType, "TYPE_")
                .endWith(PoJPA::getType, "G")
                .pageIndex(1)
                .pageSize(2)
        );
    }

}
