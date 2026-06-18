# roudan：自由选择实体、灵活操作数据、快速写入数据库的 Java 框架

> 给你的项目一个机会：不想绑死在 JPA 或 MyBatis 上？试试 roudan。

---

## 一、你的项目被 ORM 绑住了吗？

做 Java 后端这些年，你一定碰到过这些场景：

- Spring Data JPA 写简单 CRUD 爽得飞起，但一到复杂动态查询就开始怀疑人生
- MyBatis-Plus 灵活，但 XML 越写越多，Mapper 实现类一堆，维护越来越头痛
- 团队里有人用 JPA 写实体，有人用 MyBatis 写 SQL，两套风格割裂，新人被劝退
- 碰上达梦、金仓这种国产数据库，JPA 的方言适配让你怀疑是不是在给框架打工

本质问题是：**你被选定的 ORM 框架绑住了**。一旦选了，整个项目的数据库操作方式就被锁死，换框架的成本高到没人敢提。

那能不能有一条路——既保留 JPA 实体操作的便利，又有 MyBatis 原生 SQL 的自由，还不依赖任何框架可以独立运行？

---

## 二、roudan 是什么

**roudan 是一个不依赖 JPA、MyBatis、Spring 也能独立运行的 Java 数据库操作框架。**

它围绕三个核心设计：

| 核心 | 含义 |
|------|------|
| **自由** | JPA / MyBatis 实体无缝拿出来用，也可以完全不用它们独立运行 |
| **灵活** | 实体 CRUD、动态 DAO、原生 SQL、Table API……多种姿势随时切换 |
| **快速** | 批量操作极快，智能增量同步一个方法搞定 |

简单说：**你想怎么写，roudan 就让你怎么写**。

---

## 三、自由：JPA、MyBatis 实体无缝迁移，也能完全独立

### 3.1 你现在的 JPA 实体，一行不改直接用

这是你跑了多年的 JPA 实体：

```java
@Entity
@Table(name = "T_USER")
public class User {
    @Id
    @Column(name = "ID")
    private String id;
    private String name;
    private Integer age;
}
```

引入 roudan 之后——**注解不动，直接接管**：

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);

// 保存
dao.save(new User().setId("1").setName("秋道丁次"));

// 条件查询 — 两种方式都支持
// 类型安全（getter 引用，编译期检查，推荐）
List<User> users = dao.list(
    WheresBean.init(User.class).contain(User::getName, "丁次").pageIndex(1).pageSize(10)
);
// 字段名字符串（灵活性更高）
List<User> users2 = dao.list(
    Wheres.init().contain("name", "丁次").pageIndex(1).pageSize(10)
);

// 分页查询 — 同样两种写法
// 类型安全
PageResult<User> page = dao.page(
    WheresBean.init(User.class).eq(User::getName, "秋道丁次").pageIndex(1).pageSize(20)
);
// 字段名
PageResult<User> page2 = dao.page(
    Wheres.init().eq("name", "秋道丁次").pageIndex(1).pageSize(20)
);
```

你不需要改一行实体代码，不需要加任何实现类，不需要写任何 XML。

### 3.2 MyBatis-Flex 的实体同样识别

如果你的项目用的是 MyBatis-Flex 的注解：

```java
XdbConfig.setOrmType(OrmType.MYBATIS_FLEX);  
```

roudan 自动读懂这些注解，实体类不用动。

### 3.3 也可以不用任何 ORM，roudan 自身就能独立运行

如果你开新项目，连 JPA 都不想引入：

```java
@org.xht.xdb.orm.anno.Table("T_USER")
public class User {
    @org.xht.xdb.orm.anno.Id
    @org.xht.xdb.orm.anno.Column("ID")
    private String id;
    private String name;
}

