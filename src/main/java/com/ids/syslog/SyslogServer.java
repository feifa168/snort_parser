package com.ids.syslog;


import com.ids.dao.IdsAlertInterface;

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
import java.util.concurrent.ThreadPoolExecutor;

public class SyslogServer {
    private int port;
    private final int default_port = 514;
    private DatagramChannel channel = null;
    private Selector select = null;
    private SyslogQueue<IdsSyslogParser> queue = null;
    private ThreadPoolExecutor pool = null;
    private IdsAlertInterface  dao  = null;

    public SyslogServer() {
        init(default_port, null, null, null);
    }
    public SyslogServer(int port) {
        init(port, null, null, null);
    }

    public SyslogServer(int port, SyslogQueue<IdsSyslogParser> queue) {
        init(port, queue, null, null);
    }
    public SyslogServer(int port, ThreadPoolExecutor pool) {
        init(port, null, pool, null);
    }
    public SyslogServer(int port, ThreadPoolExecutor pool, IdsAlertInterface dao) {
        init(port, null, pool, dao);
    }

    private void init(int port, SyslogQueue<IdsSyslogParser> queue, ThreadPoolExecutor pool, IdsAlertInterface dao) {
        this.port = port;
        this.queue = queue;
        this.pool = pool;
        this.dao  = dao;
        try {
            select = Selector.open();
            channel = DatagramChannel.open();
        } catch (IOException e) {
            select = null;
            channel = null;

            e.printStackTrace();
        }
    }

    public void setPort(int port) { this.port = port; }
    public void setQueue(SyslogQueue<IdsSyslogParser> queue) { this.queue = queue; }

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
            if (queue != null) {
                queue.add(idsParser);
            }

            if (pool != null) {
                AlertTaskImpl<IdsSyslogParser, IdsAlertInterface> task = new AlertTaskImpl<>("taskname", idsParser, dao);
                pool.execute(task);
            }

            System.out.println("接收：" + content.toString().trim());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
