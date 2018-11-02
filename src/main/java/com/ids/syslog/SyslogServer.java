package com.ids.syslog;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class SyslogServer {
    private int port;
    private final int default_port = 514;
    private DatagramChannel channel = null;
    private Selector select = null;

    public SyslogServer() {
        port = default_port;
        init(port);
    }

    public SyslogServer(int port) {
        this.port = port;
        init(port);
    }

    private void init(int port) {
        try {
            select = Selector.open();
            channel = DatagramChannel.open();
        } catch (IOException e) {
            select = null;
            channel = null;

            e.printStackTrace();
        }
    }

    public void start() {
        if ((channel == null) || (select == null))
            return;

        try {
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(port));
            channel.register(select, SelectionKey.OP_READ);
        } catch(ClosedChannelException e) {
            e.printStackTrace();
        } catch(SocketException e) {
            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("begin...");
            while (select.select() > 0) {
                Iterator item = select.selectedKeys().iterator();
                while (item.hasNext()) {
                    SelectionKey key = (SelectionKey)item.next();
                    item.remove();

                    if (key.isReadable()) {
                        receiveData(key);
                    }
                }
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveData(SelectionKey key) {
        if (key == null)
            return;

        DatagramChannel sc = (DatagramChannel)key.channel();
        ByteBuffer buf = ByteBuffer.allocate(4096);
        try {
            SocketAddress address = sc.receive(buf);
            String[] addrPort = address.toString().replace("/", "").split(":");
            String clientAddress;
            String clientPort;
            if (addrPort.length == 2) {
                clientAddress = addrPort[0];
                clientPort = addrPort[1];
            }
            buf.flip();
            String content = new String();

            while(buf.hasRemaining()) {
                buf.get(new byte[buf.limit()]);
                content += new String(buf.array());
            }
            buf.clear();
            IdsSyslogParser idsParser = new IdsSyslogParser();
            idsParser.parser(content);
            SyslogQueue.getInstance().add(idsParser);

            System.out.println("接收：" + content.toString().trim());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
