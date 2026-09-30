---
name: roudan-dao
description: Use for roudan DAO-mode entity CRUD - single-table create/read/update/delete, Wheres/WheresBean condition building, dynamic DAO proxy by method naming, cascade queries (CascadeQueryService/@Cascade), and batch operations. Trigger when using BaseDao, EntityService, EntityServiceImp, RR.dao().baseDao, RR.dao().of, Wheres.init, WheresBean.init, saveOrUpdate, asList, CascadeQueryService, or find_by_* dynamic methods with the roudan library.
---

# Roudan DAO 模式

> 通过 `BaseDao<T>` / `EntityService<T>` 进行面向实体的单表 CRUD 操作。
> 支持标准 CRUD、Wheres 条件构造、动态方法代理、批量操作。

## 获取 BaseDao

```java
// 默认数据源
BaseDao<User> dao = RR.dao().baseDao(User.class);

// 指定数据源
BaseDao<User> dao = RR.dao().baseDao(User.class, "oracle");

// 继承方式（Spring @Repository）
@Repository
public class UserDao extends EntityServiceImp<User> {
    public UserDao() { super(User.class, "oracle"); }
}

// 动态代理方式（见文末“动态 DAO”章节）
UserDao dao = RR.dao().of(UserDao.class);
UserDao dao2 = RR.dao().of(UserDao.class, "oracle");  // 指定数据源
```

> `RR.dao()` 与 `RD.dao()` API 镜像：`baseDao(Class)` / `baseDao(Class, datasource)` 获取泛型 DAO；
> `of(DaoInterface)` / `of(DaoInterface, datasource)` 获取动态代理 DAO。

## 标准 CRUD

### 增

```java
dao.save(user);                        // 单条
dao.save(userList, 100);               // 批量，batchSize=100
dao.saveOrUpdate(user);                // 智能合并
dao.saveOrUpdate(userList, 200, true); // 批量合并，忽略null
```

### 删

```java
dao.delete(user);                      // 按实体
dao.delete(userList, 100);             // 按实体批量
dao.deleteById("id1");                 // 按ID
dao.deleteById(idList, 100);           // 批量按ID
```

### 改

```java
dao.update(user);                      // 按主键更新
dao.update(user, true);                // 忽略null字段
dao.update(userList, 200);             // 批量更新
dao.update(userList, 200, true);       // 批量更新（忽略null）
dao.update(user, true, "customId");    // 用指定ID更新（实体主键为空时）
```

### 查

```java
User u = dao.getById("1");             // 按ID查
Optional<User> opt = dao.getByIdOpt("1");
List<User> all = dao.listAll();        // 全量
List<User> list = dao.list(wheres);    // 条件查询（见Wheres章节）
List<User> list2 = dao.list(w -> w.eq(User::getType, "A"));  // Lambda 条件（w 是 WheresBean<User>）
List<User> list3 = dao.list("where type = :type", MapUtil.init().add("type", "A")); // 原始SQL条件
List<User> ids = dao.getByIds(idList); // 批量按ID
List<User> ids2 = dao.getByIds(idList, 500); // 批量按ID（分批大小）
Map<String, User> map = dao.infos(idList); // Map<ID, Entity>
boolean exist = dao.existId("1");      // 存在性
boolean exist2 = dao.exist(user);      // 按实体存在性
boolean exist3 = dao.exist(wheres);    // 按条件存在性
boolean notExist = dao.notExistId("1");// 不存在性
long count = dao.count();              // 总数
long count = dao.count(wheres);        // 条件统计
long count2 = dao.count("type = :t", MapUtil.init().add("t", "A")); // SQL统计
List<String> ids = dao.ids(String.class); // 所有ID
List<String> ids2 = dao.ids(String.class, wheres); // 条件ID列表

// 类型投影：查实体、返回 VO（只 select 需要的列时配合 wheres.select 用）
List<UserVO> vos = dao.asList(wheres, UserVO.class);
List<UserVO> allVos = dao.asListAll(UserVO.class);

// DAO 内直接写 SQL / 分页 SQL
SqlTool tool = dao.sql("select * from T_USER where type = :t");
SqlPageTool pageTool = dao.sqlPage();

// 运行时切换数据源（修改当前线程的数据源选择，影响后续调用，用完切回）
dao.datasource("oracle").listAll();
```

