package com.ids.syslog;


import com.ids.dao.IdsAlertInterface;
import com.ids.debug.DebugInformation;
import com.ids.syslog.client.NettySyslogClient;

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
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class SyslogServer {
    static private enum RunStatus {
        RUN_NEW,
        RUN_RUNNABLE,
        RUN_TERMINATED,
    };
    private static final long MAX_WAIT_MILLISECONDS = 10*1000;
    private boolean isRun = true;
    private RunStatus status = RunStatus.RUN_NEW;
    private int port;
    private final int default_port = 514;
    private DatagramChannel channel = null;
    private Selector select = null;
    private SyslogQueue<IdsSyslogParser> queue = null;
    private ThreadPoolExecutor pool = null;
    private IdsAlertInterface  dao  = null;
    private List<NettySyslogClient> syslogClients = null;

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
    public SyslogServer(int port, ThreadPoolExecutor pool, IdsAlertInterface dao, List<NettySyslogClient> syslogClients) {
        init(port, null, pool, dao, syslogClients);
    }

    private void init(int port, SyslogQueue<IdsSyslogParser> queue, ThreadPoolExecutor pool, IdsAlertInterface dao) {
        this.port = port;
        this.queue = queue;
        this.pool = pool;
        this.dao  = dao;
        this.syslogClients = null;
    }
    private void init(int port, SyslogQueue<IdsSyslogParser> queue, ThreadPoolExecutor pool, IdsAlertInterface dao, List<NettySyslogClient> syslogClients) {
        this.port = port;
        this.queue = queue;
        this.pool = pool;
        this.dao  = dao;
        this.syslogClients = syslogClients;
    }

    public void setPort(int port) { this.port = port; }
    public void setQueue(SyslogQueue<IdsSyslogParser> queue) { this.queue = queue; }

    public void stop() {
        isRun = false;
    }

    public boolean isRunnable() {
        return status == RunStatus.RUN_RUNNABLE;
    }

    public void start() {
        try {
            select = Selector.open();
            channel = DatagramChannel.open();

            try {
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(port));
                channel.register(select, SelectionKey.OP_READ);

                try {
                    System.out.println("syslog server is running...");
                    status = RunStatus.RUN_RUNNABLE;
                    while (isRun) {
                        int selNum = select.select(MAX_WAIT_MILLISECONDS);
                        if (selNum < 1) {
                            continue;
                        }

                        Iterator item = select.selectedKeys().iterator();
                        while (item.hasNext()) {
                            SelectionKey key = (SelectionKey)item.next();
                            item.remove();

                            if (key.isReadable()) {
                                receiveData(key);
                            }
                        }
                    }
                    System.out.println("syslog server exit");
                }catch(IOException e) {
                    e.printStackTrace();
                }

            } catch(ClosedChannelException e) {
                e.printStackTrace();
            } catch(SocketException e) {
                e.printStackTrace();
            }catch(IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            select = null;
            channel = null;

            e.printStackTrace();
        }
        status = RunStatus.RUN_TERMINATED;
    }

    private void receiveData(SelectionKey key) {
        if (key == null)
            return;

        DatagramChannel sc = (DatagramChannel)key.channel();
        ByteBuffer buf = ByteBuffer.allocate(4096);
        try {
            SocketAddress address = sc.receive(buf);
            String[] addrPort = address.toString().replace("/", "").split(":");
            String clientAddress = "";
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
            IdsSyslogParser idsParser = new IdsSyslogParser(clientAddress);
            idsParser.parser(content);

            // 只写入tag为snort的日志
            String tag = idsParser.idsAlert.getTag();
            if ( (null==tag) || (!tag.equals("snort")) ) {
                if (DebugInformation.ifDisplayMsg.get()) {
                    System.out.println("not valid syslog");
                }
            } else {
                if (queue != null) {
                    queue.add(idsParser);
                }

                if (pool != null) {
                    AlertTaskImpl<IdsSyslogParser, IdsAlertInterface> task = new AlertTaskImpl<>("taskname", idsParser, dao, syslogClients);
                    pool.execute(task);
                }
            }

            if (DebugInformation.ifDisplayMsg.get()) {
                System.out.println("接收：" + content.toString().trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
