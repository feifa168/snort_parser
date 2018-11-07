package ids;

import com.ids.rest.RestServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Test;

import java.io.IOException;

//@XmlRootElement
//class MyTestXml {
//    private int a = 5;
//}

public class TestRestServer {
    @Test
    public void testRestServer() {
        final HttpServer server = RestServer.startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", RestServer.BASE_URI));
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.shutdown();
    }
}
