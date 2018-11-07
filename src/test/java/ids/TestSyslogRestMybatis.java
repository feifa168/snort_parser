package ids;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import com.ids.jdbc.DbMySql;
import com.ids.rest.RestServer;
import com.ids.syslog.IdsSyslogParser;
import com.ids.syslog.SyslogQueue;
import com.ids.syslog.SyslogServer;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TestSyslogRestMybatis {

    public SqlSession sqlSession = null;

    public void init() throws IOException {
        // mybatis配置文件，这个地方的root地址为：resources，路径要对。
        String resource = "mybatis-config.xml";
        // 得到配置文件流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        // 创建会话工厂，传入mybatis的配置文件信息
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 通过工厂得到SqlSession
        //sqlSessionFactory.getConfiguration().addMapper(IdsAlertInterface.class);
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void testLong2DateTime() {
        Calendar c=Calendar.getInstance();
        int seconds = 1541150743;//数据库中提取的数据
        long millions=new Long(seconds).longValue()*1000;
        c.setTimeInMillis(millions);
        System.out.println(""+c.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sdf.format(c.getTime());
        System.out.println(dateString);
    }

    @Test
    public void testRest() {
        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.shutdown();
    }

    @Test
    public void testSyslogServerQueueMybatisMysql() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue<>();

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });

        Thread threadParseLog = new Thread(()->{
            try {
                this.init();
            } catch (IOException e) {
                e.printStackTrace();
            }

            IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);

            while(true) {
                if (!queue.isEmpty()) {
                    IdsSyslogParser parser = (IdsSyslogParser)queue.poll();
                    if (parser != null) {
                        System.out.println("parser is " + parser.toString());

                        IdsAlert alert = new IdsAlert();//( 5,"2018-10-29 10:25:43", 139, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
                        alert = parser.getIdsAlert();
                        dao.putIdsAlert(alert);
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        threadLogServer.start();
        threadParseLog.start();

        try {
            threadLogServer.join();
            threadParseLog.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSyslogServerQueueMybatisMysqlRest() {

        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));

        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue<>();
        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });

        Thread threadParseLog = new Thread(()->{
            try {
                this.init();
            } catch (IOException e) {
                e.printStackTrace();
            }

            IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);

            while(true) {
                if (!queue.isEmpty()) {
                    IdsSyslogParser parser = (IdsSyslogParser)queue.poll();
                    if (parser != null) {
                        System.out.println("parser is " + parser.toString());

                        IdsAlert alert = new IdsAlert();//( 5,"2018-10-29 10:25:43", 139, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
                        alert = parser.getIdsAlert();
                        dao.putIdsAlert(alert);
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        threadLogServer.start();
        threadParseLog.start();

        try {
            threadLogServer.join();
            threadParseLog.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
