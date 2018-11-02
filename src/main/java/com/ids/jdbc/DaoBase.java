package com.ids.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface DaoBase {
    Connection getConnection();
    void close() throws SQLException;
}