// pom.xml 里只有 roudan-core + 数据库驱动，没有 JPA，没有 MyBatis
Xdb.init().addDataSourceDefault(hikariCP);
BaseDao<User> dao = RD.dao().baseDao(User.class); // 一切照旧
```

**零外部 ORM 依赖，roudan 自己就是完整的数据库操作框架。**

---

## 四、灵活：五种姿势操作数据库，你爱怎么用就怎么用

真正的灵活不是"也能支持"，而是**同一个 Dao，你想用什么方式就切什么方式，不需要换工具**。

### 4.1 实体 CRUD

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);

dao.save(user);
dao.save(userList, 500);
dao.update(user, true);           // 忽略 null 字段
dao.delete(user);
dao.deleteById("id1");
User u = dao.getById("id1");
List<User> all = dao.listAll();
// 统计 — 类型安全 + 字段名都支持
long c = dao.count(WheresBean.init(User.class).eq(User::getType, "ADMIN"));
long c2 = dao.count(Wheres.init().eq("type", "ADMIN"));
```

### 4.2 动态 DAO：定义接口即实现，方法名就是 SQL

写一个接口，连实现类都省了：

```java
public interface UserDao extends BaseDao<User> {
    List<User> find_by_name_contain(String name);
    List<User> find_by_age_gt_and_status(Integer age, String status);
    Long count_by_dept(String dept);
    void delete_by_name(String name);
}

UserDao dao = RD.dao().of(UserDao.class);
List<User> users = dao.find_by_name_contain("张");
```

rouda 在运行时用 JDK 动态代理自动解析方法名，生成 SQL、绑定参数、执行、映射结果。支持的操作符：

| 后缀 | SQL | 示例 |
|------|-----|------|
| `_like` | LIKE | `find_by_name_like` |
| `_contain` | LIKE '%x%' | `find_by_name_contain` |
| `_start_with` | LIKE 'x%' | `find_by_name_start_with` |
| `_end_with` | LIKE '%x' | `find_by_name_end_with` |
| `_gt` / `_lt` | > / < | `find_by_age_gt` |
| `_gte` / `_lte` | >= / <= | `find_by_age_gte` |
| `_not` / `_ne` | <> | `find_by_name_not` |
| `_in` | IN (...) | `find_by_id_in` |
| `_between` | BETWEEN | `find_by_age_between` |

用 `_and_` 和 `_or_` 自由组合：`find_by_dept_and_age_gt`、`find_by_name_or_code`。

### 4.3 原生 SQL：随时切回手写

```java
// 查询
List<User> users = Xdb.sql("select * from T_USER where type = :type")
    .sqlArg("type", "ADMIN")
    .executeQuery()
    .resultBean(User.class);

// 增删改
Xdb.sql("update T_USER set name = :name where id = :id")
    .sqlArg("name", "李四").sqlArg("id", "1")
    .executeUpdate();

// count
long count = Xdb.sql("select count(1) from T_USER where age > :age")
    .sqlArg("age", 18).executeCount();
```

同一个项目里，`dao.save()` 和 `Xdb.sql(...)` 混用，不用引入两套依赖。

### 4.4 Table API：单表免 SQL

```java
Xdb.table("T_USER").save().rowBean(user).execute();
Xdb.table("T_USER").delete().row(Row.init().set("id", "1")).execute();
Row info = Xdb.table("T_USER").info().id("1").execute();
```

### 4.5 两种查询构造器，按场景选用

roudan 提供两套查询构造器，同一个 Dao 上完全互操作，你爱用哪个用哪个：

**WheresBean — 类型安全（getter 引用，编译期检查）：**

```java
dao.page(
    WheresBean.init(User.class)
        .eq(User::getName, "张三")           // 字段名写错编译直接报错
        .gt(User::getAge, 18)
        .contain(User::getRemark, "VIP")
        .between(User::getCreateTime, start, end, true)
        .in(User::getDept, deptList)
        .anyColContain("关键词", User::getName, User::getRemark)  // 多字段模糊搜索
        .isNull(User::getEmail)
        .notNull(User::getMobile)
        .pageIndex(1).pageSize(20)
);
```

