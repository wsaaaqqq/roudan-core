---
name: roudan
description: Use when working with the roudan (roudan-core, io.github.wsaaaqqq) lightweight JDBC ORM library in plain Java (no Spring required). Entry point for roudan basics - datasource init via Xdb.init(), entity annotations, transaction and logging config (XdbConfig), and choosing among the three programming modes (DAO / SQL / SQL-file). Trigger on keywords like roudan, org.xht, Xdb.init, XdbConfig, RR.dao. For Spring Boot business projects using xdb-springboot-starter, see the xdb skill instead.
---

# Roudan 数据库操作 Skill

> roudan 是纯 JDBC 封装的轻量 ORM 库，零 Spring 依赖，兼容 JPA / MyBatis-Flex / 自有注解。
>
> Maven: `io.github.wsaaaqqq:roudan-core:0.0.2` | 基础包: `org.xht`

## 三种编程模式（按场景选用，可混用）

| 模式 | Skill | 适用场景 |
|------|-----------|----------|
| **DAO 模式** | [roudan-dao](../roudan-dao/SKILL.md) | 单表 CRUD、Wheres 条件查询、动态代理、批量操作 |
| **SQL 模式** | [roudan-sql](../roudan-sql/SKILL.md) | 手写 SQL、多表关联、复杂查询、Table API |
| **SQL 文件模式** | [roudan-sqlfile](../roudan-sqlfile/SKILL.md) | SQL 集中管理、条件参数 `--:`、多数据库方言 |
| **Spring Boot 业务工程** | [xdb](../xdb/SKILL.md) | `xdb-springboot-starter` 多数据源项目：PO/DAO/Service 落地范式 |

## 通用基础

### 初始化数据源（应用启动时执行一次）

```java
HikariDataSource ds = new HikariDataSource();
ds.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
ds.setUsername("root");
ds.setPassword("pwd");
Xdb.init().addDataSourceDefault(ds);
```

多数据源：

```java
Xdb.init()
    .addDataSourceDefault(mysqlDs)                // 默认（无 DbType 则按驱动推断）
    .addDataSourceDefault(pgDs, DbType.PostgreSQL)// 默认 + 显式方言
    .addDataSource(oracleDs, DbType.ORACLE, "oracle") // 命名 + 显式方言
    .addDataSource(h2Ds, "h2");                   // 命名（方言自动推断）
```

注册签名一览（`org.xht.xdb.Xdb` 返回 this，可链式）：

| 方法 | 说明 |
|------|------|
| `addDataSource(ds, dbType, name)` | 命名数据源（完整版） |
| `addDataSource(ds, name)` | 命名数据源（方言按驱动推断） |
| `addDataSource(ds)` | 用默认名注册 |
| `addDataSourceDefault(ds[, dbType])` | 注册为默认数据源 |
| `Xdb.DEFAULT_DATASOURCE_NAME` | 默认数据源名常量 |

`DbType` 常用值：`MYSQL` / `ORACLE` / `DAMENG` / `PostgreSQL` / `KINGBASE` / `SQLSERVER` / `H2` / `SQLITE`…（全量见 `org.xht.xdb.enums.DbType`；方言决定分页 SQL 与 SQL 文件后缀选择）。

> `RR`（`org.xht.rr.RR`）与 `RD`（`org.xht.rd.RD`）是同一套 API 的双门面（方法镜像）：
> `dao()` 取 DAO、`query()/namedQuery()` 查、`modify()/namedModify()` 改，详见 roudan-dao / roudan-sql。

### 实体类（必须标注 @Id，三种注解风格任选）

> 默认按 `OrmType.JPA` 识别注解；使用 MyBatis-Flex 等风格时先
> `XdbConfig.setOrmType(OrmType.MYBATIS_FLEX)`（见下方配置项 `ormType`）。

```java
// 风格1：JPA（javax/jakarta.persistence）
@Data @Accessors(chain = true)
@Table(name = "T_USER")
public class User implements Serializable {
    @Id @Column(name = "ID")
    private String id;
    private String name;       // 列名相同时省略 @Column
}

// 风格2：MyBatis-Flex（com.mybatisflex.annotation）
@Table(value = "T_USER")
public class User implements Serializable {
    @Id @Column("ID")
    private String id;
    private String name;
}

// 风格3：自有注解（org.xht.xdb.orm 相关注解，零第三方依赖）
```

> `@Accessors(chain = true)` 让 setter 返回 this，支持 `new User().setId("1").setName("a")` 链式构建；
> 列名默认 `camelCase → UPPER_SNAKE_CASE` 映射，同名可省 `@Column`。

### 日志与事务全局配置

```java
XdbConfig.setShowSql(true);              // 打印 SQL
XdbConfig.setShowSqlArgs(true);          // 打印参数
XdbConfig.setUseSpringTransaction(true); // 参与 Spring 事务
```

常用配置项（`org.xht.xdb.XdbConfig`，均有 get/set）：

| 配置项 | 说明 |
|--------|------|
| `ormType` | 实体映射风格（`OrmType`：JPA / MyBatis-Flex / 自有注解，默认 JPA） |
| `sqlDir` | `sqlFile(String)` 相对路径的根目录（默认 user.dir） |
| `autoCommit` | DML 默认是否自动提交 |
| `autoClose` | 默认是否自动关闭连接 |
| `useSpringTransaction` | 参与 Spring 事务（`@Transactional`） |
| `showSql` / `showSqlArgs` | 打印 SQL / 参数 |
| `showSqlExecuteTime` | 打印执行耗时 |
| `logLevel` | SQL 日志级别 |
| `showSqlCaller` | 打印 SQL 调用者堆栈 |

### Spring Boot 集成

```java
@Configuration
public class RoudanConfig {
    @PostConstruct public void init() { XdbConfig.setUseSpringTransaction(true); }
    @Bean public Xdb xdb(DataSource ds) { return Xdb.init().addDataSourceDefault(ds); }
}
```
