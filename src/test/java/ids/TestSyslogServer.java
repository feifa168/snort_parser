package ids;

import com.ids.beans.IdsAlert;
import com.ids.jdbc.DbMySql;
import com.ids.syslog.IdsSyslogParser;
import com.ids.syslog.SyslogQueue;
import com.ids.syslog.SyslogServer;
import org.junit.Test;

import java.sql.SQLException;

public class TestSyslogServer {
    @Test
    public void testSyslogTime() {
        System.out.println(IdsSyslogParser.transformSyslogDate("Oct 31 17:10:01"));
    }

    @Test
    public void testSyslog() {
        IdsSyslogParser parser = new IdsSyslogParser();
        IdsAlert alert = null;
        int pri;
        IdsSyslogParser.Facility f;
        IdsSyslogParser.Severity s;

        parser.parser("<78>Oct 31 17:10:01 localhost CROND[16849]: (root) CMD (/usr/lib64/sa/sa1 1 1)");
        alert = parser.getIdsAlert();
        pri = alert.getPri();
        f = IdsSyslogParser.Facility.values()[pri>>3];
        s = IdsSyslogParser.Severity.values()[pri&0x7];
        System.out.println(parser.toString()+", "+f.getCode()+" "+f.getName());

        parser.parser("<30>Oct 31 17:10:01 localhost systemd: Starting Session 2748 of user root.");
        System.out.println(parser.toString());

        parser.parser("<38>Oct 31 17:10:15 localhost snort: [116:444:1] \"(ipv4) IPv4 option set\" [Priority: 3] {IGMP} 172.16.39.252 -> 224.0.0.251");
        System.out.println(parser.toString());

        parser.parser("<38>Oct 31 17:10:20 localhost snort: \"(arp_spoof) unicast ARP request\"");
        System.out.println(parser.toString());

        parser.parser("<38>Nov  1 09:49:13 localhost snort: [122:23:1] \"(port_scan) UDP filtered portsweep\" [Priority: 3] {UDP} 172.16.39.68:55326 -> 224.0.0.252:5355");
        System.out.println(parser.toString());
    }

    @Test
    public void testSyslogQueue() {
        SyslogQueue<String> queue = new SyslogQueue();
        queue.add(new String("hello"));
        queue.offer("tom");

        String s1 = queue.poll();
        String s2 = queue.poll();
        String s3 = queue.poll();
        String s4 = queue.poll();
    }

    @Test
    public void testSyslogServer() {
        SyslogServer logServer = new SyslogServer(514);
        logServer.start();
    }

    @Test
    public void testSyslogServerQueue() {
        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue<>();
        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });

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
    public void testSyslogServerQueueMysql() {

        SyslogQueue<IdsSyslogParser> queue = new SyslogQueue<>();
        Thread threadLogServer = new Thread(()->{
            SyslogServer logServer = new SyslogServer(514, queue);
            logServer.start();
        });

        Thread threadParseLog = new Thread(()->{
            DbMySql mysql = null;
            try {
                mysql = new DbMySql("jdbc:mysql://172.16.39.251:12239/ids", "root", "123456");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            while(true) {
                if (!queue.isEmpty()) {
                    IdsSyslogParser parser = (IdsSyslogParser)queue.poll();
                    if (parser != null) {
                        System.out.println("parser is " + parser.toString());

                        IdsAlert ids = new IdsAlert( 5,"2018-10-29 10:25:43", 139, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
                        ids = parser.getIdsAlert();
                        mysql.executeInsert(ids, "alert");
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
}
