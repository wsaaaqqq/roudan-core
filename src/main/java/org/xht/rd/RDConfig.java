package org.xht.rd;

import org.xht.rr.RRConfig;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.OrmType;

import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class RDConfig {
    @SuppressWarnings("InstantiationOfUtilityClass")
    private static final RRConfig INSTANCE = new RRConfig();

    public static String getSqlDir() {
        return XdbConfig.getSqlDir();
    }

    public static OrmType getOrmType() {
        return XdbConfig.getOrmType();
    }

    public static boolean isAutoCommit() {
        return XdbConfig.isAutoCommit();
    }

    public static boolean isAutoClose() {
        return XdbConfig.isAutoClose();
    }

    public static boolean isUseSpringTransaction() {
        return XdbConfig.isUseSpringTransaction();
    }

    public static boolean isShowSql() {
        return XdbConfig.isShowSql();
    }

    public static boolean isShowSqlArgs() {
        return XdbConfig.isShowSqlArgs();
    }

    public static boolean isShowSqlUseSystemOut() {
        return XdbConfig.isShowSqlUseSystemOut();
    }

    public static boolean isShowSqlFlagOfArgsInComment() {
        return XdbConfig.isShowSqlFlagOfArgsInComment();
    }

    public static boolean isShowSqlCaller() {
        return XdbConfig.isShowSqlCaller();
    }

    public static RRConfig setSqlDir(String sqlDir) {
        XdbConfig.setSqlDir(sqlDir);
        return INSTANCE;
    }

    public static RRConfig setOrmType(OrmType ormType) {
        XdbConfig.setOrmType(ormType);
        return INSTANCE;
    }

    public static RRConfig setAutoCommit(boolean autoCommit) {
        XdbConfig.setAutoCommit(autoCommit);
        return INSTANCE;
    }

    public static RRConfig setAutoClose(boolean autoClose) {
        XdbConfig.setAutoClose(autoClose);
        return INSTANCE;
    }

    public static RRConfig setUseSpringTransaction(boolean useSpringTransaction) {
        XdbConfig.setUseSpringTransaction(useSpringTransaction);
        return INSTANCE;
    }

    public static RRConfig setShowSql(boolean showSql) {
        XdbConfig.setShowSql(showSql);
        return INSTANCE;
    }

    public static RRConfig setShowSqlArgs(boolean showSqlArgs) {
        XdbConfig.setShowSqlArgs(showSqlArgs);
        return INSTANCE;
    }

    public static RRConfig setShowSqlUseSystemOut(boolean showSqlUseSystemOut) {
        XdbConfig.setShowSqlUseSystemOut(showSqlUseSystemOut);
        return INSTANCE;
    }

    public static RRConfig setShowSqlFlagOfArgsInComment(boolean showSqlFlagOfArgsInComment) {
        XdbConfig.setShowSqlFlagOfArgsInComment(showSqlFlagOfArgsInComment);
        return INSTANCE;
    }

    public static RRConfig setShowSqlCaller(boolean showSqlCaller) {
        XdbConfig.setShowSqlCaller(showSqlCaller);
        return INSTANCE;
    }

    public static RRConfig setIgnorePackagesForDebug(Consumer<Set<String>> configIgnorePackagesForDebug) {
        configIgnorePackagesForDebug.accept(XdbConfig.getIgnorePackagesForDebug());
        return INSTANCE;
    }
}
