package com.ids;

import com.ids.dao.IdsAlertInterface;
import com.ids.db.SqlSessionBuild;
import com.ids.rest.RestServer;
import com.ids.syslog.SyslogServer;
import com.ids.syslog.SyslogThreadPoolExecutor;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import org.apache.ibatis.session.SqlSession;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {

        ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
        List<Thread> threads = new ArrayList();

        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));

        try {
            boolean isrun = true;

            SqlSession sqlSession = SqlSessionBuild.createSqlSession("mybatis-config.xml");
            IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
            Thread threadLogServer = new Thread(()->{
                SyslogServer logServer = new SyslogServer(514, pool, dao);
                logServer.start();
            });
            threads.add(threadLogServer);

            for(Thread t : threads) {
                t.start();
            }

            try {
                byte[] buf = new byte[32];
                System.in.read(buf, 0, buf.length);
                while (true) {
                    if (buf.equals("exit")) {
                        isrun = false;
                        break;
                    }

                    System.in.read(buf, 0, buf.length);
                }

                for(Thread t : threads) {
                    t.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
