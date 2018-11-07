package com.ids.syslog;


import java.util.concurrent.*;

public class SyslogThreadPoolExecutor extends ThreadPoolExecutor {
    public static class SyslogTask implements Runnable {
        private String name = "task";
        public SyslogTask(String name) { this.name = name; }
        public String getName() { return name; }
        public void run() {
        }
    }

    private static final int  processors = Runtime.getRuntime().availableProcessors();
    public static ThreadPoolExecutor buildPool(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        return build(keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public ThreadPoolExecutor buildPool(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        return build(keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public ThreadPoolExecutor buildPool(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        return build(keepAliveTime, unit, workQueue, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public ThreadPoolExecutor buildPool(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return build(keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public SyslogThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {  }
    @Override
    protected void afterExecute(Runnable r, Throwable t) { }
    @Override
    protected void terminated() {  }

    private static ThreadPoolExecutor build(long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new SyslogThreadPoolExecutor(processors+1, processors*2, keepAliveTime, unit, workQueue, threadFactory, handler);
    }
}
