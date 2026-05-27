package test.orm;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.EntityKeysServiceImp;
import org.xht.xdb.vo.WheresBean;
import test.Dbs;
import test.orm.po.PoKeys;
import uk.org.lidalia.sysoutslf4j.context.LogLevel;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.util.List;

@Slf4j
public class EntityKeysServiceTest {
    private final String db = Dbs.h2().getDatasourceName();
    private final EntityKeysServiceImp<PoKeys> dao = new EntityKeysServiceImp<>(PoKeys.class, db);

    @BeforeEach
    public void before() {
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J(LogLevel.TRACE, LogLevel.ERROR);
        XdbConfig.setOrmType(OrmType.XDB);
        XdbConfig.setShowSql(true);
        XdbConfig.setShowSqlArgs(false);
        //        XdbConfig.setShowSql(false);
    }


    @Test
    public void saveOrUpdate() {
        PoKeys key = new PoKeys();
        key.setType("type_test");
        key.setCode("code_test");
        key.setId("id_test");
        dao.saveOrUpdate(key);
    }

    // 添加一个新的测试用例来验证重复保存的问题
    @Test
    public void testSaveOrUpdateDuplicate() {
        // 创建测试数据
        PoKeys key1 = new PoKeys();
        key1.setType("type_test");
        key1.setCode("code_test");
        key1.setId("id_test");
        key1.setName("modified_name");

        // 第一次保存
        dao.saveOrUpdate(key1);
        log.info(dao.getByKeys(key1).toString());

        // 修改一些非主键字段
        key1.setName("modified_name2");
        key1.setId("id_test2");
        key1.setIdx(100);

        // 再次保存相同的主键数据（应该执行更新而不是插入）
        dao.saveOrUpdate(key1);
        log.info(dao.getByKeys(key1).toString());
    }

    @Test
    public void testSaveOrUpdate() {
        // 创建测试数据
        PoKeys key1 = new PoKeys();
        key1.setType("type_test");
        key1.setCode("code_test");
        key1.setId("id_test");
        key1.setName("modified_name");

        // 第一次保存
        dao.saveOrUpdate(key1, true);
        log.info(dao.getByKeys(key1).toString());

        // 修改一些非主键字段
        key1.setName(null);
        key1.setId("id_test2");
        key1.setIdx(100);

        // 再次保存相同的主键数据（应该执行更新而不是插入）
        dao.saveOrUpdate(key1, false);
        log.info(dao.getByKeys(key1).toString());
    }

    @Test
    public void testSaveOrUpdate1() {
        List<PoKeys> outs = outs();
        outs.forEach(po -> po.setName(po.getName() + "44"));
        outs.forEach(po -> po.setIdx(null));
        dao.saveOrUpdate(outs, true);
        outs();
    }

    private PoKeys out() {
        List<PoKeys> list = dao.list(WheresBean.init(PoKeys.class)
                                               .eq(PoKeys::getCode, "code_test")
                                               .eq(PoKeys::getType, "type_test"));
        log.info("out ---------------------------------------");
        list.forEach(po -> log.info(po.toString()));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    private List<PoKeys> outs() {
        List<PoKeys> list = dao.list(WheresBean.init(PoKeys.class).contain(PoKeys::getType, "type_"));
        log.info("outs ---------------------------------------");
        list.forEach(po -> log.info(po.toString()));
        return list;
    }

    @Test
    public void saveOrUpdateThenReturn() {
        List<PoKeys> outs = outs();
        outs.forEach(po -> po.setName("11"));
        outs.forEach(po -> po.setIdx(null));
        dao.saveOrUpdateThenReturn(outs, 2, true);
        outs();
    }

    @Test
    public void testSaveOrUpdateThenReturn() {
        List<PoKeys> outs = outs();
        outs.forEach(po -> po.setName("11"));
        outs.forEach(po -> po.setIdx(null));
        dao.saveOrUpdateThenReturn(outs, 2, false);
        outs();
    }

    @Test
    public void testSaveOrUpdate2() {
    }

    @Test
    public void save() {
        saveOrUpdate();
        PoKeys out = out();
        dao.delete(out);
        out();
        dao.save(out);
        out();
    }

    @Test
    public void testSave() {
    }

    @Test
    public void update() {
    }

    @Test
    public void testUpdate() {
    }

    @Test
    public void testUpdate1() {
    }

    @Test
    public void testUpdate2() {
    }

    @Test
    public void testUpdate3() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void testDelete() {
    }

    @Test
    public void deleteByKeys() {
    }

    @Test
    public void testDeleteByKeys() {
    }

    @Test
    public void exist() {
    }

    @Test
    public void notExist() {
    }

    @Test
    public void getByKeys() {
    }

    @Test
    public void testGetByKeys() {
    }

    @Test
    public void getByKeysOpt() {
    }

    @Test
    public void testGetByKeys1() {
    }

    @Test
    public void infos() {
    }

    @Test
    public void testInfos() {
    }

    @Test
    public void keys() {
    }

    @Test
    public void keysSet() {
    }

    @Test
    public void listAll() {
    }


    @Test
    public void testList() {
    }

    @Test
    public void sql() {
    }

    @Test
    public void sqlPage() {
    }

    @Test
    public void testList1() {
    }

    @Test
    public void testList2() {
    }

    @Test
    public void page() {
    }

    @Test
    public void testPage() {
    }

    @Test
    public void testPage1() {
    }

    @Test
    public void testPage2() {
    }

    @Test
    public void count() {
    }

    @Test
    public void testCount() {
    }

    @Test
    public void testCount1() {
    }
}
