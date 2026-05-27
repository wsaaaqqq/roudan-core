package org.xht.xdb.function;

import java.sql.SQLException;
import java.util.List;

public interface ResultSetRowHandler {
    void handle(List<Object[]> resultSet) throws SQLException;
}
