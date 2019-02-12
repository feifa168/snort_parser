package ids;

import com.ids.beans.IdsAlert;
import com.ids.syslog.client.CustomSyslog;
import com.ids.syslog.client.NettySyslogClient;
import com.ids.syslog.client.SyslogBuild;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class TestSyslogClient {
    @Test
    public void sendMsg() {

        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);

        NettySyslogClient client = new NettySyslogClient();
        String host = "172.16.39.251";
        int port = 514;
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(()->{
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            IdsAlert alert = new IdsAlert( 5,"2018-10-29 10:25:43", 38, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
            SyslogBuild log = new CustomSyslog(alert);
            client.sendMessage(log);
            client.sendMessage(log);

            try {
                client.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            client.start(host, port);
            //Thread.sleep(3000);
            latch.countDown();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("end");
        }
    }
}
