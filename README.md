# roudan

中文名：肉蛋，是一款轻量级高性能数据库操作库，旨在平衡开发效率与技术灵活性。它融合了 JPA 面向实体的极简开发体验与 MyBatis 原生
SQL 的强大控制力，同时高效的批处理能力。

# 入门

## 添加依赖
~~~
<dependency>
    <groupId>io.github.wsaaaqqq</groupId>
    <artifactId>roudan-core</artifactId>
    <version>0.0.1</version>
</dependency>

## 创建实体类

~~~
@Data
@ToString
@Accessors(chain = true)
@Table(name = "T_TEST")
public class PoJPA implements Serializable {
    @Id
    @Column(name = "ID")
    private String id;
    private String name;
    private String code;
    private String type;
    private Integer idx;
}
~~~

## 初始化roudan
~~~

~~~

## 实体操作
~~~
BaseDao<PoJPA> dao = RR.dao().baseDao(PoJPA.class);
~~~
