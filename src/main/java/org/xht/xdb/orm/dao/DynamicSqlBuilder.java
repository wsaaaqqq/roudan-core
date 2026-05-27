package org.xht.xdb.orm.dao;

import org.xht.xdb.orm.dao.vo.MethodField;
import org.xht.xdb.orm.dao.vo.MethodParseResult;
import org.xht.xdb.orm.util.OrmAnnoUtil;
import org.xht.xdb.sql.SqlTool;
import org.xht.xdb.util.MapUtil;

import java.lang.reflect.Array;
import java.util.List;

/**
 * 动态SQL构建器
 */
public class DynamicSqlBuilder {
    private final Class<?> entityClass;

    public <T> DynamicSqlBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 根据方法解析结果构建查询SQL
     */
    public SqlTool build(MethodParseResult parseResult, Object[] args) {
        String tableName = OrmAnnoUtil.getTableNameByBeanClass(entityClass);
        StringBuilder sql = new StringBuilder();
        MapUtil<Object> sqlArgs = MapUtil.init(Object.class);
        String operation = parseResult.getOperation().toLowerCase();
        List<MethodField> fields = parseResult.getFields();

        // 根据操作类型构建SQL
        switch (operation) {
            case "find":
            case "read":
            case "get":
                sql.append("SELECT * FROM ").append(tableName);
                break;
            case "count":
                sql.append("SELECT COUNT(1) FROM ").append(tableName);
                break;
            case "exists":
                sql.append("SELECT 1 FROM ").append(tableName);
                break;
            case "delete":
                sql.append("DELETE FROM ").append(tableName);
                break;
            default:
                sql.append("SELECT * FROM ").append(tableName);
                break;
        }

        addWhere(args, fields, sql, sqlArgs);

        // 如果是exists操作，需要添加LIMIT 1以提高性能
        if ("exists".equals(operation)) {
            sql.append(" LIMIT 1");
        }
        SqlTool sqlTool = new SqlTool();
        sqlTool.sql(sql.toString());
        sqlTool.sqlArgs(sqlArgs);
        return sqlTool;
    }

    private void addWhere(Object[] args, List<MethodField> fields, StringBuilder sql, MapUtil<Object> sqlArgs) {
        if (!fields.isEmpty()) {
            sql.append(" WHERE ");
            String ARG_NAME = "ARG_";
            for (int i = 0; i < fields.size(); i++) {
                MethodField field = fields.get(i);
                String condition = field.getCondition().toLowerCase();

                String columnName = OrmAnnoUtil.getColNameByBeanClass(entityClass, field.getFieldName());
                sql.append(columnName).append(" ");

                Object value = MethodNamingParser.formatValue(field.getCondition(), args[i]);
                switch (condition) {
                    case "ne":
                        sql.append(" != :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "gt":
                        sql.append(" > :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "lt":
                        sql.append(" < :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "gte":
                        sql.append(" >= :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "lte":
                        sql.append(" <= :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "in":
                        sql.append(" IN ( :").append(ARG_NAME).append(i).append(" ) ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "between":
                        sql
                                .append(" BETWEEN :")
                                .append(ARG_NAME)
                                .append(i)
                                .append("_0  AND :")
                                .append(ARG_NAME)
                                .append(i)
                                .append("_1 ");
                        if (value.getClass().isArray()) {
                            if (Array.getLength(value) < 2) {
                                throw new RuntimeException("between args length < 2 ");
                            }
                            sqlArgs.add(ARG_NAME + i + "_0", Array.get(value, 0));
                            sqlArgs.add(ARG_NAME + i + "_1", Array.get(value, 1));
                        } else if (value instanceof List) {
                            //noinspection rawtypes
                            List _value = (List) value;
                            if (_value.size() < 2) {
                                throw new RuntimeException("between args length < 2 ");
                            }
                            sqlArgs.add(ARG_NAME + i + "_0", _value.get(0));
                            sqlArgs.add(ARG_NAME + i + "_1", _value.get(1));
                        } else {
                            throw new RuntimeException("between args is not List or Array");
                        }
                        break;
                    case "start_with":
                        sql.append(" LIKE :").append(ARG_NAME).append(i).append("||'%' ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "end_with":
                        sql.append(" LIKE '%'|| :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    case "like":
                    case "contain":
                        sql.append(" LIKE '%'|| :").append(ARG_NAME).append(i).append("||'%' ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                    default:
                        sql.append(" = :").append(ARG_NAME).append(i).append(" ");
                        sqlArgs.add(ARG_NAME + i, value);
                        break;
                }

                // 添加连接符
                if (i < fields.size() - 1) {
                    sql.append(" ").append(field.getNextConnector()).append(" ");
                }
            }
        }
    }

}
