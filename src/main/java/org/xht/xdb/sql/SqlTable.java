package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.util.CloseUtil;
import org.xht.xdb.vo.Row;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTable {

    private final String tableName;

    public SqlTable(String tableName) {
        this.tableName = tableName;
    }

    public SqlTableSave save() {
        return new SqlTableSave(this.tableName);
    }

    public SqlTableMerge merge() {
        return new SqlTableMerge(this.tableName);
    }

    public SqlTableUpdate update() {
        return new SqlTableUpdate(this.tableName);
    }

    public SqlTableInfo info() {
        return new SqlTableInfo(this.tableName);
    }

    public SqlTableInfos list() {
        return new SqlTableInfos(this.tableName);
    }

    public List<Row> listAll() {
        return Xdb.sql("select * from " + tableName).executeQuery(true).resultRow();
    }

    /**
     * 主键列默认为 id, 使用 .id("obj_id") 进行切换
     */
    public SqlTableDelete delete() {
        return new SqlTableDelete(this.tableName);
    }

    public boolean exist() {
        String sql = "select 1 from " + this.tableName + " limit 1";
        Connection conn = Xdb.getConnection();
        if (conn == null)
            throw new RuntimeException("can not get connection from db");
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (Exception e) {
            log.error("error: {} - sql: {}", e.getMessage(), sql);
            return false;
        } finally {
            CloseUtil.close(conn, XdbConfig.isAutoClose());
        }
    }
}
