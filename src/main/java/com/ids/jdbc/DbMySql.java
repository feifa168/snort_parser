package com.ids.jdbc;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbMySql extends AbstractDaoBase {
    public static final String defaultDriver = "com.mysql.jdbc.Driver";//"com.mysql.cj.jdbc.Driver";//"com.mysql.jdbc.Driver";
    private Connection conn = null;

    public DbMySql(String dburl, String user, String passwd)  throws ClassNotFoundException, SQLException {
        super(defaultDriver, dburl, user, passwd);
        conn = getConnection();
    }

    public <T> List<T> executeQuery(String sql, Class<T> clazz) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            List<T> listAlerts = new ArrayList<T>();
            T t = null;
            while (rs.next()) {
                try {
                    t = clazz.newInstance();
                    String columnName;
                    for (int i=0; i<columnCount; i++) {
                        columnName = rsmd.getColumnName(i+1);
                        try {
                            Object o = rs.getObject(columnName);
                            if (rs.wasNull()) {
                                o = "null";
                            }
                            BeanUtils.copyProperty(t, columnName, o);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                    listAlerts.add(t);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            return listAlerts;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class ColumnNameValue {
        private String name;
        private Object value;

        public ColumnNameValue() {}
        public ColumnNameValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public Object getValue() { return value; }
        public void setName(String name) { this.name = name; }
        public void setValue(Object value) { this.value = value; }
    }
    public <T> int executeInsert(T t, String tableName) {
        List<ColumnNameValue> listCnv = new ArrayList<>();
        Field[] fls = t.getClass().getDeclaredFields();
        for (Field f : fls) {
            f.setAccessible(true);
            try {
                String name = f.getName();
                Object value = f.get(t);
                listCnv.add(new ColumnNameValue(name, value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" (");
        StringBuilder columns   = new StringBuilder();
        StringBuilder values    = new StringBuilder();
        final int listSize = listCnv.size();
        for (int i=1; i<listSize; i++) {
            ColumnNameValue cnv = listCnv.get(i);
            columns.append(cnv.getName());
            if (cnv.value instanceof String)
                values.append("\'").append(cnv.getValue()).append("\'");
            else
                values.append(cnv.getValue());
            if (i+1 != listSize) {
                columns.append(", ");
                values.append(", ");
            }
        }
        columns.append(")");
        values.append(")");
        sql.append(columns).append(" values(").append(values);

        try {
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(sql.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
