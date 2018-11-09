package ids;

import org.junit.Test;

import java.io.IOException;

public class TestOther {
    @Test
    public void testRead() {    // Junit不支持控制台输入

        byte[] buf = new byte[32];
        try {
            System.in.read(buf, 0, buf.length);

            while (true) {
                if (buf.equals("exit")) {
                    break;
                }

                System.in.read(buf, 0, buf.length);
                System.out.println(buf);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
