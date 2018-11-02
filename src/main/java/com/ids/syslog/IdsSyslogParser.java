package com.ids.syslog;

import com.ids.beans.IdsAlert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//    Numerical             Facility
//    Code
//    0             kernel messages
//    1             user-level messages
//    2             mail system
//    3             system daemons
//    4             security/authorization messages (note 1)
//    5             messages generated internally by syslogd
//    6             line printer subsystem
//    7             network news subsystem
//    8             UUCP subsystem
//    9             clock daemon (note 2)
//    10             security/authorization messages (note 1)
//    11             FTP daemon
//    12             NTP subsystem
//    13             log audit (note 1)
//    14             log alert (note 1)
//    15             clock daemon (note 2)
//    16             local use 0  (local0)
//    17             local use 1  (local1)
//    18             local use 2  (local2)
//    19             local use 3  (local3)
//    20             local use 4  (local4)
//    21             local use 5  (local5)
//    22             local use 6  (local6)
//    23             local use 7  (local7)
//    Note 1 - Various operating systems have been found to utilize
//    Facilities 4, 10, 13 and 14 for security/authorization,
//    audit, and alert messages which seem to be similar.
//    Note 2 - Various operating systems have been found to utilize
//    both Facilities 9 and 15 for clock (cron/at) messages.
//Numerical         Severity
//Code
//0       Emergency: system is unusable
//1       Alert: action must be taken immediately
//2       Critical: critical conditions
//3       Error: error conditions
//4       Warning: warning conditions
//5       Notice: normal but significant condition
//6       Informational: informational messages
//7       Debug: debug-level messages

public class IdsSyslogParser {
    public enum Facility {
        KernelMsg(0, "kernel messages"),
        UserLevelMsg(1, "user-level messages"),
        MailSystem(2, "mail system"),
        SysDaemon(3, "system daemons"),
        SecurityAuth4(4, "security/authorization messages"),
        Generated(5, "messages generated internally by syslogd"),
        LinePrinter(6, "line printer subsystem"),
        NetworkNews(7, "network news subsystem"),
        UUCP(8, "UUCP subsystem"),
        ClockDaemon9(9, "clock daemon"),
        SecurityAuth10(10, "security/authorization messages"),
        FtpDaemon(11, "FTP daemon"),
        NTP(12, "NTP subsystem"),
        LogAudit(13, "log audit"),
        LogAlert(14, "log alert"),
        ClockDaemon15(15, "clock daemon"),
        Local0(16, "local0"),
        Local1(17, "local1"),
        Local2(18, "local2"),
        Local3(19, "local3"),
        Local4(20, "local4"),
        Local5(21, "local5"),
        Local6(22, "local6"),
        Local7(23, "local7");

        private int code;
        private String name;
        private Facility(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }
        public String getName() {
            return name;
        }
    }

    public enum Severity {
        Emergency(0, "Emergency"),
        Alert(1, "Alert"),
        Critical(2, "Critical"),
        Error(3, "Error"),
        Warning(4, "Warning"),
        Notice(5, "Notice"),
        Infor(6, "Informational"),
        Debug(7, "Debug");

        private int code;
        private String name;
        private Severity(int code, String name) {
            this.code = code;
            this.name = name;
        }
        public int getCode() {
            return code;
        }
        public String getName() {
            return name;
        }
    }

