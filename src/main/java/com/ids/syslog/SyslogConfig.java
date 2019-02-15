package com.ids.syslog;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

//<syslog>
//        <parse>
//        <item type="ids">
//        <regex><![CDATA[<(?<pri>\d+)>(?<time>\w{3}\s+\d+\s+\d+:\d+:\d+)?\s*(?<host>\w+)\s+((?<tag>\w+)(\[(?<pid>\w+)\])?:)?\s*(\[(?<gid>\w+):(?<sid>\w+):(?<rid>\w+)\])?\s*(\"(?<msg>.+)\"|(?<msg1>.+))(\s+\[\w+:\s+(?<priority>\d+)\]\s+\{(?<proto>\w+)\}\s+(?<sip>\d+\.\d+\.\d+\.\d+)(\:(?<sport>\d+))?\s+(?<direction>->|<-)\s+(?<dip>\d+\.\d+\.\d+\.\d+)(\:(?<dport>\d+))?)?]]></regex>
//        <format><![CDATA[yyyy-MM-dd HH:mm:ss]]></format>

public class SyslogConfig {
    static public class SyslogServerInfo {
        public static String defaultProtocol = "udp";
        public static int defaultPort        = 514;
        public String protolcol;
        public int port;
        public SyslogServerInfo() {
            port        = defaultPort;
            protolcol   = defaultProtocol;
        }
    }
    static public class SyslogReceiver {
        public static String defaultProtocol = "udp";
        public static int defaultPort        = 514;
        public String protolcol;
        public String host;
        public int port;
        public SyslogReceiver() {
            port        = defaultPort;
            protolcol   = defaultProtocol;
            host        = "";
        }
    }
    static public class SensorInfo {
        public String name = "";
        public boolean uselocalip = false;
        public String ip;
        public String source = "GRXA";
        public String type = "syslog";
        public String delimiter = "^";
        public String tag = "idsparser";

        public SensorInfo() {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    public static String regex;
    public static String tmOutFormat;
    public static String tmFormat;
    public static List<SyslogServerInfo> logServers = new ArrayList<>();
    public static List<SyslogReceiver> logReceivers = new ArrayList<>();
    public static SensorInfo sensor = new SensorInfo();;

    public static boolean parse(String xml)  {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(xml));

            List<Node> ndServers = doc.selectNodes("/syslog/servers/server");
            if (null != ndServers) {
                for (Node ndServer : ndServers) {
                    Node ndProtocol = ndServer.selectSingleNode("protocol");
                    Node ndPort     = ndServer.selectSingleNode("port");

                    String s;
                    SyslogServerInfo server = new SyslogServerInfo();
                    if (null != ndProtocol) {
                        s = ndProtocol.getText();
                        if (!s.isEmpty())
                            server.protolcol = s;
                    } else {
                        server.protolcol = SyslogServerInfo.defaultProtocol;
                    }

                    if (null != ndPort) {
                        s = ndPort.getText();
                        if (!s.isEmpty())
                            server.port = Integer.parseInt(s);
                    } else {
                        server.port = SyslogServerInfo.defaultPort;
                    }

                    logServers.add(server);
                }
            } else {
                SyslogServerInfo server = new SyslogServerInfo();
                logServers.add(server);
            }

            List<Node> ndReceives = doc.selectNodes("/syslog/receivers/server");
            if (null != ndReceives) {
                for (Node ndServer : ndReceives) {
                    Node ndProtocol = ndServer.selectSingleNode("protocol");
                    Node ndHost     = ndServer.selectSingleNode("host");
                    Node ndPort     = ndServer.selectSingleNode("port");

                    String s;
                    SyslogReceiver receiver = new SyslogReceiver();
                    if (null != ndProtocol) {
                        s = ndProtocol.getText();
                        if (!s.isEmpty())
                            receiver.protolcol = s;
                    } else {
                        receiver.protolcol = SyslogServerInfo.defaultProtocol;
                    }

                    if (null != ndHost) {
                        s = ndHost.getText();
                        if (!s.isEmpty())
                            receiver.host = s;
                    }

                    if (null != ndPort) {
                        s = ndPort.getText();
                        if (!s.isEmpty())
                            receiver.port = Integer.parseInt(s);
                    } else {
                        receiver.port = SyslogServerInfo.defaultPort;
                    }

                    logReceivers.add(receiver);
                }
            }

            Node ndSensor = doc.selectSingleNode("/syslog/sensor");
            if (ndSensor != null) {
                sensor.name     = getNodeText(ndSensor.selectSingleNode("name"),"");
                sensor.source   = getNodeText(ndSensor.selectSingleNode("source"),    sensor.source);
                sensor.uselocalip   = Boolean.valueOf(getNodeText(ndSensor.selectSingleNode("uselocalip"), "false"));
                sensor.type     = getNodeText(ndSensor.selectSingleNode("type"),      sensor.type);
                sensor.delimiter= getNodeText(ndSensor.selectSingleNode("delimiter"), sensor.delimiter);
                sensor.tag      = getNodeText(ndSensor.selectSingleNode("tag"), sensor.tag);
            }

            Node item = doc.selectSingleNode("/syslog/parse/item[@type=\"ids\"]");
            if (item != null) {
                Node ndregex    = item.selectSingleNode("regex");
                Node ndformat   = item.selectSingleNode("tmFormat");
                Node ndOutFormat   = item.selectSingleNode("tmOutFormat");
                String s;
                if (ndregex != null) {
                    s = ndregex.getText();
                    if (!s.equals(""))
                        regex = s;
                }
                if (ndOutFormat != null) {
                    s = ndOutFormat.getText();
                    if (!s.equals(""))
                        tmOutFormat = s;
                }
                if (ndformat != null) {
                    s = ndformat.getText();
                    if (!s.equals(""))
                        tmFormat = s;
                }
                if ((regex != null) || (tmFormat != null) || (tmOutFormat != null))
                    return true;
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getNodeText(Node node, String defaultText) {
        if (node != null) {
            String s = node.getText();
            if (!"".equals(s))
                return s;
        }
        return defaultText;
    }
}
