package org.xht.xdb.sql.statement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * This class wraps around a {@link PreparedStatement} and allows the programmer to set parameters by name instead
 * of by index. This eliminates any confusion as to which parameter index represents what. This also means that
 * rearranging the SQL statement or adding a parameter doesn't involve renumbering your indices.
 * Code such as this:
 *
 * <pre><code>
 * Connection conn = getConnection();
 * String sql = "select * from my_table where name=? or address=?";
 * PreparedStatement p = conn.prepareStatement(sql);
 * p.setString(1, "bob");
 * p.setString(2, "123");
 * ResultSet rs = p.executeQuery();
 * </code></pre>
 *
 * Can be replaced with:
 *
 * <pre><code>
 * Connection conn = getConnection();
 * String sql = "select * from my_table where name=:name or address=:address";
 * NamedParameterStatement p = new NamedParameterStatement(conn, sql);
 * p.setString("name", "bob");
 * p.setString("address", "123");
 * ResultSet rs = p.executeQuery();
 * </code></pre>
 */
@SuppressWarnings("unused")
public class NamedParameterStatement extends PreparedStatementWrapper {
    private static final HashMap<String, Map<String, List<Integer>>> nameIndexCache = new HashMap<>();
    private static final HashMap<String, String> parsedSqlCache = new HashMap<>();

    private final Map<String, List<Integer>> nameIndexMap;

    public static void setObject(PreparedStatement s, int parameterIndex, Object value, int targetSqlType, int scaleOrLength) throws SQLException {
        if (value == null) {
            s.setNull(parameterIndex, targetSqlType);
        } else {
            s.setObject(parameterIndex, value, targetSqlType, scaleOrLength);
        }
    }

    public static void setObject(PreparedStatement s, int parameterIndex, Object value, int targetSqlType) throws SQLException {
        if (value == null) {
            s.setNull(parameterIndex, targetSqlType);
        } else {
            s.setObject(parameterIndex, value, targetSqlType);
        }
    }

    public static void setObject(PreparedStatement s, int parameterIndex, Object value)
            throws SQLException {
        if (value == null) {
            s.setNull(parameterIndex, Types.NULL);
        } else {
            s.setObject(parameterIndex, value);
        }
    }

    public static void setObject(NamedParameterStatement s, String key, Object value)
            throws SQLException {
        if (value == null) {
            s.setNull(key, Types.NULL);
        } else {
            s.setObject(key, value);
        }
    }

    /**
     * Creates a NamedParameterStatement. Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
     * @param conn the database connection
     * @param sql      the parameterized sql
     * @throws SQLException if the statement could not be created
     */
    public NamedParameterStatement(Connection conn, String sql) throws SQLException {
        String parsedSql;
        if (nameIndexCache.containsKey(sql)) {
            nameIndexMap = nameIndexCache.get(sql);
            parsedSql = parsedSqlCache.get(sql);
        } else {
            nameIndexMap = new HashMap<>();
            parsedSql = parseNamedSql(sql, nameIndexMap);
            nameIndexCache.put(sql, nameIndexMap);
            parsedSqlCache.put(sql, parsedSql);
        }
        //noinspection SqlSourceToSinkFlow
        s = conn.prepareStatement(parsedSql);
    }

