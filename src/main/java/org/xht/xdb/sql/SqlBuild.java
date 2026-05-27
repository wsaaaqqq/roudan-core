package org.xht.xdb.sql;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.util.MapUtil;

import java.io.Serializable;
import java.util.Collection;

/**
 * sql执行工具
 */
@SuppressWarnings("ALL")
@Slf4j
public class SqlBuild implements Serializable {
    private String sql = "";
    private MapUtil<Object> args = MapUtil.init();

    public static SqlBuild init() {
        return new SqlBuild();
    }

    public SqlBuild add(String sqlPart) {
        if (this.sql.endsWith("\n")) {
            this.sql += sqlPart;
        } else {
            this.sql += "\n" + sqlPart;
        }
        return this;
    }

    public SqlBuild addIfNotNull(String sqlPart, String argName, Object argValue) {
        if (argValue != null) {
            if (!this.sql.endsWith("\n"))
                this.sql += "\n";
            this.sql += sqlPart;
            this.args.add(argName, argValue);
        }
        return this;
    }


    public SqlBuild addIfNotEmpty(String sqlPart, String argName, String argValue) {
        if (argValue != null && !argValue.isEmpty()) {
            if (!this.sql.endsWith("\n"))
                this.sql += "\n";
            this.sql += sqlPart;
            this.args.add(argName, argValue);
        }
        return this;
    }


    public SqlBuild addIfNotEmpty(String sqlPart, String argName, Collection argValue) {
        if (argValue != null && !argValue.isEmpty()) {
            if (!this.sql.endsWith("\n"))
                this.sql += "\n";
            this.sql += sqlPart;
            this.args.add(argName, argValue);
        }
        return this;
    }

    public SqlBuild addIf(String sqlPart, String argName, Object argValue, String sqlPartIfNull) {
        if (!this.sql.endsWith("\n"))
            this.sql += "\n";
        if (argValue == null) {
            this.sql += sqlPartIfNull;
        } else {
            this.sql += sqlPart;
            this.args.add(argName, argValue);
        }
        return this;
    }

    public SqlBuild out() {
        log.info("sql: {}", sql);
        log.info("args: {}", args);
        return this;
    }

    public SqlBuild addIfNotNull(String sqlPart, Object value) {
        if (value != null) {
            if (!this.sql.endsWith("\n"))
                this.sql += "\n";
            this.sql += sqlPart;
        }
        return this;
    }

    public SqlBuild addIfNotEmpty(String sqlPart, String value) {
        if (value != null && !value.isEmpty()) {
            if (!this.sql.endsWith("\n"))
                this.sql += "\n";
            this.sql += sqlPart;
        }
        return this;
    }

    public String getSql() {
        sql = sql.replaceAll("where[ \n]+1[ ]*=[ ]*1[ \n]+[aA][nN][dD][ \n]*", "where ");
        sql = sql.replaceAll("where[ \n]+1[ ]*=[ ]*1[ \n]*$", "");
        return sql;
    }

    public SqlBuild addWhereStart() {
        if (sql == null || sql.isEmpty()) {
            sql = "\nwhere 1=1 ";
        } else {
            sql = sql.replaceAll("where[ \n]+1[ ]*=[ ]*1[ \n]$", "");
            sql = sql.replaceAll("where[ \n]+1[ ]*=[ ]*1[ \n]$", "");
            sql += "\nwhere 1=1\n";
        }
        return this;
    }

    public MapUtil<Object> getArgs() {
        return args;
    }
}
