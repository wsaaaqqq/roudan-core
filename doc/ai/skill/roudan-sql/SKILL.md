---
name: roudan-sql
description: Use for roudan SQL-mode hand-written SQL execution via Xdb.sql() with :named parameters - complex queries, multi-table joins, reports, the Table API (Xdb.table()), pagination (Xdb.sqlPage / SqlBuild), RR/RD facades (RR.query/namedQuery/modify/namedModify), transactions, and multi-datasource switching. Trigger when using Xdb.sql, Xdb.table, Xdb.sqlPage, RR.query, RR.namedModify, executeQuery/executeUpdate, resultBean, firstBean, resultFirstColumn, sqlArgIf, or MapUtil.init with the roudan library.
---

# Roudan SQL 模式

> 通过 `Xdb.sql()` 执行手写 SQL，使用 `:paramName` 命名参数占位符。
> 适用于复杂查询、多表关联、报表统计等 DAO 模式覆盖不了的场景。

## 基本用法

```java
import org.xht.xdb.Xdb;
import org.xht.xdb.util.MapUtil;
import org.xht.xdb.vo.PageResult;
```

## 查询

```java
// 返回实体列表
List<User> users = Xdb.sql("select * from T_USER where type = :type")
    .sqlArg("type", "ADMIN")
    .executeQuery()
    .resultBean(User.class);

// 返回多行 Row（Row 继承 HashMap，键默认为大写下划线，如 USER_ID）
List<Row> rows = Xdb.sql("select * from T_USER where type = :type")
    .sqlArg("type", "A")
    .executeQuery()
    .resultRow();

// 返回首行 Row
Row row = Xdb.sql("select * from T_USER where id = :id")
    .sqlArg("id", "1")
    .executeQuery()
    .firstRow();

// Row 大小写变体：firstRowOrigin/firstRowCamel/firstRowLowerCase/firstRowUpperCase
// Row 类型取值：row.getString("NAME", null)、row.getLong("IDX", 0L)、row.getInt(...) 等

// 返回首行首列单值
String name = Xdb.sql("select name from T_USER where id = :id")
    .sqlArg("id", "1")
    .executeQuery()
    .firstRowFirstCol(String.class);

// 返回首行单 Bean
User user = Xdb.sql("select * from T_USER where id = :id")
    .sqlArg("id", "1")
    .executeQuery()
    .firstBean(User.class);

// 返回单列列表
List<String> names = Xdb.sql("select name from T_USER where type = :type")
    .sqlArg("type", "A")
    .executeQuery()
    .resultFirstColumn(String.class);

// 返回首行 Map
Map<String, Object> map = Xdb.sql("select * from T_USER where id = :id")
    .sqlArg("id", "1")
    .executeQuery()
    .first();

// 返回 List<Map>
List<Map<String, Object>> maps = Xdb.sql("select * from T_USER where type = :type")
    .sqlArg("type", "A")
    .executeQuery()
    .result();

// List<Map> 大小写变体：toCamelList()/toLowerCaseList()/toUpperCaseList()/toOriginList()
// List<Row> 大小写变体：resultRowCamel()/resultRowLowerCase()/resultRowUpperCase()/resultRowOrigin()
```

### 首行 / 分片 / 流式

```java
import org.xht.xdb.sql.ResultQuery;
import org.xht.xdb.sql.ResultQueryBatch;

// 注意：每个取值方法都会消费并关闭结果集，不能在同一 ResultQuery 上连续调用多个取值方法
Map<String, Object> firstMap = Xdb.sql("select * from T_USER where type = :type")
    .sqlArg("type", "A").executeQuery().first();   // 首行 Map（无行则 null）
Object[] firstArr = Xdb.sql("select * from T_USER").executeQuery().firstArray();  // 首行数组
List<Map<String, Object>> slice = Xdb.sql("select * from T_USER").executeQuery().limit(0, 10);  // 内存分页
List<User> sliceBean = Xdb.sql("select * from T_USER").executeQuery().limitBean(User.class, 0, 10);

// 大结果集流式处理（batchSize 行一批，自动关闭）
try (ResultQueryBatch batch = q.resultBatch(200)) {
    batch.forEachBatchRow(rows -> rows.forEach(this::handle));
}

// 分组计数：select type, count(1) c ... → {ADMIN=10, USER=20}
Map<String, Object> groupCount = Xdb.sql("select type, count(1) c from T_USER group by type")
    .executeQuery().toGroupCount();

// 自定义行映射
List<String> names = Xdb.sql("select * from T_USER").executeQuery()
    .resultTo(row -> row.getString("NAME", null));

// TreeMap 有序容器：以实体自身为键（实体需实现 Comparable）
TreeMap<User, User> map = Xdb.sql("select * from T_USER").executeQuery()
    .resultTreeMap(User.class);
```

