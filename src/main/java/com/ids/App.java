package com.ids;

import com.ids.dao.IdsAlertInterface;
import com.ids.db.SqlSessionBuild;
import com.ids.debug.DebugInformation;
import com.ids.rest.RestServer;
import com.ids.syslog.SyslogServer;
import com.ids.syslog.SyslogThreadPoolExecutor;
import org.apache.ibatis.session.SqlSession;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) {

        ThreadPoolExecutor parseSyslogPool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
        List<Thread> threads = new ArrayList();

        final HttpServer restServer = RestServer.startServer();
        System.out.println(String.format("restful server started at %s/ids/index \nHit enter to stop it...", RestServer.BASE_URI));

        class ThreadLoopExit {
            public AtomicBoolean isRun = new AtomicBoolean(true);
        }
        ThreadLoopExit loopExit = new ThreadLoopExit();

        try {
            SqlSession sqlSession = SqlSessionBuild.createSqlSession("mybatis-config.xml");
            IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
            SyslogServer logServer = new SyslogServer(514, parseSyslogPool, dao);
            Thread threadLogServer = new Thread(()->{
                String name = Thread.currentThread().getName();
                System.out.println(name + "[" + Thread.currentThread().getId()+"] is running...");
                logServer.start();
                System.out.println(name + " is exiting...");
            });
            threads.add(threadLogServer);

            Thread threadWaitForExit = new Thread(()->{
                String name = Thread.currentThread().getName();
                System.out.println(name + "[" + Thread.currentThread().getId()+"] is running...");
                while (true) {
                    if (!loopExit.isRun.get()) {

                        restServer.shutdown();
                        logServer.stop();

                        // 线程池和数据库要等syslog处理模块结束后才关闭
                        while (logServer.isRunnable()) {
                            try {
                                //System.out.println("wait for log server end");
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("syslog server is terminated");

                        // 关闭并等待线程池结束
                        parseSyslogPool.shutdown();
                        // 使用awaitTermination或isTerminated判断线程池是否结束
                        // parseSyslogPool.awaitTermination(10, TimeUnit.SECONDS);
                        while (true) {
                            if (parseSyslogPool.isTerminated()) {
                                break;
                            }
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("threadpool is terminated");

                        // 关闭数据库
                        sqlSession.close();

                        break;
                    }
                }
                System.out.println(name + " is exiting...");
            });
            threads.add(threadWaitForExit);

            for(Thread t : threads) {
                t.start();
            }

            byte[] buf = new byte[32];
            while (true) {
                System.out.println(
                        "please enter exit to quit"
                        + "\n\tinput display to display log"
                        + "\n\tinput undisplay to end display log"
                );
                try {
                    System.in.read(buf, 0, buf.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String input = new String(buf);
                if (input.startsWith("exit")) {
                    boolean isExit = loopExit.isRun.compareAndSet(loopExit.isRun.get(), false);
                    System.out.println("set exit flag is "+(isExit ? "successed" : "failed")+". please wait for a moment...");
                    if (isExit) {
                        break;
                    }
                } else if (input.startsWith("display")) {
                    DebugInformation.ifDisplayMsg.set(true);

                } else if (input.startsWith("undisplay")) {
                    DebugInformation.ifDisplayMsg.set(false);
                }
            }

            try {
                for(Thread t : threads) {
                    t.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("exit app");
    }
}
