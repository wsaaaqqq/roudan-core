package org.xht.xdb.function;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