    /**
     * Returns the indexes for a parameter.
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private List<Integer> getIndexes(String name) {
        List<Integer> indexes = nameIndexMap.get(name);
        if (indexes == null) {
            throw new IllegalArgumentException("Parameter not found: " + name);
        }
        return indexes;
    }

    /**
     * Parses a sql with named parameters. The parameter-index mappings
     * are put into the map, and the parsed sql is returned.
     * @param sql    sql with named parameters
     * @return the parsed sql
     */
    private static String parseNamedSql(String sql, Map<String, List<Integer>> nameIndexMap) {
        int length = sql.length();
        StringBuilder parsedSql = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;
        for (int i = 0; i < length; i++) {
            char c = sql.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < length && Character.isJavaIdentifierPart(sql.charAt(j))) {
                        j++;
                    }
                    String name = sql.substring(i + 1, j);
                    c = '?'; // replace the parameter with a question mark
                    i += name.length(); // skip past the end if the parameter
                    List<Integer> indexList = nameIndexMap.computeIfAbsent(name, k -> new LinkedList<>());
                    indexList.add(index);
                    index++;
                }
            }
            parsedSql.append(c);
        }
        return parsedSql.toString();
    }

    public void setArray(String name, Array value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setArray(index, value);
        }
    }

    public void setAsciiStream(String name, InputStream value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setAsciiStream(index, value);
        }
    }

    public void setAsciiStream(String name, InputStream value, int length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setAsciiStream(index, value, length);
        }
    }

    public void setBigDecimal(String name, BigDecimal value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBigDecimal(index, value);
        }
    }

    public void setBinaryStream(String name, InputStream value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBinaryStream(index, value);
        }
    }

    public void setBinaryStream(String name, InputStream value, int length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBinaryStream(index, value, length);
        }
    }

    public void setBinaryStream(String name, InputStream value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBinaryStream(index, value, length);
        }
    }

    public void setBlob(String name, Blob value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBlob(index, value);
        }
    }

    public void setBlob(String name, InputStream value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBlob(index, value);
        }
    }

    public void setBlob(String name, InputStream value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBlob(index, value, length);
        }
    }

    public void setBoolean(String name, boolean value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBoolean(index, value);
        }
    }

    public void setByte(String name, byte value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setByte(index, value);
        }
    }

    public void setBytes(String name, byte[] value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setBytes(index, value);
        }
    }

    public void setCharacterStream(String name, Reader value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setCharacterStream(index, value);
        }
    }

    public void setCharacterStream(String name, Reader value, int length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setCharacterStream(index, value, length);
        }
    }

    public void setCharacterStream(String name, Reader value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setCharacterStream(index, value, length);
        }
    }

    public void setClob(String name, Clob value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setClob(index, value);
        }
    }

    public void setClob(String name, Reader value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setClob(index, value);
        }
    }

    public void setClob(String name, Reader value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setClob(index, value, length);
        }
    }

    public void setDate(String name, Date value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setDate(index, value);
        }
    }

    public void setDate(String name, Date value, Calendar cal) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setDate(index, value, cal);
        }
    }

    public void setDouble(String name, double value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setDouble(index, value);
        }
    }

    public void setFloat(String name, float value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setFloat(index, value);
        }
    }

    public void setInt(String name, int value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setInt(index, value);
        }
    }

    public void setLong(String name, long value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setLong(index, value);
        }
    }

    public void setNCharacterStream(String name, Reader value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNCharacterStream(index, value);
        }
    }

    public void setNCharacterStream(String name, Reader value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNCharacterStream(index, value, length);
        }
    }

    public void setNClob(String name, NClob value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNClob(index, value);
        }
    }

    public void setNClob(String name, Reader value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNClob(index, value);
        }
    }

    public void setNClob(String name, Reader value, long length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNClob(index, value, length);
        }
    }

    public void setNString(String name, String value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNString(index, value);
        }
    }

    public void setNull(String name, int sqlType) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setNull(index, sqlType);
        }
    }

    public void setObject(String name, Object value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            if (value instanceof java.sql.Blob) {
                s.setBlob(index, (java.sql.Blob) value);
            } else {
                setObject(s, index, value);
            }
        }
    }

    public void setObject(String name, Object value, int targetSqlType) throws SQLException {
        for (Integer index : getIndexes(name)) {
            setObject(s, index, value, targetSqlType);
        }
    }

    public void setObject(String name, Object value, int targetSqlType, int scaleOrLength) throws SQLException {
        for (Integer index : getIndexes(name)) {
            setObject(s, index, value, targetSqlType, scaleOrLength);
        }
    }

    public void setRef(String name, Ref value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setRef(index, value);
        }
    }

    public void setRowId(String name, RowId value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setRowId(index, value);
        }
    }

    public void setShort(String name, short value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setShort(index, value);
        }
    }

    public void setSQLXML(String name, SQLXML value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setSQLXML(index, value);
        }
    }

    public void setString(String name, String value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setString(index, value);
        }
    }

    public void setTime(String name, Time value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setTime(index, value);
        }
    }

    public void setTime(String name, Time value, Calendar cal) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setTime(index, value, cal);
        }
    }

    public void setTimestamp(String name, Timestamp value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setTimestamp(index, value);
        }
    }

    public void setTimestamp(String name, Timestamp value, Calendar cal) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setTimestamp(index, value, cal);
        }
    }

    @SuppressWarnings("deprecation")
    public void setUnicodeStream(String name, InputStream value, int length) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setUnicodeStream(index, value, length);
        }
    }

    public void setURL(String name, URL value) throws SQLException {
        for (Integer index : getIndexes(name)) {
            s.setURL(index, value);
        }
    }

}
