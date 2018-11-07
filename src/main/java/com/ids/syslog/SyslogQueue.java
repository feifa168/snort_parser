package com.ids.syslog;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SyslogQueue<T> extends ConcurrentLinkedDeque<T> {
//    public static SyslogQueue logQueue = null;
//
//    public static <E> SyslogQueue getInstance() {
//        if (logQueue == null) {
//            logQueue = new SyslogQueue<E>();
//        }
//
//        return logQueue;
//    }
    public SyslogQueue() {
        super();
    }
}