## RR/RD 门面（快捷入口）

`org.xht.rr.RR` 与 `org.xht.rd.RD` 是同一套 API 的双门面（方法镜像），按占位符风格四选一：

| 门面方法 | 占位符 | 参数形态 | 终结方法 |
|----------|--------|----------|----------|
| `RR.query()` | `?` | `args(Object...)` | `executeQuery()/executeCount()` |
| `RR.namedQuery()` | `:name` | `args(MapUtil)/args(k,v)/args(k,v,cond)` | `executeQuery()/executeCount()` |
| `RR.modify()` | `?` | `argsBatch(List<Object[]>)` | `execute()/executeBatch(n)` |
| `RR.namedModify()` | `:name` | `argsBatch(List<Map>)` | `execute()/executeBatch(n)` |

```java
import org.xht.rr.RR;

// ? 占位查询
List<Row> rows = RR.query().sql("select * from T_USER where type = ?")
    .args("ADMIN").executeQuery().resultRow();

// :name 占位查询
List<User> users = RR.namedQuery().sql("select * from T_USER where type = :type")
    .args("type", "ADMIN").executeQuery().resultBean(User.class);

// ? 占位批量插入（List<Object[]>，每项与 ? 一一对应）
RR.modify()
    .sql("insert into T_USER (id, name) values (?, ?)")
    .argsBatch(rows)
    .executeBatch(100);

// :name 占位批量插入（List<Map<String,Object>>，Consumer 形态可流式构建）
RR.namedModify()
    .sql("insert into T_USER (id, name) values (:id, :name)")
    .argsBatch(list -> {
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", "id" + i);
            row.put("name", "name" + i);
            list.add(row);
        }
    })
    .executeBatch(100);
```

> `query/namedQuery` 另支持 `sqlFile(...)` 加载 SQL 文件、`executeWithHandler` 自定义 ResultSet 处理；
> 四者都支持 `datasource(name)/datasourceDefault()` 切换数据源（见多数据源章节）。

## 增删改

```java
// 插入
Xdb.sql("insert into T_USER (id, name, type) values (:id, :name, :type)")
    .sqlArgs(MapUtil.init().add("id", "1").add("name", "张三").add("type", "A"))
    .executeUpdate();

// 更新
int rows = Xdb.sql("update T_USER set name = :name where id = :id")
    .sqlArgs(MapUtil.init().add("name", "李四").add("id", "1"))
    .executeUpdate();

// 删除
Xdb.sql("delete from T_USER where id = :id")
    .sqlArg("id", "1")
    .executeUpdate();

// 统计
long count = Xdb.sql("select count(1) from T_USER where type = :type")
    .sqlArg("type", "ADMIN")
    .executeCount();
```

## 参数设置

```java
// 逐个参数
Xdb.sql("...").sqlArg("name", "value");

// 批量参数（推荐）
Xdb.sql("...").sqlArgs(MapUtil.init()
    .add("k1", v1)
    .add("k2", v2));

// 条件参数（等价于 SQL 文件的 --: 行，满足条件才绑定；-- 必须位于行首）
Xdb.sql("... where 1=1\n-- and type = :type")
    .sqlArgIf("type", type);                        // 非空才绑定+启用该行
Xdb.sql("...").sqlArgIf("type", type, hasType);     // 布尔条件为真才绑定
Xdb.sql("...").sqlArgIf("type", type, () -> check()); // Supplier 延迟求值

// 普通 Map 必须转换（sqlArgs 只收 MapUtil）
Map<String, Object> params = new HashMap<>();
params.put("id", "1");
Xdb.sql("...").sqlArgs(MapUtil.clone(params));
```

`MapUtil.init().add(k, v)` 是 roudan 的自定义 Map，链式添加参数；另有 `addIf/addOnlyNotNull/addJoinString/del/clone/formatKey` 等方法。

## Table API（免写 INSERT/UPDATE/DELETE 语句）

