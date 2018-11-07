package ids;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import com.ids.rest.RestServer;
import com.ids.syslog.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class TestThreadPool {

    class MyTask implements Runnable {
        private String name;
        MyTask(String name) { this.name = name; }
        public String getName() { return name; }
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ", " + name);
        }
    }

    @Test
    public void testThreadPool() {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int corePoolSize = 2;//processors + 1;
        final int maximumPoolSize = 3;//corePoolSize*2;
        final int queueSize = 5;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy()) {

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                System.out.println("准备执行：" + ((MyTask) r).getName());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                System.out.println("执行完成： " + ((MyTask) r).getName());
            }
            @Override
            protected void terminated() {
                System.out.println("线程池退出");
            }

        };

        for (int i=0; i<20; i++) {
            pool.execute(new MyTask("task"+(i+1)));
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();
    }


    class SyslogQueue2<T> extends ConcurrentLinkedDeque<T> {
        public SyslogQueue2() {
            super();
        }
    }

    public static boolean isrun = true;
    @Test
    public void testSyslogThreadPoolExecutor() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue();
        Thread threadPut = new Thread(()->{
            int i = 1;
            while(isrun) {
                IdsSyslogParser parser = new IdsSyslogParser();
                parser.parser("<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22");
                queue.add(parser);
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            System.out.println("input size is "+queue.size());
        });
        threadPut.start();

        Thread threadGet2 = new Thread(()->{
            ThreadPoolExecutor pool = new SyslogThreadPoolExecutor(1, 1,10, TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<Runnable>(2),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.DiscardPolicy());
            int i = 1;
            while(isrun) {
                if (!queue.isEmpty())
                    pool.execute(new SyslogTaskImpl<SyslogQueue<IdsSyslogParser>, IdsSyslogParser >(queue, "task "+(i++)));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pool.shutdown();
        });
        threadGet2.start();

        try {
            Thread.sleep(4000);
            isrun = false;

            threadPut.join();
            threadGet2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSyslogThreadPoolExecutorSyslogServer() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue();
        List<Thread> threads = new ArrayList(3);

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });
        threads.add(threadLogServer);

        Thread threadParseLog = new Thread(()->{
            while(true) {
                if (!queue.isEmpty()) {
                    IdsSyslogParser log = (IdsSyslogParser)queue.poll();
                    if (log != null) {
                        System.out.println("parser is " + log.toString());
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
        threads.add(threadParseLog);

        Thread threadGet = new Thread(()->{
            ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
            int i = 1;
            while(isrun) {
                pool.execute(new SyslogTaskImpl<SyslogQueue<IdsSyslogParser>, IdsSyslogParser >(queue, "task "+(i++)));
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pool.shutdown();
        });
        threads.add(threadGet);

        for(Thread t : threads) {
            t.start();
        }

        try {
            Thread.sleep(4000);
            isrun = false;

            for(Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
    public void testSyslogThreadPoolExecutorSyslogServerMybatis() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue();
        List<Thread> threads = new ArrayList(4);

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });
        threads.add(threadLogServer);

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
        threads.add(threadParseLog);

        Thread threadGet = new Thread(()->{
            ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
            int i = 1;
            while(isrun) {
                pool.execute(new SyslogTaskImpl<SyslogQueue<IdsSyslogParser>, IdsSyslogParser >(queue, "task "+(i++)));
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pool.shutdown();
        });
        threads.add(threadGet);

        for(Thread t : threads) {
            t.start();
        }

        try {
            Thread.sleep(4000);
            isrun = false;

            for(Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testSyslogThreadPoolExecutorSyslogServerMybatisRest() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue();
        List<Thread> threads = new ArrayList(4);

        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });
        threads.add(threadLogServer);

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
        threads.add(threadParseLog);

        Thread threadGet = new Thread(()->{
            ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
            int i = 1;
            while(isrun) {
                pool.execute(new SyslogTaskImpl<SyslogQueue<IdsSyslogParser>, IdsSyslogParser >(queue, "task "+(i++)));
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            pool.shutdown();
        });
        threads.add(threadGet);

        for(Thread t : threads) {
            t.start();
        }

        try {
            Thread.sleep(4000);
            isrun = false;

            for(Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
