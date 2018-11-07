package com.ids.syslog;

public interface ISyslogTask<T> {
    T getTask();
    void setQueue(T t);
    T getQueue();
}
