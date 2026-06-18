package test.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xht.xdb.vo.Wheres;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wheres SQL生成测试")
public class WheresTest {

    @Test
    @DisplayName("anyColContain 不产生多余 and")
    void anyColContainShouldNotWrapWithAnd() {
        Wheres wheres = Wheres.init()
                .anyColContain("测试", "CZPBH", "CZMD");

        String sql = wheres.getWhereSql();
        System.out.println("anyColContain sql: " + sql);

        assertFalse(sql.contains("and CZPBH") || sql.contains("CZMD and )"),
                "不应该出现多余的 and: " + sql);
        assertTrue(sql.contains("CZPBH") && sql.contains("or CZMD"),
                "应该包含正确的 or 连接: " + sql);
        assertFalse(sql.contains(" ( and ") || sql.contains(" and ) "),
                "括号内不应该有孤立的 and: " + sql);
    }

    @Test
    @DisplayName("sub + or 混合场景")
    void subWithOr() {
        Wheres wheres = Wheres.init()
                .sub(sub -> {
                    sub.or();
                    sub.eq("A", "a");
                    sub.eq("B", "b");
                });

        String sql = wheres.getWhereSql();
        System.out.println("sub+or sql: " + sql);

        assertTrue(sql.contains("A = :") || sql.contains("\"A\""),
                "应该包含 A 条件");
        assertTrue(sql.contains(" or "), "子查询应该使用 or 连接");
        assertFalse(sql.contains(" ( and "), "不应该出现 ( and ");
        assertFalse(sql.contains(" and ) "), "不应该出现 and )");
    }

    @Test
    @DisplayName("sub + and 混合场景")
    void subWithAnd() {
        Wheres wheres = Wheres.init()
                .sub(sub -> {
                    sub.and();
                    sub.eq("A", "a");
                    sub.eq("B", "b");
                });

        String sql = wheres.getWhereSql();
        System.out.println("sub+and sql: " + sql);

        assertTrue(sql.contains("A = :") || sql.contains("\"A\""),
                "应该包含 A 条件");
        assertFalse(sql.contains(" ( or "), "子查询应该使用 and 连接而非 or");
        assertFalse(sql.contains(" ( and "), "不应该出现 ( and ");
        assertFalse(sql.contains(" and ) "), "不应该出现 and )");
    }

    @Test
    @DisplayName("contain + anyColContain 组合")
    void containWithAnyColContain() {
        Wheres wheres = Wheres.init()
                .contain("ARCHIVE_STATE", "100")
                .anyColContain("测试", "CZPBH", "CZMD");

        String sql = wheres.getWhereSql();
        System.out.println("contain+anyColContain sql: " + sql);

        assertTrue(sql.contains("ARCHIVE_STATE"), "应该包含 ARCHIVE_STATE 条件");
        assertTrue(sql.contains("CZPBH"), "应该包含 CZPBH 条件");
        assertTrue(sql.contains("CZMD"), "应该包含 CZMD 条件");
        assertFalse(sql.contains(" ( and "), "不应该出现 ( and ");
        assertFalse(sql.contains(" and ) "), "不应该出现 and )");
    }

    @Test
    @DisplayName("anyColContain + between 组合(复现线上场景)")
    void anyColContainWithBetween() {
        Wheres wheres = Wheres.init()
                .anyColContain("测试", "CZPBH", "CZMD")
                .between("CREATETIME", null, null, true);

        String sql = wheres.getWhereSql();
        System.out.println("anyColContain+between sql: " + sql);

        assertTrue(sql.contains("CZPBH"), "应该包含 CZPBH 条件");
        assertTrue(sql.contains("CZMD"), "应该包含 CZMD 条件");
        assertFalse(sql.contains(" ( and "), "不应该出现 ( and ");
        assertFalse(sql.contains(" and ) "), "不应该出现 and )");
    }

    @Test
    @DisplayName("anyColContain 单独使用(bug复现)")
    void anyColContainAlone() {
        Wheres wheres = Wheres.init()
                .anyColContain("测试", "CZPBH", "CZMD");

        String sql = wheres.getWhereSql();
        System.out.println("anyColContain alone sql: " + sql);

        assertFalse(sql.contains("( and"), "不应该有 ( and");
        assertFalse(sql.contains("and )"), "不应该有 and )");
    }

    @Test
    @DisplayName("sub 嵌套 anyColContain")
    void subNestedAnyColContain() {
        Wheres wheres = Wheres.init()
                .sub(sub -> sub.anyColContain("测试", "CZPBH", "CZMD"));

        String sql = wheres.getWhereSql();
        System.out.println("sub+anyColContain sql: " + sql);

        assertFalse(sql.contains("( and"), "不应该有 ( and");
        assertFalse(sql.contains("and )"), "不应该有 and )");
    }
}