## Wheres 条件构造器

### 字符串风格

```java
import org.xht.xdb.vo.Wheres;

Wheres wheres = Wheres.init()
    .eq("type", "ADMIN")               // =
    .ne("status", "DELETED")           // <>
    .gt("age", 18)                     // >
    .ge("score", 60)                   // >=
    .lt("age", 60)                     // <
    .le("score", 100)                  // <=
    .contain("name", "张")             // LIKE '%张%'
    .startWith("name", "张")           // LIKE '张%'
    .endWith("name", "伟")             // LIKE '%伟'
    .in("dept", Arrays.asList("D1"))   // IN
    .notIn("type", excludeList)        // NOT IN
    .between("age", 18, 60, true)      // BETWEEN
    .isNull("remark")                  // IS NULL
    .notNull("email")                  // IS NOT NULL
    .inJoinString("dept", "D1,D2")     // 逗号串拆 IN（另有 inJoinString(col, str, split) 自定义分隔符）
    .anyColContain("关键词", "name", "remark")  // 多列任一 LIKE
    .orderByDesc("age", true)          // 排序（orderByAsc 同理；第二参 nullsLast）
    .pageIndex(1).pageSize(20);        // 分页
```

子条件组与逻辑切换：

```java
Wheres wheres = Wheres.init()
    .eq("status", "ACTIVE")
    .sub(sub -> sub.or().eq("type", "A").eq("type", "B"));
// → WHERE status=:x AND (type=:y OR type=:z)

Wheres w2 = Wheres.init()
    .eq("a", 1).or().eq("b", 2);       // and()/or() 切换连接符，默认 and
// → WHERE a=:x OR b=:y
```

条件执行（guard 三形态——无条件/布尔/Supplier，所有条件方法都有）：

```java
.contain("name", keyword)                    // 始终生效
.contain("name", keyword, hasKeyword)        // 布尔为真才生效
.contain("name", keyword, () -> keyword != null)  // Supplier 延迟求值
.between("age", 18, 60, true, c1, c2)        // between 的起止可各设 guard
```

### Lambda 类型安全风格

```java
import org.xht.xdb.vo.WheresBean;

WheresBean<User> w = WheresBean.init(User.class)
    .eq(User::getType, "ADMIN")       // ne/gt/ge/lt/le 同理
    .gt(User::getAge, 18)
    .contain(User::getName, "张")     // startWith/endWith 同理
    .in(User::getDept, deptList)      // notIn/inJoinString 同理
    .isNull(User::getRemark)          // notNull/between 同理
    .anyColContain("关键词", User::getName, User::getRemark)  // 多字段模糊搜索
    .pageIndex(1).pageSize(20);
```

列投影 + 排序 + 子组（Lambda 风格全量能力）：

```java
WheresBean<User> w = WheresBean.init(User.class)
    .select(User::getId).select(User::getName)  // 只查指定列（配合 asList 转 VO）
    .eq(User::getType, "ADMIN")
    .sub(s -> s.or().eq(User::getType, "A").isNull(User::getType))
    .orderByDesc(User::getAge, true)            // 第二参 nullsLast；orderByAsc 同理
    .and().ge(User::getAge, 18);                // and()/or() 切换连接符
```

> Lambda 风格每个条件方法都有 `(getter, value)` / `(getter, value, boolean)` /
> `(getter, value, Supplier<Boolean>)` 三形态；`orderByAsc/Desc` 必须传 `(getter, nullsLast)` 双参。

### 使用 Wheres

```java
List<User> list = dao.list(wheres);
PageResult<User> page = dao.page(wheres);
long count = dao.count(wheres);
```

## 分页

```java
PageResult<User> page = dao.page(
    Wheres.init().eq("type", "A").pageIndex(1).pageSize(20)
);
List<User> rows = page.getItems();
long total = page.getCount();
```

## 动态 DAO（命名约定）