**Wheres — 字段名字符串（运行时灵活，动态场景更自由）：**

```java
dao.page(
    Wheres.init()
        .eq("name", "张三")                  // 字段名可以运行时拼接
        .gt("age", 18)
        .contain("remark", "VIP")
        .between("create_time", start, end, true)
        .in("dept", deptList)
        .anyColContain("关键词", "name", "remark")
        .isNull("email")
        .notNull("mobile")
        .sub(sub -> sub.or().eq("type", "A").eq("type", "B"))  // 条件分组
        .pageIndex(1).pageSize(20)
);
```

**两者共享的能力：**
- `eq / ne / gt / lt / ge / le` 全比较运算符
- `contain / startWith / endWith` 模糊匹配
- `in / notIn` 集合匹配
- `between` 范围
- `isNull / notNull` 空值判断
- `sub()` 条件分组 + `and()` / `or()` 逻辑切换
- `Supplier<Boolean>` guard 条件（后面细说）
- `pageIndex / pageSize` 分页

### 4.6 智能条件 SQL 文件

SQL 集中管理在文件里，用 `--:` 前缀实现条件化：

```sql
-- sql/user_query.sql
select * from t_user where 1=1
--: and name like '%' || :name || '%'
--: and dept = :dept
--: and age > :age
```

```java
// 只传 name，dept 和 age 没传 → 只生成 name 条件
Xdb.sqlFile("sql/user_query.sql")
    .sqlArg("name", "张")
    .executeQuery()
    .resultBean(User.class);
```

`--:` 开头的行，如果该行所有参数都传了值，自动取消注释生成条件；没传就跳过。**一条 SQL 文件适配所有查询场景**，不用写动态拼接逻辑，不用 MyBatis `<if>` 标签。

### 4.7 `select()` 按需查字段，不浪费带宽

```java
List<User> items = dao.list(
    WheresBean.init(User.class)
        .eq(User::getDept, "技术部")
        .select(User::getId)     // 只查 id
        .select(User::getName)   // 只查 name
);
// 生成 SQL: select ID, NAME from T_USER where DEPT = ?
```

### 4.8 复合主键自动识别

实体上有多个 `@Id` 字段？roudan 自动处理，查询和删除的 WHERE 条件自动拼好：

```java
@Table("T_ORDER_ITEM")
public class OrderItem {
    @Id private String orderId;
    @Id private String itemCode;
    private Integer qty;
}

EntityKeysService<OrderItem> svc = new EntityKeysServiceImp<>(OrderItem.class, Xdb.DEFAULT_DATASOURCE_NAME);
// 自动生成 WHERE order_id=? AND item_code=?
OrderItem item = svc.getByKeys(new OrderItem().setOrderId("O1").setItemCode("A"));
svc.deleteByKeys(item);
```

### 4.9 多数据源，一行切换

```java
// 初始化
Xdb.init()
    .addDataSourceDefault(mysqlDs, DbType.MYSQL)
    .addDataSource(oracleDs, DbType.ORACLE, "oracle");
```

数据源在 DAO 层使用，三种方式任选：

**方式一：创建 DAO 时指定数据源**

```java
BaseDao<User> dao = RD.dao().baseDao(User.class, "oracle");
dao.listAll(); // 自动连 oracle
```

**方式二：运行时动态切换**

```java
BaseDao<User> dao = RD.dao().baseDao(User.class); //default 数据源
dao.datasource("oracle").listAll();  // 临时切到 oracle
dao.datasource("mysql").listAll();   // 再切到 mysql
```

**方式三：继承 EntityServiceImp，写死数据源**

```java
@Repository
@Slf4j
public class UserDao extends EntityServiceImp<User> {
    public UserDao() {
        super(User.class, "oracle");
    }
}
// 注入即可用，数据源永不漂移
```

ThreadLocal 隔离，多数据源并发互不干扰。

---

## 五、快速：批量操作快得飞起

