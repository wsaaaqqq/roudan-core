package org.xht.xdb.orm.dao;

import lombok.extern.slf4j.Slf4j;
import org.xht.xdb.orm.dao.vo.MethodField;
import org.xht.xdb.orm.dao.vo.MethodParseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 方法命名解析器，支持下划线命名风格
 * 例如: find_by_id_name, find_by_username_and_email, count_by_username, exists_by_username
 */
@Slf4j
public class MethodNamingParser {

    // 解析方法名的正则表达式
    private static final Pattern METHOD_PATTERN =
            Pattern.compile("^(find|read|get|count|exists|delete|save|update)_by_(.+)$");

    private static final Pattern AND_OR_PATTERN = Pattern.compile("(_and_|_or_)");

    /**
     * 解析方法名，返回操作类型和字段列表
     */
    public static MethodParseResult parseMethodName(String methodName) {
        Matcher matcher = METHOD_PATTERN.matcher(methodName);
        if (!matcher.matches()) {
            return null;
        }

        String operation = matcher.group(1);
        String fieldPart = matcher.group(2);

        // 解析字段名和操作符
        List<MethodField> fields = parseFields(fieldPart);

        return new MethodParseResult(operation, fields);
    }

    /**
     * 解析字段部分，提取字段名和操作符
     */
    private static List<MethodField> parseFields(String fieldPart) {
        List<MethodField> fields = new ArrayList<>();

        // 按照 _and_ 或 _or_ 分割字段
        String[] fieldConditions = AND_OR_PATTERN.split(fieldPart);

        for (String fieldCondition : fieldConditions) {
            // 解析字段名和条件操作符 (如: like, in, between, gt, lt 等)
            String fieldName = fieldCondition;
            String condition = "eq"; // 默认相等操作

            // 检查是否有特殊条件操作符
            if (fieldCondition.contains("_like")) {
                fieldName = fieldCondition.replace("_like", "");
                condition = "like";
            } else if (fieldCondition.contains("_in")) {
                fieldName = fieldCondition.replace("_in", "");
                condition = "in";
            } else if (fieldCondition.contains("_between")) {
                fieldName = fieldCondition.replace("_between", "");
                condition = "between";
            } else if (fieldCondition.contains("_gte")) {
                fieldName = fieldCondition.replace("_gte", "");
                condition = "gte";
            } else if (fieldCondition.contains("_gt")) {
                fieldName = fieldCondition.replace("_gt", "");
                condition = "gt";
            } else if (fieldCondition.contains("_lte")) {
                fieldName = fieldCondition.replace("_lte", "");
                condition = "lte";
            } else if (fieldCondition.contains("_lt")) {
                fieldName = fieldCondition.replace("_lt", "");
                condition = "lt";
            } else if (fieldCondition.contains("_not") || fieldCondition.contains("_ne")) {
                fieldName = fieldCondition.replace("_not", "");
                condition = "ne";
            } else if (fieldCondition.contains("_start_with")) {
                fieldName = fieldCondition.replace("_start_with", "");
                condition = "start_with";
            } else if (fieldCondition.contains("_end_with")) {
                fieldName = fieldCondition.replace("_end_with", "");
                condition = "end_with";
            } else if (fieldCondition.contains("_contain")) {
                fieldName = fieldCondition.replace("_contain", "");
                condition = "contain";
            }

            fields.add(new MethodField(fieldName, condition));
        }

        // 提取连接符 (AND 或 OR)
        List<String> connectors = new ArrayList<>();
        Matcher connectorMatcher = AND_OR_PATTERN.matcher(fieldPart);
        while (connectorMatcher.find()) {
            String connector = connectorMatcher.group(1).replace("_", "").toUpperCase();
            connectors.add(connector);
        }

        // 设置字段之间的连接符
        for (int i = 0; i < fields.size() - 1; i++) {
            if (i < connectors.size()) {
                fields.get(i).setNextConnector(connectors.get(i));
            } else {
                fields.get(i).setNextConnector("AND");
            }
        }

        return fields;
    }

    /**
     * 判断方法名是否支持命名约定
     */
    public static boolean isSupportedMethod(String methodName) {
        return METHOD_PATTERN.matcher(methodName).matches();
    }

    /**
     * 根据条件类型生成参数值
     */
    public static Object formatValue(String condition, Object value) {
        if (value == null) {
            return null;
        }

        switch (condition.toLowerCase()) {
            case "start_with":
                return value + "%";
            case "end_with":
                return "%" + value;
            case "contain":
                return "%" + value + "%";
            default:
                return value;
        }
    }
}