定义接口，无需实现——框架通过 JDK 代理自动生成 SQL。

```java
public interface UserDao extends BaseDao<User> {

    List<User> find_by_name(String name);                // WHERE name=:name
    List<User> find_by_name_and_type(String n, String t); // WHERE name=:n AND type=:t
    List<User> find_by_name_or_type(String n, String t);  // WHERE name=:n OR type=:t

    Long count_by_name(String name);
    boolean exists_by_name(String name);
    void delete_by_name(String name);

    // 操作符后缀
    List<User> find_by_name_like(String name);           // LIKE :name
    List<User> find_by_name_contain(String name);        // LIKE '%name%'
    List<User> find_by_name_start_with(String name);     // LIKE 'name%'
    List<User> find_by_name_end_with(String name);       // LIKE '%name'
    List<User> find_by_idx_gt(Integer i);                // >
    List<User> find_by_idx_lt(Integer i);                // <
    List<User> find_by_idx_gte(Integer i);               // >=
    List<User> find_by_idx_lte(Integer i);               // <=
    List<User> find_by_name_not(String name);            // <>
    List<User> find_by_name_in(List<String> names);      // IN
    List<User> find_by_idx_between(List<Integer> range); // BETWEEN

    List<User> find_by_idx_gt_and_name_start_with(Integer idx, String name);
}

// 使用
UserDao userDao = RR.dao().of(UserDao.class);
UserDao userDao2 = RR.dao().of(UserDao.class, "oracle");  // 指定数据源
List<User> users = userDao.find_by_name_like("张%");
```

> `default` 方法不被代理（走接口默认实现）；方法可返回 `Optional<T>`；
> 支持 `count_/exists_/delete_` 前缀与 `_and/_or` 组合，见上例。

## 批量操作

```java
dao.save(list, 100);                    // 批量插入
dao.update(list, 200, true);            // 批量更新（忽略null）
dao.saveOrUpdate(list, 200, true);      // 批量智能合并

// 合并并返回结果
SaveOrUpdateBatchResult<User> r = dao.saveOrUpdateThenReturn(list, 500, true);
r.getSaveList();   // 新增的
r.getUpdateList(); // 更新的

// 单条合并忽略 null；集合合并非批量版
dao.saveOrUpdate(user, true);
dao.saveOrUpdate(userList, true);
```

## 级联查询（CascadeQueryService）

按需加载关联对象：`@Cascade` 标记子字段，`register` 注册加载器，`query` 触发。

```java
import org.xht.xdb.orm.cascade.Cascade;
import org.xht.xdb.orm.cascade.CascadeQueryService;

public class Dept {
    @Id private String id;
    @Cascade @Transient private List<Dept> children;  // 子部门
    @Cascade @Transient private List<User> users;     // 部门用户
    // setter 里回填外键：setChildren 时 child.setPid(this.id)
}

// 注册加载器（每个类型注册一次，可复用单例）
CascadeQueryService cascade = new CascadeQueryService()
    .register(Dept.class, d -> {
        if (d.getUsers() == null)
            d.setUsers(userDao.list(WheresBean.init(User.class).eq(User::getDeptId, d.getId())));
        if (d.getChildren() == null)
            d.setChildren(deptDao.list(WheresBean.init(Dept.class).eq(Dept::getPid, d.getId())));
    });

// 查询后触发级联加载（支持 List/Set/单对象，循环引用安全）
List<Dept> roots = deptDao.list(w -> w.isNull(Dept::getPid));
List<Dept> result = cascade.query(roots);
```

`@Cascade` 属性：`enabled`（总开关）/`datasource`（跨库加载）/`cascadeSave/cascadeUpdate/cascadeDelete`（写侧开关）。

## 多数据源

```java
// 创建时指定
BaseDao<User> dao = RR.dao().baseDao(User.class, "oracle");

// 运行时切换（EntityService 只有 datasource(name)；切回默认用 RR/RD 门面）
dao.datasource("oracle").listAll();

// 门面级切换（影响后续所有调用，记得切回）
RR.datasource("oracle");
RR.datasourceDefault();
```
