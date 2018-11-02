package com.ids;

import com.ids.syslog.SyslogServer;

public class App {
    public static void main(String[] args) {
        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514);
            logServer.start();
        });

        Thread threadParseLog = new Thread(()->{
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
}
