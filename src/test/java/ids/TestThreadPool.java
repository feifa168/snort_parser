package ids;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import com.ids.debug.DebugInformation;
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
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    private boolean initSyslogServer() {
        // syslog解析用到的配置
        if (!SyslogConfig.parse("syslog.xml")) {
            System.out.println("syslog.xml格式不正确，请查证");
            return false;
        }

        DebugInformation.ifDisplayMsg.set(true);
        return true;
    }

    @Test
    public void testThreadPoolMe() {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int corePoolSize = 2;//processors + 1;
        final int maximumPoolSize = 3;//corePoolSize*2;
        final int queueSize = 100;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize));

        for (int i=0; i<200; i++) {
            pool.execute(new MyTask("task"+(i+1)));
            System.out.println("        pool execute " + (i+1));
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        try {
//            pool.awaitTermination(10, TimeUnit.SECONDS);//.shutdown();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        while (true) {
            int activCount = pool.getActiveCount();
            int taskCount = pool.getQueue().size();
            boolean is = pool.isTerminated();
            System.out.println(pool.toString());
            if (activCount==0 && taskCount==0) {
                pool.shutdown();
                break;
            } else {
                System.out.println(pool.toString());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("shutdown pool===========================================================");
    }

    @Test
    public void testThreadPool() {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int corePoolSize = 2;//processors + 1;
        final int maximumPoolSize = 3;//corePoolSize*2;
        final int queueSize = 50;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy()) {

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                System.out.println("准备执行：" + ((MyTask) r).getName());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                System.out.println("执行完成： " + ((MyTask) r).getName());
                System.out.println(this.toString());
            }
            @Override
            protected void terminated() {
                System.out.println("线程池退出");
                System.out.println(this.toString());
            }

        };

        for (int i=0; i<50; i++) {
            pool.execute(new MyTask("task"+(i+1)));
            System.out.println("        pool execute " + (i+1));
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        try {
//            pool.awaitTermination(10, TimeUnit.SECONDS);//.shutdown();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        while (true) {
            int activCount = pool.getActiveCount();
            int taskCount = pool.getQueue().size();
            boolean is = pool.isTerminated();
            System.out.println(pool.toString());
            if (activCount==0 && taskCount==0) {
                pool.shutdown();
                break;
            } else {
                System.out.println(pool.toString());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("shutdown pool===========================================================");
    }

    @Test
    public void testThreadPoolAlertTask() {
        final int processors = Runtime.getRuntime().availableProcessors();
        final int corePoolSize = 2;//processors + 1;
        final int maximumPoolSize = 3;//corePoolSize*2;
        final int queueSize = 5;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy()) {

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                System.out.println("准备执行：" + ((AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>) r).getName());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                System.out.println("执行完成： " + ((AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>) r).getName());
            }
            @Override
            protected void terminated() {
                System.out.println("线程池退出");
            }

        };

        String[] msgs = {
                "<38>Oct 26 11:49:15 localhost snort: \"(arp_spoof) unicast ARP request",
                "<38>localhost snort: [116:442:1] \"(icmp4) ICMP destination unreachable communication with destination host is administratively prohibited\" [Priority: 3] {ICMP} 172.16.39.5 -> 172.16.39.16",
                "<38>Oct 26 11:49:23 localhost [116:442:1] \"(icmp4) ICMP destination unreachable communication with destination host is administratively prohibited\" [Priority: 3] {ICMP} 172.16.39.5 -> 172.16.39.16",
                "<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22",
                "<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 <- 224.0.0.22",
                "<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22",
                "<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22",
                "<38>Oct 26 11:49:25 localhost snort: [122:15:1] \"(port_scan) IP filtered protocol sweep\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22",
                "<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22",
                "<78>Oct 31 17:10:01 localhost CROND[16849]: (root) CMD (/usr/lib64/sa/sa1 1 1)",
                "<78>Oct 31 17:10:01 localhost CROND: (root) CMD (/usr/lib64/sa/sa1 1 1)",
                "<30>Oct 31 17:10:01 localhost systemd: Starting Session 2748 of user root.",
                "<38>Nov  1 09:49:13 localhost snort: [122:23:1] \"(port_scan) UDP filtered portsweep\" [Priority: 3] {UDP} 172.16.39.68:55326 -> 224.0.0.252:5355",
                "<38>Nov  1 09:49:19 localhost snort: \"(arp_spoof) unicast ARP request",
                "<38>Nov  1 09:49:35 localhost snort: [116:442:1] \"(icmp4) ICMP destination unreachable communication with destination host is administratively prohibited\" [Priority: 3] {ICMP} 172.16.39.5 -> 172.16.39.135"
        };
        for (int i=0; i<20; i++) {
            IdsSyslogParser log = new IdsSyslogParser();
            log.parser(msgs[i%msgs.length]);
            pool.execute(new AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>("task"+(i+1), log));
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
    public void testSyslogThreadPoolExecutorAlertTask() {
        Thread threadPut = new Thread(()->{
            int i = 1;
            while(isrun) {
                IdsSyslogParser parser = new IdsSyslogParser();
                parser.parser("<38>Oct 26 11:49:25 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.69 -> 224.0.0.22");

                ThreadPoolExecutor pool = new SyslogThreadPoolExecutor(1, 1,10, TimeUnit.MILLISECONDS,
                        new LinkedBlockingDeque<Runnable>(2),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.DiscardPolicy());
                while(isrun) {
                    pool.execute(new AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>("task"+(i++), parser));
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
                pool.shutdown();
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
        threadPut.start();

        try {
            Thread.sleep(4000);
            isrun = false;

            threadPut.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSyslogThreadPoolExecutorSyslogServer() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue();
        List<Thread> threads = new ArrayList(3);

        if (!initSyslogServer()) {
            return;
        }

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

    @Test
    public void testSyslogThreadPoolExecutorSyslogServerAlertTask() {
        ThreadPoolExecutor pool = new SyslogThreadPoolExecutor(1, 1,10, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(2),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
        List<Thread> threads = new ArrayList();

        if (!initSyslogServer()) {
            return;
        }

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, pool);
            logServer.start();
        });
        threads.add(threadLogServer);

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

        if (!initSyslogServer()) {
            return;
        }

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
    public void testSyslogThreadPoolExecutorSyslogServerMybatisAlertTask() {
        ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
        List<Thread> threads = new ArrayList();

        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);

        if (!initSyslogServer()) {
            return;
        }

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, pool, dao);
            logServer.start();
        });
        threads.add(threadLogServer);

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
        System.out.println(String.format("app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));

        if (!initSyslogServer()) {
            return;
        }

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
    public void testSyslogThreadPoolExecutorSyslogServerMybatisRestAlertTask() {
        ThreadPoolExecutor pool = SyslogThreadPoolExecutor.buildPool(60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1000));
        List<Thread> threads = new ArrayList();

        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));

        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);

        if (!initSyslogServer()) {
            return;
        }

        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, pool, dao);
            logServer.start();
        });
        threads.add(threadLogServer);

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
