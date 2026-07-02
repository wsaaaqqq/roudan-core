package org.xht.xdb;

import org.slf4j.event.Level;
import org.xht.xdb.enums.OrmType;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Xdb配置类。
 */
@SuppressWarnings("UnusedReturnValue")
public class XdbConfig {
    private static final XdbConfig INSTANCE = new XdbConfig();
    private String sqlDir;
    private OrmType ormType = OrmType.JPA;
    private boolean autoCommit = true;
    private boolean autoClose = true;
    private boolean useSpringTransaction = false;
    private final ThreadLocal<Level> logLevel = new ThreadLocal<Level>() {{
        set(Level.DEBUG);
    }};
    private boolean showSql = true;
    private boolean showSqlArgs = true;
    private boolean showSqlUseSystemOut = false;
    private boolean showSqlFlagOfArgsInComment = false;
    private boolean showSqlCaller = true;
    private final Set<String> ignorePackagesForDebug = new HashSet<String>() {{
        add("org.xht.xdb.");
        add("org.xht.rr.");
        add("java.");
        add("javax.");
        add("sun.reflect.");
        add("org.springframework.");
        add("org.junit.");
        add("com.intellij.");
    }};
    private boolean showSqlExecuteTime = true;

    public static boolean isShowSqlExecuteTime() {
        return INSTANCE.showSqlExecuteTime;
    }

    public static XdbConfig setShowSqlExecuteTime(boolean showSqlExecuteTime) {
        INSTANCE.showSqlExecuteTime = showSqlExecuteTime;
        return INSTANCE;
    }

    public static String getSqlDir() {
        return INSTANCE.sqlDir;
    }

    public static XdbConfig setSqlDir(String sqlDir) {
        INSTANCE.sqlDir = sqlDir;
        return INSTANCE;
    }

    public static OrmType getOrmType() {
        return INSTANCE.ormType;
    }

    public static XdbConfig setOrmType(OrmType ormType) {
        INSTANCE.ormType = ormType;
        return INSTANCE;
    }

    public static boolean isAutoCommit() {
        return INSTANCE.autoCommit;
    }

    public static XdbConfig setAutoCommit(boolean autoCommit) {
        INSTANCE.autoCommit = autoCommit;
        return INSTANCE;
    }

    public static boolean isAutoClose() {
        return INSTANCE.autoClose;
    }

    public static XdbConfig setAutoClose(boolean autoClose) {
        INSTANCE.autoClose = autoClose;
        return INSTANCE;
    }

    public static boolean isUseSpringTransaction() {
        return INSTANCE.useSpringTransaction;
    }

    public static XdbConfig setUseSpringTransaction(boolean useSpringTransaction) {
        INSTANCE.useSpringTransaction = useSpringTransaction;
        return INSTANCE;
    }

    public static Level getLogLevel() {
        return INSTANCE.logLevel.get();
    }

    public static XdbConfig setLogLevel(Level logLevel) {
        INSTANCE.logLevel.set(logLevel);
        return INSTANCE;
    }

    public static boolean isShowSql() {
        return INSTANCE.showSql;
    }

    public static XdbConfig setShowSql(boolean showSql) {
        INSTANCE.showSql = showSql;
        XdbConfig.setShowSqlExecuteTime(showSql);
        return INSTANCE;
    }

    public static boolean isShowSqlArgs() {
        return INSTANCE.showSqlArgs;
    }

    public static XdbConfig setShowSqlArgs(boolean showSqlArgs) {
        INSTANCE.showSqlArgs = showSqlArgs;
        return INSTANCE;
    }

    public static boolean isShowSqlUseSystemOut() {
        return INSTANCE.showSqlUseSystemOut;
    }

    public static XdbConfig setShowSqlUseSystemOut(boolean showSqlUseSystemOut) {
        INSTANCE.showSqlUseSystemOut = showSqlUseSystemOut;
        return INSTANCE;
    }

    public static boolean isShowSqlFlagOfArgsInComment() {
        return INSTANCE.showSqlFlagOfArgsInComment;
    }

    public static XdbConfig setShowSqlFlagOfArgsInComment(boolean showSqlFlagOfArgsInComment) {
        INSTANCE.showSqlFlagOfArgsInComment = showSqlFlagOfArgsInComment;
        return INSTANCE;
    }

    public static boolean isShowSqlCaller() {
        return INSTANCE.showSqlCaller;
    }

    public static XdbConfig setShowSqlCaller(boolean showSqlCaller) {
        INSTANCE.showSqlCaller = showSqlCaller;
        return INSTANCE;
    }

    public static Set<String> getIgnorePackagesForDebug() {
        return INSTANCE.ignorePackagesForDebug;
    }

    public static XdbConfig setIgnorePackagesForDebug(Consumer<Set<String>> configIgnorePackagesForDebug) {
        configIgnorePackagesForDebug.accept(getIgnorePackagesForDebug());
        return INSTANCE;
    }
}