    public IdsAlert idsAlert = null;
    //private static final String regex = "<(?<PRI>\\d+)>(\\w{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)?\\s*(?<host>\\w+)\\s+((?<tag>\\w+):)?\\s*(\\[(?<gid>\\w+):(?<sid>\\w+):(?<rid>\\w+)\\])?\\s*\\\"(?<msg>.+)\\\"(\\s+\\[\\w+:\\s+(?<priority>\\d+)\\]\\s+\\{(?<proto>\\w+)\\}\\s+(?<sip>\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(?<direction>->|<-)\\s+(?<dip>\\d+\\.\\d+\\.\\d+\\.\\d+))?";
    //private static final String regex = "<(?<PRI>\\d+)>(\\w{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)?\\s*(?<host>\\w+)\\s+((?<tag>\\w+):)?\\s*(\\[(?<gid>\\w+):(?<sid>\\w+):(?<rid>\\w+)\\])?\\s*(\\\"(?<msg>.+)\\\"|(?<msg1>.+))(\\s+\\[\\w+:\\s+(?<priority>\\d+)\\]\\s+\\{(?<proto>\\w+)\\}\\s+(?<sip>\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(?<direction>->|<-)\\s+(?<dip>\\d+\\.\\d+\\.\\d+\\.\\d+))?";
    private static final String regex = "<(?<pri>\\d+)>(?<time>\\w{3}\\s+\\d+\\s+\\d+:\\d+:\\d+)?\\s*(?<host>\\w+)\\s+((?<tag>\\w+):)?\\s*(\\[(?<gid>\\w+):(?<sid>\\w+):(?<rid>\\w+)\\])?\\s*(\\\"(?<msg>.+)\\\"|(?<msg1>.+))(\\s+\\[\\w+:\\s+(?<priority>\\d+)\\]\\s+\\{(?<proto>\\w+)\\}\\s+(?<sip>\\d+\\.\\d+\\.\\d+\\.\\d+)(\\:(?<sport>\\d+))?\\s+(?<direction>->|<-)\\s+(?<dip>\\d+\\.\\d+\\.\\d+\\.\\d+)(\\:(?<dport>\\d+))?)?";
    public void parser(String syslogMsg) {
        Matcher m = Pattern.compile(regex).matcher(syslogMsg);
        if (m.find()) {
            if (idsAlert == null) {
                idsAlert = new IdsAlert();
            }

            String value = getGroupName(m,"pri");
            if (value != null) { idsAlert.setpri(Integer.valueOf(value)); }
            value = getGroupName(m, "time");
            if (value != null) { idsAlert.setTime(IdsSyslogParser.transformSyslogDate(value)); }
            idsAlert.setHost(getGroupName(m, "host"));
            idsAlert.setTag(getGroupName(m,"tag"));
            value = getGroupName(m, "gid");
            if (value != null) { idsAlert.setGid(Integer.valueOf(value)); }
            value = getGroupName(m,"sid");
            if (value != null) { idsAlert.setSid(Integer.valueOf(value)); }
            value = getGroupName(m, "rid");
            if (value != null) { idsAlert.setRid(Integer.valueOf(value)); }
            String msg     = getGroupName(m,"msg");
            if ("".equals(msg)) {
                msg = getGroupName(m, "msg1");
            }
            idsAlert.setMsg(msg);
            value = getGroupName(m, "priority");
            if (value != null) { idsAlert.setPriority(Integer.valueOf(value)); }
            idsAlert.setProto(getGroupName(m,"proto"));
            idsAlert.setSip(getGroupName(m, "sip"));
            value = getGroupName(m, "sport");
            if (value != null) { idsAlert.setSport(Integer.valueOf(value)); }
            String direction   = getGroupName(m, "direction");
            boolean isLeft2Right = true;
            if ("<-".equals(direction)) {
                isLeft2Right = false;
            }
            idsAlert.setIsLeft2Right(isLeft2Right);
            idsAlert.setDip(getGroupName(m,"dip"));
            value = getGroupName(m, "dport");
            if (value != null) { idsAlert.setDport(Integer.valueOf(value)); }
        }
    }

    public IdsAlert getIdsAlert() { return idsAlert; }
    public String toString() {
        return idsAlert.toString();
    }

    private String getGroupName(Matcher m, String groupName) {
        //return m.group(groupName)!=null ? m.group(groupName) : "";
        return m.group(groupName);
    }

    public static String transformSyslogDate(String syslogDate) {
        Calendar cal=Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String tm = null;

        final String DATE_FORMAT = "MMM dd HH:mm:ssyyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.ENGLISH);
        try {
            String strData = syslogDate+year;
            Date dt = dateFormat.parse(strData);
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
            tm = dateFormat.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tm;
    }
}