```java
import org.xht.xdb.Xdb;
import org.xht.xdb.vo.DataCompareResult;
import org.xht.xdb.vo.Row;

// 插入（另有 rowsBean/rowsMap/rowMap/rowMapUtil 批量形态，batchSize() 设分批大小）
Xdb.table("T_USER").save().rowBean(user).execute();

// 按 ID 删除：id 列名默认 "id"（自动兼容 id/ID 键），值从 row/rowBean 里取
Xdb.table("T_USER").delete().row(Row.init().set("id", "1")).execute();
// id 列名非默认时先声明列名
Xdb.table("T_USER").delete().id("USER_ID").row(Row.init().set("USER_ID", "1")).execute();

// 按 ID 查询单条（id(ID) 单参版；或 id(idCol, idValue) 双参版）
Row info = Xdb.table("T_USER").info().id("1").execute();
User u = Xdb.table("T_USER").info().id("USER_ID", "1").execute(User.class);

// 按 ID 列表批量查询（自动按 batchSize 分批 IN 查询，默认 500）
List<User> list = Xdb.table("T_USER").list().ids("id", idList).execute(User.class);
List<Row> rows = Xdb.table("T_USER").list().ids("id", idList).execute();

// 整表查询
List<Row> all = Xdb.table("T_USER").listAll();

// 存在性
boolean exists = Xdb.table("T_USER").exist();

// 更新（ignoreNulls(true) 跳过 null 字段；rowBean(t, idValue) 可另行指定主键值）
Xdb.table("T_USER").update().ignoreNulls(true).rowBean(user).execute();

// 合并（新旧数据比对：computeInsert/computeUpdate/computeDelete + allowInsert/allowUpdate/allowDelete 开关，keyColumns 声明比对键）
DataCompareResult<Row> r = Xdb.table("T_USER").merge()
    .keyColumns("id")
    .dataNew(newRows)
    .dataOldQueryTableAllData()
    .execute();
```

> Table API 覆盖 save/merge/update/info/list/listAll/delete/exist 全链路；条件查询请用 `Xdb.sql()` 或 DAO 模式，`list()` 只支持按 ID 列表查询。

## 分页查询

```java
import org.xht.xdb.Xdb;
import org.xht.xdb.vo.PageResult;

PageResult<User> page = Xdb.sqlPage()
    .sqlSelect("select u.*")
    .sqlMain("from T_USER u where u.type = :type")
    .sqlOrder("order by u.create_time desc")
    .sqlArgs(MapUtil.init().add("type", "NORMAL"))
    .pageIndex(1)
    .pagePerSize(20)
    .resultBean(User.class);
```

> 注意：`SqlPageTool` 只有 `sqlArgs(MapUtil)`，没有单个 `sqlArg`；`pageIndex` 从 1 起始。

框架自动生成对应的 `count(1)` 查询和方言适配的分页 SQL。

### 带动态条件的复杂分页

```java
import org.xht.xdb.sql.SqlBuild;

String name = params.get("name");  // 可能为 null

PageResult<User> page = Xdb.sqlPage()
    .sqlSelect("select u.*")
    .sqlMain("from T_USER u",
        SqlBuild.init()
            .addWhereStart()
            .addIfNotEmpty("and u.type = :type", "type", params.get("type"))
            .addIfNotEmpty("and u.name like :name", "name", name)
    )
    .sqlOrder("order by u.create_time desc")
    .pageIndex(1).pagePerSize(20)
    .resultBean(User.class);
```

## 多数据源

```java
// 切换数据源：设置的是当前线程的数据源选择，会影响之后的调用，用完记得切回
Xdb.datasource("oracle");
Xdb.sql("select * from T_USER").executeQuery();
Xdb.datasourceDefault();  // 切回默认

// 等价写法（selectDataSourceByName / selectDataSourceDefault）
Xdb.selectDataSourceByName("oracle");
Xdb.sql("select * from T_USER").executeQuery();
Xdb.selectDataSourceDefault();  // 切回默认
```

> `Xdb.datasourceDefault()` 等价于 `selectDataSourceDefault()` 的链式版；
> `RR.datasource(name)` / `RR.datasourceDefault()` / `RD` 同理。
> 默认数据源名常量：`Xdb.DEFAULT_DATASOURCE_NAME`。

## 事务

```java
Connection conn = Xdb.getConnection();
conn.setAutoCommit(false);
Xdb.setConnection(conn);

try {
    Xdb.sql("insert into T_ORDER (id,amt) values (:id,:amt)")
        .sqlArgs(MapUtil.init().add("id","1").add("amt",100))
        .executeUpdate();
    Xdb.sql("update T_ACCOUNT set bal = bal - :amt where id = :aid")
        .sqlArgs(MapUtil.init().add("amt",100).add("aid","A1"))
        .executeUpdate();
    conn.commit();
} catch (Exception e) {
    conn.rollback();
} finally {
    XdbConfig.setAutoClose(true); // 自动关闭连接
}

// Spring 事务：设置后直接用 @Transactional
XdbConfig.setUseSpringTransaction(true);
```

## 调试

```java
Xdb.sql("select * from T_USER").debug().executeQuery();  // 单次打印完整SQL
Xdb.sql("...").debug(Level.DEBUG).executeQuery();        // 指定日志级别
XdbConfig.setShowSql(true);       // 全局打印
XdbConfig.setShowSqlArgs(true);   // 打印参数值
```

> `executeUpdateWithoutFormat()`：跳过 SQL 格式化（注释开关解析）直接执行，适用于 SQL 中含 `--` 但非条件的场景。
