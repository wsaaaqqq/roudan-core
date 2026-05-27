package org.xht.xdb.util;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.Xdb;
import org.xht.xdb.XdbConfig;
import org.xht.xdb.enums.DbType;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("rawtypes")
@Slf4j
public class SqlFileUtil {

    /**
     * <pre>
     * 功能：动态组装sql
     *    1、sqlFileClass和sqlFileName需在同一目录层级
     *    2、sql文件中的参数使用冒号占位符，如" :id "," :name "
     *    3、冒号占位符前后必须留有空格
     *    4、注释语句以1行为单位，1行内的所有参数占位符如果都在sqlArgs中传入，则自动放开此行语句
     * </pre>
     *
     * @param sqlFileClass 与sql文件同目录的任意clazz（方便通过clazz获取类加载路径，从而找到sql文件）
     * @param sqlFileName  sql文件名
     * @param sqlArgs      sql查询参数
     * @return sql
     */
    public static <T> String getSql(Class<T> sqlFileClass, String sqlFileName, MapUtil sqlArgs) {
        InputStream is = getInputStream(sqlFileClass, sqlFileName);
        return getContext(is, sqlArgs);
    }

    /**
     * 获得sql
     * <pre>
     * 功能：动态组装sql
     *    1、 XdbConfig.setSqlDir
     *    2、sql文件中的参数使用冒号占位符，如" :id "," :name "
     *    3、冒号占位符前后必须留有空格
     *    4、注释语句以1行为单位，1行内的所有参数占位符如果都在sqlArgs中传入，则自动放开此行语句
     * </pre>
     *
     * @param sqlFileRelativePath sql文件相对路径
     * @param sqlArgs             sql查询参数
     * @return sql
     */
    public static String getSql(String sqlFileRelativePath, MapUtil sqlArgs) {
        String sqlDir = XdbConfig.getSqlDir();
        if (sqlDir == null || sqlDir.isEmpty()) {
            sqlDir = System.getProperty("user.dir").concat("/files/sql/");
        } else if (sqlDir.equals(".") || sqlDir.equals("./")) {
            sqlDir = System.getProperty("user.dir").concat("/");
        } else if (!sqlDir.startsWith("/")) {
            sqlDir = System.getProperty("user.dir").concat("/").concat(sqlDir);
        }
        File file = new File(sqlDir, sqlFileRelativePath);
        try {
            InputStream is = Files.newInputStream(file.toPath());
            return getContext(is, sqlArgs);
        } catch (Exception e) {
            throw new RuntimeException("sql file not found: " + file.getAbsolutePath());
        }
    }

    private static InputStream getInputStream(Class<?> sqlFileClass, String sqlFileName) {
        DbType dbType = Xdb.getXDataSource().getDbType();
        String sqlFileNameCopy = sqlFileName;
        //优先查找.sql.oracle和.sql.sqlite后缀的sql文件，找不到时才使用.sql后缀的sql文件
        sqlFileName = DbType.getSqlFileSuffix(sqlFileName, dbType);
        URL url = sqlFileClass.getResource(sqlFileName);
        if (url == null) {
            url = sqlFileClass.getResource(sqlFileNameCopy);
        }
        InputStream is = null;
        if (url != null) {
            try {
                is = url.openStream();
            } catch (IOException e) {
                throw new RuntimeException("sql file found error: " +
                        sqlFileName +
                        " or " +
                        sqlFileNameCopy +
                        " " +
                        e.getMessage());
            }
        }
        return is;
    }

    private static String getContext(InputStream is, MapUtil sqlParams) {
        InputStreamReader isr = null;
        BufferedReader reader = null;
        List<String> lines = new ArrayList<>();
        try {
            String line;
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            CloseUtil.closeNotThrow(is);
            CloseUtil.closeNotThrow(isr);
            CloseUtil.closeNotThrow(reader);
        }
        return dynamicFormatSql(lines, sqlParams);
    }

    @SuppressWarnings("unchecked")
    public static String dynamicFormatSql(List<String> lines, MapUtil sqlParams) {
        StringBuilder sqlBuffer = new StringBuilder();
        boolean multyComment = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("--")) {
                if (sqlParams != null) {
                    line = line.replace("--", "");
                    String realLine = line;
                    Set<String> keys = sqlParams.keySet();
                    if (keys != null) {
                        for (String key : keys) {
                            String _key = ":".concat(key);
                            line = line.replaceAll(_key.concat(" "), "");
                            line = line.replaceAll(_key.concat("\\)"), "");
                            line = line.replaceAll(_key.concat("\\|"), "");
                            line = line.replaceAll(_key.concat("\\,"), "");
                            line = line.replaceAll(_key.concat("$"), "");
                        }
                        if (doesNotContainNamedParameters(line)) {// 排除日期格式字符串
                            sqlBuffer.append(" ").append(realLine).append(" ");
                        }
                    }
                }
                continue;
            }
            if (line.startsWith("/*")) {
                multyComment = true;
                continue;
            }
            if (multyComment) {
                if (line.endsWith("*/")) {
                    multyComment = false;
                }
                continue;
            }
            sqlBuffer.append(line).append(" ");
        }
        return sqlBuffer.toString();
    }

    public static void main(String[] args) {
        String sql;
        sql = "xxx=:xxxand xxx=:xxx and xxx=:xxx, and xxx=:xxx2 adn xxx in (:xxx) time='12:12:12' xxx=:xxx";
        String _key = ":xxx";
        sql = sql.replaceAll(_key.concat(" "), " ");
        sql = sql.replaceAll(_key.concat("\\)"), ")");
        sql = sql.replaceAll(_key.concat("\\|"), "|");
        sql = sql.replaceAll(_key.concat("\\,"), ",");
        sql = sql.replaceAll(_key.concat("$"), "");
        System.out.println(sql);
        System.out.println(doesNotContainNamedParameters("id=:xxx"));
        System.out.println(doesNotContainNamedParameters("xx=1 and id= time='12:12:12' "));
        System.out.println(doesNotContainNamedParameters("xxx in (xx) time='12:12:12'"));
        System.out.println(doesNotContainNamedParameters("xxx in (:xxx) time='hh:mm:ss'"));
    }

    public static boolean doesNotContainNamedParameters(String sql) {
        boolean inQuotes = false;
        int colonCount = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '\'') {
                // 切换引号状态
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == ':') {
                    colonCount++;
                } else if (c == '{' && i < sql.length() - 1 && sql.charAt(i + 1) == ':') {
                    // 排除日期格式化字符串中的冒号
                    colonCount--;
                }
            }
        }
        // 如果冒号数量为0，则认为没有命名参数
        return colonCount == 0;
    }

}
