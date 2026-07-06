# roudan (肉蛋)

[![Maven Central](https://img.shields.io/badge/maven--central-0.0.4-blue)](https://central.sonatype.com/artifact/io.github.wsaaaqqq/roudan-core)
[![Java](https://img.shields.io/badge/java-8%2B-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)

**roudan** 是一款高性能 Java 数据库操作库，旨在平衡开发效率与技术灵活性。

它融合了 **JPA 面向实体的极简开发体验** 与 **MyBatis 原生 SQL 的强大控制力**，同时内置高效的批处理能力。

> 命名有三重含义：其一来自《火影忍者》秋道一族的秘术——**肉弹战车**；其二是作者女儿的小名**「肉肉」**；其三是"肉蛋"听着就敦实可靠。和操作数据库一样——干脆、直接。

---

## 特性

- **零依赖 Spring** — 纯 JDBC 封装，可脱离 Spring 独立使用，也完美适配 Spring Boot
- **双模开发** — JPA 式的实体 CRUD + MyBatis 式的自由 SQL，两者可混用
- **动态 DAO 代理** — 基于方法命名约定自动生成 SQL，无需编写实现
- **流畅查询构造器** — `Wheres` / `WheresBean<T>` 链式构建 WHERE 条件，支持类型安全 Lambda
- **多 ORM 注解兼容** — 同时支持 JPA、MyBatis-Flex 及自有注解
- **多数据源** — ThreadLocal 隔离的多数据源管理，运行时动态切换
- **40+ 数据库** — Oracle、MySQL、PostgreSQL、达梦、金仓、H2 等
- **SQL 文件管理** — SQL 集中管理，支持 DB 方言后缀，参数自动条件化
- **分页支持** — 自动生成 count 查询和方言 LIMIT
- **批量操作** — 高效的 batch insert/update/delete/saveOrUpdate
- **连接池限流** — Semaphore 信号量限流，保护数据库
- **可调式日志** — SQL 打印、调用链追踪、执行耗时统计

---

## 快速入门

### 1. 添加依赖

```xml
<dependency>
    <groupId>io.github.wsaaaqqq</groupId>
    <artifactId>roudan-core</artifactId>
    <version>0.0.4</version>
</dependency>
```

> Spring Boot 用户推荐直接引入 [roudan-spring-boot-starter](https://github.com/wsaaaqqq/roudan-spring-boot-starter)，自动完成数据源装配。

### 2. 定义实体类

roudan 兼容多种注解风格，任选一种即可：

**JPA 注解：**

```java
@Data
@Accessors(chain = true)
@Entity
@Table(name = "T_USER")
public class User implements Serializable {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;
    private String code;
    private String type;
    private Integer idx;
}
```

> 也支持 roudan 自有注解及 MyBatis-Flex `@Table` 等，通过 `RDConfig.setOrmType()` 切换。

### 3. 初始化数据源

```java
HikariDataSource ds = new HikariDataSource();
ds.setJdbcUrl("jdbc:h2:mem:test");
ds.setUsername("sa");
ds.setPassword("");

RD.dataSourceConfig(cfg -> cfg.addDataSourceDefault(ds));
```

### 4. 开始使用

```java
// 获取 BaseDao
BaseDao<User> dao = RD.dao().baseDao(User.class);

// 保存
dao.save(new User().setId("1").setName("张三").setIdx(1));

// 查询
User user = dao.getById("1");
List<User> list = dao.listAll();

// 分页
PageResult<User> page = dao.page(
    Wheres.init()
        .startWith("name", "张")
        .pageIndex(1)
        .pageSize(10)
);

// 原生 SQL
List<User> users = RD.namedQuery()
    .sql("select * from T_USER where name like :name")
    .args("name", "%张%")
    .executeQuery()
    .resultBean(User.class);
```

---

## 核心用法

> 所有对外操作统一从 `RD` 入口发起，所有全局配置统一走 `RDConfig`。

### 实体 CRUD (BaseDao)

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);

// 保存
dao.save(user);
dao.save(userList, 100);  // 批量，每批100条

// 更新
dao.update(user);
dao.update(user, true);   // 忽略 null 字段

// 保存或更新（合并在库数据后批量写入）
dao.saveOrUpdate(userList, 200, true);

// 删除
dao.delete(user);
dao.deleteById("id1");
dao.deleteById(idList, 100);

// 查询
User u = dao.getById("id");
Optional<User> opt = dao.getByIdOpt("id");
List<User> all = dao.listAll();
boolean exists = dao.existId("id");

// 统计
long count = dao.count();
long count2 = dao.count(Wheres.init().eq("type", "ADMIN"));
```

### 动态 DAO (方法命名约定)

只需定义接口，无需实现类，框架自动代理生成 SQL：

```java
public interface UserDao extends BaseDao<User> {

    List<User> find_by_name(String name);

    List<User> find_by_name_and_type(String name, String type);

    List<User> find_by_name_or_type(String name, String type);

    Long count_by_name(String name);

    boolean exists_by_name(String name);

    void delete_by_name(String name);

    // 支持操作符后缀
    List<User> find_by_name_like(String name);
    List<User> find_by_name_contain(String name);      // LIKE '%值%'
    List<User> find_by_name_start_with(String name);   // LIKE '值%'
    List<User> find_by_name_end_with(String name);     // LIKE '%值'
    List<User> find_by_idx_gt(Integer order);          // >
    List<User> find_by_idx_lt(Integer order);          // <
    List<User> find_by_idx_gte(Integer order);         // >=
    List<User> find_by_idx_lte(Integer order);         // <=
    List<User> find_by_name_not(String name);           // <> / !=
    List<User> find_by_name_in(List<String> names);    // IN
    List<User> find_by_idx_between(List<Integer> range); // BETWEEN

    // 复杂组合
    List<User> find_by_idx_gt_and_name_start_with(Integer idx, String name);
}
```

使用：

```java
UserDao userDao = RD.dao().of(UserDao.class);
List<User> users = userDao.find_by_name_like("张%");
```

### 原生 SQL

roudan 提供两种参数风格：命名参数（`:name`）走 `RD.namedQuery()` / `RD.namedModify()`，占位符（`?`）走 `RD.query()` / `RD.modify()`。

```java
// 命名参数查询
List<User> users = RD.namedQuery()
    .sql("select * from T_USER where type = :type")
    .args("type", "ADMIN")
    .executeQuery()
    .resultBean(User.class);

// 多参数 + 条件参数（值不满足条件时忽略该参数）
List<User> list = RD.namedQuery()
    .sql("select * from T_USER where 1=1 and type = :type")
    .args("type", "ADMIN", type != null)
    .executeQuery()
    .resultBean(User.class);

// 占位符查询
List<User> admins = RD.query()
    .sql("select * from T_USER where type = ? and idx > ?")
    .args("ADMIN", 5)
    .executeQuery()
    .resultBean(User.class);

// 行结果
List<Row> rows = RD.namedQuery()
    .sql("select * from T_USER where id = :id")
    .args("id", "1")
    .executeQuery()
    .resultRow();

// 增删改
int rows2 = RD.namedModify()
    .sql("update T_USER set name = :name where id = :id")
    .args("name", "李四")
    .args("id", "1")
    .execute();

// count
long count = RD.namedQuery()
    .sql("select count(1) from T_USER")
    .executeCount();
```

### SQL 文件

SQL 可以从 classpath 加载，支持数据库方言后缀（`.sql.oracle`、`.sql.mysql` 等）：

```java
// device.sql 内容：
// select * from device
// where 1=1
// --: and type = :type
// --: and name like :name

List<Device> devices = RD.namedQuery()
    .sqlFile(MyClass.class, "files/sql/device.sql")
    .args(MapUtil.init().add("type", "SENSOR").add("name", "%温度%"))
    .executeQuery()
    .resultBean(Device.class);
```

以 `--:` 开头的行在参数未传入时自动注释，实现条件 SQL。

### 流畅查询构造器

**Wheres（字段名字符串）：**

```java
Wheres wheres = Wheres.init()
    .eq("type", "ADMIN")                         // =
    .ne("status", "DELETED")                     // <>
    .gt("age", 18)                               // >
    .ge("score", 60)                             // >=
    .lt("age", 60)                               // <
    .le("score", 100)                            // <=
    .contain("name", "张")                        // LIKE '%张%'
    .startWith("name", "张")                      // LIKE '张%'
    .endWith("name", "伟")                        // LIKE '%伟'
    .in("dept", Arrays.asList("D1", "D2"))        // IN
    .notIn("type", excludeTypes)                 // NOT IN
    .between("age", 18, 60, true)                // BETWEEN (含边界)
    .isNull("remark")                            // IS NULL
    .notNull("email")                            // IS NOT NULL
    .pageIndex(1)
    .pageSize(20);

List<User> users = dao.list(wheres);
PageResult<User> page = dao.page(wheres);
```

**子条件组 + AND/OR：**

```java
Wheres wheres = Wheres.init()
    .eq("status", "ACTIVE")
    .sub(sub -> sub
        .or()
        .eq("type", "ADMIN")
        .eq("type", "SUPER_ADMIN")
    );
// WHERE status = :x AND (type = :y OR type = :z)
```

**条件执行（guard）：**

```java
String keyword = null;
Wheres wheres = Wheres.init()
    .eq("type", "A")
    .contain("name", keyword, () -> keyword != null);  // keyword为null时跳过
```

**WheresBean — 类型安全（Lambda getter）：**

```java
WheresBean<User> wheres = WheresBean.init(User.class)
    .eq(User::getType, "ADMIN")
    .gt(User::getAge, 18)
    .contain(User::getName, "张")
    .in(User::getDept, deptList)
    .between(User::getCreateTime, start, end, true)
    .anyColContain("关键词", User::getName, User::getRemark) // 多字段模糊搜索
    .pageIndex(1)
    .pageSize(20);

PageResult<User> page = dao.page(wheres);
```

### 分页

```java
PageResult<User> page = dao.page(
    WheresBean.init(User.class)
        .eq(User::getType, "NORMAL")
        .pageIndex(1)
        .pageSize(20)
);

// page.getList()   — 当前页数据
// page.getTotal()  — 总记录数
```

### 多数据源

数据源在 DAO 层使用，三种方式任选：

**方式一：创建 DAO 时指定数据源**

```java
BaseDao<User> dao = RD.dao().baseDao(User.class, "oracle");
dao.listAll(); // 自动连 oracle
```

**方式二：运行时动态切换**

```java
BaseDao<User> dao = RD.dao().baseDao(User.class);
dao.datasource("oracle").listAll();  // 临时切到 oracle
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
```

注册多数据源：

```java
RD.dataSourceConfig(cfg -> cfg
    .addDataSourceDefault(masterDs)      // 默认数据源
    .addDataSource(oracleDs, "oracle")   // 命名数据源
);
```

ThreadLocal 隔离，多数据源并发互不干扰。

### 批量操作

```java
// 实体批量保存 / 更新
dao.save(userList, 100);           // 每批 100 条
dao.update(userList, 200, true);   // 忽略 null

// 批量保存或更新（增量合并）
SaveOrUpdateBatchResult<User> result = dao.saveOrUpdateThenReturn(
    newUsers,
    500,    // batchSize
    true    // ignoreNulls
);
// result.getSaveList()   — 新增的记录
// result.getUpdateList()  — 更新的记录

// 原生 SQL 批量
RD.namedModify()
    .sql("insert into T_USER(id, name) values(:id, :name)")
    .argsBatch(list -> {
        list.add(MapUtil.init().add("id", "1").add("name", "张三").get());
        list.add(MapUtil.init().add("id", "2").add("name", "李四").get());
    })
    .executeBatch(500);
```

### Spring 集成

Spring Boot 项目推荐使用 [roudan-spring-boot-starter](https://github.com/wsaaaqqq/roudan-spring-boot-starter)，自动读取 `spring.datasource` 完成初始化。手动集成：

```java
@Configuration
public class RoudanConfig {

    public RoudanConfig(DataSource dataSource) {
        RD.dataSourceConfig(cfg -> cfg.addDataSourceDefault(dataSource));
        RDConfig.setUseSpringTransaction(true); // roudan 复用 Spring 管理的连接
    }
}

// 配合 Spring 事务
@Transactional
public void doBusiness() {
    BaseDao<User> dao = RD.dao().baseDao(User.class);
    dao.save(new User().setId("1").setName("test"));

    RD.namedModify()
        .sql("update T_ORDER set status = :s")
        .args("s", "PAID")
        .execute();
}
```

### 调试与日志

```java
// 全局配置（统一走 RDConfig）
RDConfig.setShowSql(true);        // 打印 SQL（默认开启）
RDConfig.setShowSqlArgs(true);    // 打印参数（默认开启）
RDConfig.setShowSqlCaller(true);  // 打印调用方类/方法（默认开启）

// 忽略部分包，避免调用栈定位到框架内部
RDConfig.setIgnorePackagesForDebug(pkgs -> pkgs.add("com.your.wrapper"));
```

---

## 配置项

全局配置统一通过 `RDConfig` 设置：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `RDConfig.setOrmType()` | ORM 注解风格 | `OrmType.JPA` |
| `RDConfig.setShowSql()` | 是否打印 SQL | `true` |
| `RDConfig.setShowSqlArgs()` | 是否打印参数 | `true` |
| `RDConfig.setShowSqlCaller()` | 是否打印调用位置 | `true` |
| `RDConfig.setShowSqlUseSystemOut()` | 用 System.out 打印 SQL | `false` |
| `RDConfig.setAutoCommit()` | 是否自动提交 | `true` |
| `RDConfig.setAutoClose()` | 是否自动关闭连接 | `true` |
| `RDConfig.setUseSpringTransaction()` | 使用 Spring 事务 | `false` |
| `RDConfig.setSqlDir()` | SQL 文件根目录 | `user.dir` |

---

## 开源协议

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
