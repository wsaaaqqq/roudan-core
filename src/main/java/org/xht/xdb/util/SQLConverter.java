package org.xht.xdb.util;

import org.xht.xdb.vo.SqlArgVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SQLConverter {
    public static SqlArgVo formatArgsWithoutNamedParam(String sql, Map<String, Object> args) {
        List<Object> parameters = new ArrayList<>();
        StringBuilder convertedSQL = new StringBuilder(sql);

        int paramIndex = 1;
        while (paramIndex != -1) {
            int paramNameStart = convertedSQL.indexOf(":", paramIndex);
            if (paramNameStart == -1) {
                break;
            }
            int paramNameEnd = IntStream.of(
                    convertedSQL.indexOf(" ", paramNameStart),
                    convertedSQL.indexOf(")", paramNameStart),
                    convertedSQL.indexOf(",", paramNameStart),
                    convertedSQL.indexOf("\n", paramNameStart)
            ).filter(e -> e != -1).min().orElse(-1);

            if (paramNameEnd == -1) {
                paramNameEnd = convertedSQL.length();
            }

            String paramName = convertedSQL.substring(paramNameStart + 1, paramNameEnd);
            if (args.containsKey(paramName)) {
                Object paramValue = args.get(paramName);
                // 替换命名参数为?占位符
                convertedSQL.replace(paramNameStart, paramNameEnd, "?");
                // 添加参数值到参数列表
                parameters.add(paramValue);
            }
            paramIndex = paramNameStart + 1;
        }

        return new SqlArgVo(convertedSQL.toString(), parameters.toArray());
    }

    public static void main(String[] args1) {
        String sql = "SELECT * FROM users WHERE id=':minAge ' age > :minAge AND age < :maxAge AND name = :minAge and xx >:maxAge";
        Map<String, Object> args = new HashMap<>();
        args.put("minAge", 20);
        args.put("maxAge", 30);

        SqlArgVo sqlVO = formatArgsWithoutNamedParam(sql, args);
        String convertedSQL = sqlVO.getSql();
        Object[] parameters = sqlVO.getParameters();

        System.out.println("Converted SQL: " + convertedSQL);
        System.out.println("Parameters: ");
        for (Object param : parameters) {
            System.out.println(param);
        }
    }
}
