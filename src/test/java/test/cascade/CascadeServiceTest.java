package test.cascade;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.xht.rd.RD;
import org.xht.rd.RDConfig;
import org.xht.xdb.enums.OrmType;
import org.xht.xdb.orm.cascade.CascadeQueryService;
import org.xht.xdb.orm.cascade.CascadeService;
import org.xht.xdb.orm.cascade.CascadeServiceImpl;
import org.xht.xdb.orm.dao.BaseDao;
import org.xht.xdb.vo.WheresBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CascadeServiceTest {
    CascadeService cascadeService = new CascadeServiceImpl();
    BaseDao<Dept> deptDao = RD.dao().baseDao(Dept.class);
    BaseDao<User> userDao = RD.dao().baseDao(User.class);
    BaseDao<Role> roleDao = RD.dao().baseDao(Role.class);
    BaseDao<TreeNode> treeDao = RD.dao().baseDao(TreeNode.class);
    CascadeQueryService cascadeQueryService = new CascadeQueryService().register(
            Dept.class, d -> {
                if (d.getUsers() == null) {
                    List<User> list = userDao.list(WheresBean.init(User.class).eq(User::getDeptId, d.getId()));
                    d.setUsers(list);
                }
                if (d.getChildren() == null) {
                    List<Dept> list = deptDao.list(WheresBean.init(Dept.class).eq(Dept::getPid, d.getId()));
                    d.setChildren(list);
                }
            }
    ).register(
            User.class, u -> {
                if (u.getRoles() == null) {
                    List<Role> list = roleDao.list(WheresBean.init(Role.class).eq(Role::getUserId, u.getId()));
                    u.setRoles(list);
                }
            }
    ).register(
            TreeNode.class, t -> {
                if (t.getChildNodes() == null) {
                    List<TreeNode> list = treeDao.list(WheresBean.init(TreeNode.class).eq(TreeNode::getPid, t.getId()));
                    t.setChildNodes(list);
                }
            }
    );

    @Test
    void beanSaveOrders() {
        // 测试指定保存顺序
        Dept dept = new Dept();
        dept.setId("dept_order_test");
        dept.setName("部门顺序测试");

        User user = new User();
        user.setId("user_order_test");
        user.setName("用户顺序测试");
        user.setDeptId(dept.getId());

        Role role = new Role();
        role.setId("role_order_test");
        role.setName("角色顺序测试");
        role.setUserId(user.getId());

        user.setRoles(Collections.singletonList(role));
        dept.setUsers(Collections.singletonList(user));

        // 指定先保存 Dept，再保存 User，最后保存 Role
        cascadeService.beanSaveOrders(Dept.class, User.class, Role.class).save(dept);

        // 验证数据已保存
        List<Dept> depts = deptDao.listAll();
        List<User> users = userDao.listAll();
        List<Role> roles = roleDao.listAll();

        assert depts.stream().anyMatch(d -> "dept_order_test".equals(d.getId()));
        assert users.stream().anyMatch(u -> "user_order_test".equals(u.getId()));
        assert roles.stream().anyMatch(r -> "role_order_test".equals(r.getId()));
    }

    @Test
    void query() {
        //        Dept dept = dept(1);
        //        dept.setChildren(depts(11, 15));
        //        dept.setUsers(users(1, 5));
        //        Dept dept2 = dept(2);
        //        dept2.setChildren(depts(21, 25));
        //        List<Dept> list = Arrays.asList(dept, dept2);
        List<Dept> list = JSONUtil.toList(json(), Dept.class);
        cascadeService.saveAll(list, 2);
        print();
        System.out.println("--------------------------------------------------");
        list = deptDao.list(w -> w.isNull(Dept::getPid));
        List<Dept> query = cascadeQueryService.query(list);
        //        System.out.println(JSONUtil.toJsonPrettyStr(query));
        cascadeService.updateAll(query, 100, false);
        cascadeService.deleteAll(query, 100);
        print();
    }

    @Test
    void tree() {
        String json = FileUtil.readUtf8String("C:\\ws\\java\\roudan-core\\src\\test\\java\\test\\cascade\\tree.json");
        List<TreeNode> list = JSONUtil.toList(json, TreeNode.class);
        cascadeService.saveAll(list, 2);
        treeDao.listAll().forEach(System.out::println);
        System.out.println("--------------------------------------------------");
        list = treeDao.list(w -> w.isNull(TreeNode::getPid));
        System.out.println(JSONUtil.toJsonPrettyStr(cascadeQueryService.query(list, TreeNode.class)));
    }

    private String json() {
        return "[\n" +
                "    {\n" +
                "        \"id\": \"dept1\",\n" +
                "        \"children\": [\n" +
                "            {   \"id\": \"dept1_1\",   \"name\": \"deptName_11\",\n" +
                "                \"children\": [\n" +
                "                    {\"id\": \"dept1_1_1\",   \"name\": \"deptName_1_1_1\"},\n" +
                "                    {\"id\": \"dept1_1_2\",   \"name\": \"deptName_1_1_2\"}\n" +
                "                ]   \n" +
                "            },\n" +
                "            {   \"id\": \"dept12\",   \"name\": \"deptName12\"}\n" +
                "        ],\n" +
                "        \"users\": [\n" +
                "            {   \"id\": \"user1\",   \"name\": \"userName1\" ,\"roles\":[{ \"id\": \"role1\", " +
                "\"name\": \"roleName1\"},{ \"id\": \"role2\", \"name\": \"roleName2\"}]},\n" +
                "            {   \"id\": \"user2\",   \"name\": \"userName2\" ,\"roles\":[{ \"id\": \"role1\", " +
                "\"name\": \"roleName1\"},{ \"id\": \"role3\", \"name\": \"roleName3\"}]}\n" +
                "        ],\n" +
                "        \"name\": \"deptName1\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"dept2\",\n" +
                "        \"children\": [\n" +
                "            {   \"id\": \"dept2_1\",   \"name\": \"deptName2_1\"},\n" +
                "            {   \"id\": \"dept2_2\",   \"name\": \"deptName2_2\"}\n" +
                "        ],\n" +
                "        \"name\": \"deptName2\"\n" +
                "    }\n" +
                "]";
    }

    @Test
    void save() {
        // 测试单个实体保存
        Role role = new Role();
        role.setId("role_single");
        role.setName("单个角色");
        role.setUserId("user_single");

        cascadeService.save(role);

        List<Role> roles = roleDao.listAll();
        assert roles.stream().anyMatch(r -> "role_single".equals(r.getId()));
    }

    private void print() {
        roleDao.listAll().forEach(System.out::println);
        System.out.println("--------------------------------------------------");
        userDao.listAll().forEach(System.out::println);
        System.out.println("--------------------------------------------------");
        deptDao.listAll().forEach(System.out::println);
    }

    @Test
    void saveAll() {
        // 测试批量保存
        List<Dept> depts = IntStream.range(100, 110).mapToObj(i -> {
            Dept dept = new Dept();
            dept.setId("dept_batch_" + i);
            dept.setName("部门批量_" + i);
            return dept;
        }).collect(Collectors.toList());

        cascadeService.saveAll(depts, 5);

        List<Dept> allDepts = deptDao.listAll();
        assert allDepts.size() >= 10;
        assert allDepts.stream().anyMatch(d -> d.getId().startsWith("dept_batch_"));
    }

    @Test
    void update() {
        // 测试单个实体更新
        Dept dept = new Dept();
        dept.setId("dept_update");
        dept.setName("原名称");

        // 先保存
        cascadeService.save(dept);

        // 再更新
        dept.setName("更新后的名称");
        cascadeService.update(dept, false);

        Dept updated = deptDao.getById("dept_update");
        assert "更新后的名称".equals(updated.getName());
    }

    @Test
    void updateAll() {
        // 测试批量更新
        List<User> users = IntStream.range(200, 210).mapToObj(i -> {
            User user = new User();
            user.setId("user_update_" + i);
            user.setName("原名_" + i);
            return user;
        }).collect(Collectors.toList());

        // 先保存
        cascadeService.saveAll(users, 5);

        // 批量更新
        users.forEach(u -> u.setName("更新名_" + u.getId().substring("user_update_".length())));
        cascadeService.updateAll(users, 5, false);

        List<User> allUsers = userDao.listAll();
        assert allUsers.stream()
                       .filter(u -> u.getId().startsWith("user_update_"))
                       .allMatch(u -> u.getName().startsWith("更新名_"));
    }

    @Test
    void delete() {
        // 测试单个实体删除
        Role role = new Role();
        role.setId("role_delete");
        role.setName("待删除角色");
        role.setUserId("user_delete");

        // 先保存
        cascadeService.save(role);

        // 验证已保存
        assert roleDao.getById("role_delete") != null;

        // 再删除
        cascadeService.delete(role);

        // 验证已删除
        assert roleDao.getById("role_delete") == null;
    }

    @Test
    void deleteAll() {
        // 测试批量删除
        List<Dept> depts = IntStream.range(300, 310).mapToObj(i -> {
            Dept dept = new Dept();
            dept.setId("dept_delete_" + i);
            dept.setName("待删除部门_" + i);
            return dept;
        }).collect(Collectors.toList());

        // 先保存
        cascadeService.saveAll(depts, 5);

        // 验证已保存
        long countBefore = deptDao.listAll().stream().filter(d -> d.getId().startsWith("dept_delete_")).count();
        assert countBefore == 10;

        // 批量删除
        cascadeService.deleteAll(depts, 5);

        // 验证已删除
        long countAfter = deptDao.listAll().stream().filter(d -> d.getId().startsWith("dept_delete_")).count();
        assert countAfter == 0;
    }

    @Test
    void saveWithCascade() {
        // 测试级联保存（带子对象）
        Role role1 = new Role();
        role1.setId("role_cascade_1");
        role1.setName("角色 1");

        Role role2 = new Role();
        role2.setId("role_cascade_2");
        role2.setName("角色 2");

        User user = new User();
        user.setId("user_cascade");
        user.setName("级联用户");
        user.setRoles(Arrays.asList(role1, role2));

        Dept dept = new Dept();
        dept.setId("dept_cascade");
        dept.setName("级联部门");
        dept.setUsers(Collections.singletonList(user));

        cascadeService.save(dept);

        // 验证所有数据都已保存
        assert deptDao.getById("dept_cascade") != null;
        assert userDao.getById("user_cascade") != null;
        assert roleDao.getById("role_cascade_1") != null;
        assert roleDao.getById("role_cascade_2") != null;
    }

    @Test
    void updateIgnoreNulls() {
        // 测试更新时忽略 null 值
        User user = new User();
        user.setId("user_update_null");
        user.setName("原名");
        user.setDeptId("dept_original");

        cascadeService.save(user);

        // 更新时只设置名称，不设置 deptId
        User updateUser = new User();
        updateUser.setId("user_update_null");
        updateUser.setName("新名称");
        // deptId 为 null

        // ignoreNulls=true，不会更新 null 字段
        cascadeService.update(updateUser, true);

        User result = userDao.getById("user_update_null");
        assert "新名称".equals(result.getName());
        // deptId 应该保持原值
        assert "dept_original".equals(result.getDeptId());
    }

    @Test
    void updateNotIgnoreNulls() {
        // 测试更新时不忽略 null 值
        User user = new User();
        user.setId("user_update_not_null");
        user.setName("原名");
        user.setDeptId("dept_original");

        cascadeService.save(user);

        // 更新时只设置名称，不设置 deptId
        User updateUser = new User();
        updateUser.setId("user_update_not_null");
        updateUser.setName("新名称");
        // deptId 为 null

        // ignoreNulls=false，会将 null 字段更新到数据库
        cascadeService.update(updateUser, false);

        User result = userDao.getById("user_update_not_null");
        assert "新名称".equals(result.getName());
        // deptId 应该被更新为 null
        assert result.getDeptId() == null;
    }

    @Test
    void queryWithCascade() {
        // 测试级联查询
        List<Dept> list = JSONUtil.toList(json(), Dept.class);
        cascadeService.saveAll(list, 2);

        // 查询根部门并级联加载所有子对象
        List<Dept> rootDepts = deptDao.list(w -> w.isNull(Dept::getPid));
        List<Dept> result = cascadeQueryService.query(rootDepts);

        // 验证级联数据已加载
        assert !result.isEmpty();
        for (Dept dept : result) {
            // 验证子部门已加载
            List<Dept> children = dept.getChildren();
            assert children != null && !children.isEmpty();
            // 验证用户已加载
            if (dept.getUsers() != null) {
                for (User user : dept.getUsers()) {
                    // 验证用户的角色已加载
                    List<Role> roles = user.getRoles();
                    assert roles != null && !roles.isEmpty();
                }
            }
        }
    }

    @Test
    void treeStructureSaveAndQuery() {
        // 测试树形结构的保存和查询
        TreeNode root = new TreeNode();
        root.setId("tree_root");
        root.setText("根节点");

        TreeNode child1 = new TreeNode();
        child1.setId("tree_child_1");
        child1.setText("子节点 1");

        TreeNode child2 = new TreeNode();
        child2.setId("tree_child_2");
        child2.setText("子节点 2");

        TreeNode grandChild = new TreeNode();
        grandChild.setId("tree_grandchild");
        grandChild.setText("孙节点");

        child1.setChildNodes(Collections.singletonList(grandChild));
        root.setChildNodes(Arrays.asList(child1, child2));

        // 保存整棵树
        cascadeService.save(root);

        // 验证所有节点都已保存
        assert treeDao.getById("tree_root") != null;
        assert treeDao.getById("tree_child_1") != null;
        assert treeDao.getById("tree_child_2") != null;
        assert treeDao.getById("tree_grandchild") != null;

        // 查询并级联加载
        List<TreeNode> roots = treeDao.list(w -> w.isNull(TreeNode::getPid));
        List<TreeNode> result = cascadeQueryService.query(roots, TreeNode.class);

        assert !result.isEmpty();
        assert result.get(0).getChildNodes() != null;
        assert result.get(0).getChildNodes().size() == 2;
    }

    @Test
    void batchSaveWithDifferentBatchSizes() {
        // 测试不同批量大小的保存
        int[] batchSizes = {1, 5, 10, 50};

        for (int batchSize : batchSizes) {
            // 清理之前的数据
            deptDao.listAll().forEach(d -> deptDao.delete(d));

            List<Dept> depts = IntStream.range(0, 25).mapToObj(i -> {
                Dept dept = new Dept();
                dept.setId("dept_batch_size_" + batchSize + "_" + i);
                dept.setName("批量大小" + batchSize + "_" + i);
                return dept;
            }).collect(Collectors.toList());

            cascadeService.saveAll(depts, batchSize);

            long count = deptDao.listAll()
                                .stream()
                                .filter(d -> d.getId().startsWith("dept_batch_size_" + batchSize))
                                .count();
            assert count == 25 : "Batch size " + batchSize + " failed, expected 25 but got " + count;
        }
    }

    @Test
    void circularReferenceHandling() {
        // 测试循环引用处理
        Dept parent = new Dept();
        parent.setId("dept_parent");
        parent.setName("父部门");

        Dept child = new Dept();
        child.setId("dept_child");
        child.setName("子部门");
        child.setPid(parent.getId());

        // 创建循环引用
        parent.setChildren(Collections.singletonList(child));
        child.setChildren(Collections.singletonList(parent));

        // 应该不会栈溢出
        cascadeService.save(parent);

        // 验证数据已保存
        assert deptDao.getById("dept_parent") != null;
        assert deptDao.getById("dept_child") != null;
    }


    @SuppressWarnings("unused")
    private Dept dept(int i) {
        Dept dept = new Dept();
        dept.setId("dept" + i);
        dept.setName("name" + i);
        return dept;
    }

    @SuppressWarnings("unused")
    private List<Dept> depts(int start, int end) {
        return IntStream.range(start, end).mapToObj(i -> {
            Dept dept = new Dept();
            dept.setId("deptId" + i);
            dept.setName("deptName_" + i);
            return dept;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private List<User> users(int start, int end) {
        return IntStream.range(start, end).mapToObj(i -> {
            User user = new User();
            user.setId("user" + i);
            user.setName("userName" + i);
            return user;
        }).collect(Collectors.toList());
    }

    @BeforeEach
    public void createTable() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        RD.dataSourceConfig(c -> c.addDataSource(dataSource));
        RDConfig.setShowSql(false);
        RDConfig.setOrmType(OrmType.JPA);
        try {
            RD.modify().sql("DROP TABLE T_DEPT").execute();
        } catch (Exception ignored) {
        }
        RD.modify()
          .sql("CREATE TABLE T_DEPT (\n" +
                       "    ID VARCHAR(32) NOT NULL,\n" +
                       "    PID VARCHAR(32) DEFAULT NULL,\n" +
                       "    NAME VARCHAR(255) DEFAULT NULL,\n" +
                       "    CODE VARCHAR(255) DEFAULT NULL,\n" +
                       "    TYPE VARCHAR(255) DEFAULT NULL,\n" +
                       "    IDX INT DEFAULT NULL ,\n" +
                       "    PRIMARY KEY (ID)\n" +
                       ") ")
          .execute();


        try {
            RD.modify().sql("DROP TABLE T_USER").execute();
        } catch (Exception ignored) {
        }
        RD.modify()
          .sql("CREATE TABLE T_USER (\n" +
                       "    ID VARCHAR(32) NOT NULL,\n" +
                       "    NAME VARCHAR(255) DEFAULT NULL,\n" +
                       "    CODE VARCHAR(255) DEFAULT NULL,\n" +
                       "    IDX INT DEFAULT NULL ,\n" +
                       "    DEPT_ID VARCHAR(32) DEFAULT NULL,\n" +
                       "    PRIMARY KEY (ID)\n" +
                       ") ")
          .execute();

        try {
            RD.modify().sql("DROP TABLE T_ROLE").execute();
        } catch (Exception ignored) {
        }
        RD.modify()
          .sql("CREATE TABLE T_ROLE (\n" +
                       "    ID VARCHAR(32) NOT NULL,\n" +
                       "    NAME VARCHAR(255) DEFAULT NULL,\n" +
                       "    CODE VARCHAR(255) DEFAULT NULL,\n" +
                       "    IDX INT DEFAULT NULL ,\n" +
                       "    USER_ID VARCHAR(32) DEFAULT NULL,\n" +
                       "    PRIMARY KEY (ID)\n" +
                       ") ")
          .execute();

        try {
            RD.modify().sql("DROP TABLE T_TREE").execute();
        } catch (Exception ignored) {
        }
        RD.modify()
          .sql("CREATE TABLE T_TREE (\n" +
                       "    ID VARCHAR(55) NOT NULL,\n" +
                       "    TEXT VARCHAR(255) DEFAULT NULL,\n" +
                       "    PID VARCHAR(55) DEFAULT NULL,\n" +
                       "    PRIMARY KEY (ID)\n" +
                       ") ")
          .execute();


        RDConfig.setShowSql(true);
    }

}
