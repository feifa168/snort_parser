package com.ids.syslog;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SyslogTaskImpl<T extends ConcurrentLinkedDeque, E extends IdsSyslogParser> implements ISyslogTask<T>, Runnable {
    private T queue;
    private String name;

    public SyslogTaskImpl(T data, String name) {
        this.queue = data;
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public T getTask() {
        return queue;
    }

    @Override
    public void setQueue(T t) {
        this.queue = t;
    }

    @Override
    public T getQueue() { return queue; }

    @Override
    public void run() {
        E e = (E) queue.poll();
        if (e != null)
            System.out.println(name + ", " + Thread.currentThread().getName() + ", " + e.toString());
    }
}
