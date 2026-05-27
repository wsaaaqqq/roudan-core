package test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.xht.xdb.orm.EntityService;
import org.xht.xdb.util.MapUtil;
import test.orm.po.PoJPA;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrmTestController {

    private final EntityService<PoJPA> entityService = EntityService.of(PoJPA.class, "ds1");

    @GetMapping("/test/orm/count")
    public long count() {
        return entityService.count("where id like '-%' ");
    }

    @GetMapping("/test/orm/count2")
    public long count2() {
        return entityService.count();
    }

    @GetMapping("/test/orm/count3")
    public long count3() {
        return entityService.count("where id like ''|| :like ||'%' ", MapUtil.init().add("like", "-"));
    }

    @GetMapping("/test/orm/delete")
    public void delete() {
        PoJPA poJPA = new PoJPA().setId("-1").setCode("---1");
        entityService.delete(poJPA);
    }

    @GetMapping("/test/orm/delete2")
    public void delete2() {
        List<PoJPA> where = entityService.list("where id like '-%' ");
        entityService.delete(where, 100);
    }

    @GetMapping("/test/orm/deleteById")
    public void deleteById() {
        entityService.deleteById("-1");
    }

    @GetMapping("/test/orm/deleteById2")
    public void deleteById2() {
        List<PoJPA> where = entityService.list("where id like '-%' ");
        List<Object> ids = where.stream().map(PoJPA::getId).collect(Collectors.toList());
        entityService.deleteById(ids);
    }

    @GetMapping("/test/orm/error/{error}")
    @Transactional
    public List<PoJPA> test(@PathVariable boolean error) {
        List<PoJPA> beans = initData(10, "-");
        entityService.save(beans, 100);
        PoJPA zd = entityService.getById("-1");
        System.out.println(zd);
        if (error) {
            //noinspection unused,NumericOverflow,divzero
            int i = 1 / 0;
        }
        return where();
    }

    @GetMapping("/test/orm/saveOrUpdate")
    @Transactional
    public void saveOrUpdate() {
        PoJPA poJPA = new PoJPA().setId("-1").setCode("-1");
        entityService.saveOrUpdate(poJPA);
    }

    @GetMapping("/test/orm/saveOrUpdate2")
    public void saveOrUpdate2() {
        Function<Integer, PoJPA> createPo = (i) -> {
            String _i = i.toString();
            return new PoJPA().setId(_i).setName(_i).setCode(_i).setType(_i).setIdx(i);
        };
        List<PoJPA> collect = IntStream.range(1, 1000).mapToObj(createPo::apply).collect(Collectors.toList());
        entityService.saveOrUpdate(collect, 500, true);
    }

    @GetMapping("/test/orm/save")
    public void save() {
        PoJPA poJPA = new PoJPA().setId("-1").setCode("-1");
        entityService.save(poJPA);
    }

    @GetMapping("/test/orm/save2")
    public void save2() {
        List<PoJPA> list = initData(10, "-");
        entityService.save(list, 100);
    }

    @GetMapping("/test/orm/update")
    public void update() {
        PoJPA poJPA = new PoJPA().setId("-1").setCode("---1");
        entityService.save(poJPA);
    }

    @GetMapping("/test/orm/update2")
    public void update2() {
        List<PoJPA> list = initData(10, "-");
        entityService.save(list, 100);
    }

    @GetMapping("/test/orm/getById")
    @Cacheable("get")
    public PoJPA getById() {
        return entityService.getById("-1");
    }

    @GetMapping("/test/orm/getById2")
    public List<PoJPA> getById2() {
        List<PoJPA> where = entityService.list("where id like '-%' ");
        List<Object> ids = where.stream().map(PoJPA::getId).collect(Collectors.toList());
        return entityService.getById(ids, 100);
    }

    @GetMapping("/test/orm/where")
    public List<PoJPA> where() {
        return entityService.list("where id like '-%' ");
    }

    @GetMapping("/test/orm/where2")
    public List<PoJPA> where2() {
        return entityService.list("where id like ''|| :like ||'%' ", MapUtil.init().add("like", "-"));
    }

    @SuppressWarnings("SameParameterValue")
    private static List<PoJPA> initData(int size, String flag) {
        return IntStream
                .range(0, size)
                .mapToObj(i -> new PoJPA().setId("-" + i).setCode(flag + i))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public EntityService<PoJPA> datasource(String datasource) {
        return null;
    }
}