```java
// 批量写入，自定义 batchSize，自动拆批
dao.saveOrUpdate(userList, 500, true); //true 表示忽略 null 字段
dao.save(userList, 500);
dao.update(userList, 500, true); //true 表示忽略 null 字段
dao.deleteById(idList, 500);

SaveOrUpdateBatchResult<User> result = dao.saveOrUpdateThenReturn(incoming, 500, true);

result.getSaveList();    // 新增了哪些 → 已批量 INSERT
result.getUpdateList();  // 更新了哪些 → 已批量 UPDATE

**ETL 导入、外部系统数据对账、批量同步——以前几百行的对比+批量逻辑，现在一个方法。**

底层是纯 JDBC 的 `PreparedStatement.executeBatch()`，没有 ORM 的 Session 管理、脏检查、N+1 问题、LazyLoading 陷阱。**快，就是直接。**
```
        
---

## 六、一张表看懂差异

| | MyBatis-Plus | JPA/Hibernate | roudan |
|--|:--:|:--:|:--:|
| 实体 CRUD | ✅ | ✅ | ✅ |
| 原生 SQL | ✅ 需 XML | ❌ 仅 JPQL | ✅ |
| 方法命名代理 | ❌ | ✅ SpringData | ✅ 内建 |
| Lambda 类型安全 | ✅ | ❌ | ✅ |
| 无需 XML | ✅ | ✅ | ✅ |
| 无需实现类 | ❌ | ✅ | ✅ |
| 脱离 ORM 独立运行 | ❌ | ❌ | ✅ |
| 接管已有 JPA 实体 | ❌ | — | ✅ |
| 智能增量同步 | ❌ | ❌ | ✅ |
| 动态条件 SQL 文件 | ❌ | ❌ | ✅ |
| 复合主键 | 手动 | ✅ | ✅ |
| 信创/达梦/金仓 | 部分 | 部分 | ✅ |

---

## 七、一分钟上手

**pom.xml：**

```xml
<dependency>
    <groupId>io.github.wsaaaqqq</groupId>
    <artifactId>roudan-core</artifactId>
    <version>0.0.1</version>
</dependency>
```

**实体类：**

```java
@Entity @Table(name = "T_USER")
public class User {
    @Id @Column(name = "ID")
    private String id;
    private String name;
    private Integer age;
}
```

**初始化：**

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:h2:mem:test");
config.setUsername("sa");
config.setPassword("");
HikariDataSource ds = new HikariDataSource(config);

Xdb.init().addDataSourceDefault(ds);
```

**CRUD：**

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);

// 保存
dao.save(new User().setId("1").setName("秋道丁次").setAge(16));

// 查询
User user = dao.getById("1");
List<User> all = dao.listAll();

// 条件分页
PageResult<User> page = dao.page(
    WheresBean.init(User.class).eq(User::getName, "秋道丁次").pageIndex(1).pageSize(10)
);

// 原生 SQL
List<User> users = Xdb.sql("select * from T_USER where age > :age")
    .sqlArg("age", 10).executeQuery().resultBean(User.class);
```

从零到 CRUD 跑通，5 分钟。

---

## 八、彩蛋

命名灵感有三重：

其一是《火影忍者》秋道一族的秘术——**肉弹战车（Nikudan Sensha）**，丁次发动时摧枯拉朽，碾过一切障碍；

其二是作者女儿的小名**「肉肉」**，把女儿的名字写进代码里，大概是程序员独一份的浪漫；

其三是"肉蛋"听着就敦实可靠——数据库操作库嘛，稳比炫更重要。

和 roudan 操作数据库一样——干脆、直接、不拖泥带水。

---

仓库地址：[https://github.com/wsaaaqqq/roudan-core](https://github.com/wsaaaqqq/roudan-core)

Maven 中央仓库已发布，坐标：`io.github.wsaaaqqq:roudan-core:0.0.1`

觉得有用的话，来个 Star ⭐，欢迎 Issues 和 PR。
