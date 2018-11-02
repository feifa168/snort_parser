package ids;

import com.ids.beans.IdsAlert;
import com.ids.jdbc.DbMySql;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

public class TestDao {

    public static DbMySql mysql = null;
    public static class Inner2{}

    @BeforeClass
    public static void testBeforeClass(){
        System.out.println("before Class");
        try {
            mysql = new DbMySql("jdbc:mysql://172.16.39.251:12239/ids", "snort", "123456");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @After
    public void afterClass(){
        System.out.println("after Class");
        try {
            mysql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIdsAlert() {
        try {
            Class<?> cls = Class.forName("com.ids.beans.IdsAlert");
            Field[] fls = cls.getDeclaredFields();
            Object obj = null;
            try {
                obj = cls.newInstance();
                for (Field f : fls) {
                    f.setAccessible(true);
                    try {
                        Object fv = f.get(obj);
                        String value = fv != null ? fv.toString() : "null";
                        System.out.print(f.getName()+"="+value.toString()+",");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("");
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testMysqlQuery(){
        List<IdsAlert> alerts = mysql.executeQuery("select * from alert", IdsAlert.class);
        for (IdsAlert ats : alerts) {
            Field[] fls = IdsAlert.class.getDeclaredFields();
            for (Field f : fls) {
                f.setAccessible(true);
                try {
                    System.out.print(f.getName()+"="+f.get(ats).toString()+",");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("");
        }
    }

    @Test
    public void testMysqlInsert(){
        IdsAlert ids = new IdsAlert( 8,"2018-11-02 10:25:43", 139, "172.16.5.37",
                "snort", 4, 8, 2, "this is for test", 3, "TCP",
                "sip", 30, false, "dip", 555);
        mysql.executeInsert(ids, "alert");
        //ids = new IdsAlert( 8,"Nov  1 09:49:13", 139, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
        //mysql.executeInsert(ids, "alert");
    }
}
