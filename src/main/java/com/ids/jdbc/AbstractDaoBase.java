package com.ids.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractDaoBase implements DaoBase {
    private String dbdriver;
    private String dburl;
    private String user;
    private String passwd;
    private Connection conn = null;
    private boolean isvalid = false;
    private PreparedStatement pstmt = null;

    public AbstractDaoBase(String dbdriver, String dburl, String user, String passwd) throws ClassNotFoundException, SQLException {
        this.dbdriver = dbdriver;
        this.dburl = dburl;
        this.user = user;
        this.passwd = passwd;

        init();
        connection();
    }

    private void init() throws ClassNotFoundException{
        Class.forName(dbdriver);
    }

    private void connection() throws SQLException {
        conn = DriverManager.getConnection(dburl, user, passwd);
        isvalid = true;
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}
