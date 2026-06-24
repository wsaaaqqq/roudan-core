package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.orm.util.AnnoUtil;
import org.xht.xdb.vo.Row;

import java.lang.reflect.Field;
import java.util.List;

/**
 * sql执行工具
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Slf4j
public class SqlTableInfo {
    private final String tableName;
    private String id;
    private Object value;

    public SqlTableInfo(String tableName) {
        this.tableName = tableName;
    }

    public <ID> SqlTableInfo id(String idCol, ID idValue) {
        this.id = idCol;
        this.value = idValue;
        return this;
    }

    public <ID> SqlTableInfo id(ID idValue) {
        return id("id", idValue);
    }

    public Row execute(boolean... autoCloseConnection) {
        return Xdb.sql("select * from " + tableName + " where " + id + "= :" + id)
                .sqlArg(id, value)
                .executeQuery(autoCloseConnection)
                .firstRow();
    }

    public Row execute() {
        return execute(XdbConfig.isAutoClose());
    }

    public <T> T execute(Class<T> tClass, boolean... autoCloseConnection) {
        T t = Xdb.sql("select * from " + tableName + " where " + id + "= :" + id)
                .sqlArg(id, value)
                .executeQuery(autoCloseConnection)
                .firstBean(tClass);
        List<Field> jonColumnFields = AnnoUtil.getFieldsByBeanClass(tClass, AnnoUtil.JPA_ROOT + ".JoinColumn");
        if (jonColumnFields == null || jonColumnFields.isEmpty()) return t;
        for (Field jonColumnField : jonColumnFields) {
            AnnoUtil.getAnnoValueOfField(jonColumnField, AnnoUtil.JPA_ROOT + ".JoinColumn", "name");
        }
        return t;
    }

    public <T> T execute(Class<T> tClass) {
        return execute(tClass, XdbConfig.isAutoCommit(), XdbConfig.isAutoClose());
    }

    public SqlTableInfo debug() {
        Xdb.sql("select * from " + tableName + " where " + id + "= :" + id).sqlArg(id, value).debug();
        return this;
    }

}
