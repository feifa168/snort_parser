package ids;

import com.ids.syslog.SyslogServer;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSyslog {
    @BeforeClass
    public static void testBefore() {
    }

    @Test
    public void MyTestSyslogServer() {
        SyslogServer logServer = new SyslogServer(514);
        logServer.start();
    }
}
