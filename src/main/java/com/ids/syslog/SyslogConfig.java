package com.ids.syslog;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;

//<syslog>
//        <parse>
//        <item type="ids">
//        <regex><![CDATA[<(?<pri>\d+)>(?<time>\w{3}\s+\d+\s+\d+:\d+:\d+)?\s*(?<host>\w+)\s+((?<tag>\w+)(\[(?<pid>\w+)\])?:)?\s*(\[(?<gid>\w+):(?<sid>\w+):(?<rid>\w+)\])?\s*(\"(?<msg>.+)\"|(?<msg1>.+))(\s+\[\w+:\s+(?<priority>\d+)\]\s+\{(?<proto>\w+)\}\s+(?<sip>\d+\.\d+\.\d+\.\d+)(\:(?<sport>\d+))?\s+(?<direction>->|<-)\s+(?<dip>\d+\.\d+\.\d+\.\d+)(\:(?<dport>\d+))?)?]]></regex>
//        <format><![CDATA[yyyy-MM-dd HH:mm:ss]]></format>

public class SyslogConfig {
    public static String regex;
    public static String tmOutFormat;
    public static String tmFormat;
    public static boolean parse(String xml)  {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(xml));
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
}
