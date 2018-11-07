package com.ids.beans;


import org.apache.ibatis.type.Alias;

import javax.xml.bind.annotation.XmlRootElement;

@Alias("IdsAlert")
@XmlRootElement
public class IdsAlert {
    private int     id;
    private String  time;
    private int     pri;
    private String  host;
    private String  tag;
    private int     gid;
    private int     sid;
    private int     rid;
    private String  msg;
    private int     priority;
    private String  proto;
    private String  sip;
    private int     sport;
    private boolean isleft2right;
    private String  dip;
    private int     dport;

    public IdsAlert() {}
    public IdsAlert(int id, String time, int pri, String host, String tag, int gid, int sid, int rid,
                    String msg, int priority, String proto, String sip, int sport, boolean isleft2right, String dip, int dport) {
        this.id     = id;
        this.time   = time;
        this.pri    = pri;
        this.host   = host;
        this.tag    = tag;
        this.gid    = gid;
        this.sid    = sid;
        this.rid    = rid;
        this.msg    = msg;
        this.priority = priority;
        this.proto  = proto;
        this.sip    = sip;
        this.sport  = sport;
        this.isleft2right = isleft2right;
        this.dip    = dip;
        this.dport  = dport;
    }
    public int     getId() { return id; }
    public String   getTime() { return time; }
    public int      getPri() { return pri; }
    public String   getHost() { return host; }
    public String   getTag() { return tag; }
    public int      getGid() { return gid; }
    public int      getSid() { return sid; }
    public int      getRid() { return rid; }
    public String   getMsg() { return msg; }
    public int      getPriority() { return priority; }
    public String   getProto() { return proto; }
    public String   getSip() { return sip; }
    public int      getSport() { return sport; }
    public boolean  getIsLeft2Right() { return isleft2right; }
    public String   getDip() { return dip; }
    public int      getDport() { return dport; }

    public void setId(int id) { this.id = id; }
    public void setTime(String time) { this.time = time; }
    public void setpri(int pri) { this.pri = pri; }
    public void setHost(String host) { this.host = host; }
    public void setTag(String tag) { this.tag = tag; }
    public void setGid(int gid) { this.gid = gid; }
    public void setSid(int sid) { this.sid = sid; }
    public void setRid(int rid) { this.rid = rid; }
    public void setMsg(String msg) { this.msg = msg; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setProto(String proto) { this.proto = proto; }
    public void setSip(String sip) { this.sip = sip; }
    public void setSport(int sport) { this.sport = sport; }
    public void setIsLeft2Right(boolean isleft2right) { this.isleft2right = isleft2right; }
    public void setDip(String dip) { this.dip = dip; }
    public void setDport(int dport) { this.dport = dport; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);

        //int facilitySeverity = Integer.parseInt(pri);
        sb.append("pri="+pri)
                .append(", facility="+(pri>>3))
                .append(", severity="+(pri&0x7))
                .append(", time="+time)
                .append(", host="+host)
                .append(", tag="+tag)
                .append(", gid="+gid)
                .append(", sid="+sid)
                .append(", rid="+rid)
                .append(", msg="+msg)
                .append(", priority="+priority)
                .append(", proto="+proto)
                .append(", sip="+sip)
                .append(", sport="+sport)
                .append(", isLeft2Right="+isleft2right)
                .append(", dip="+dip)
                .append(", dport="+dport)
        ;
        return sb.toString();
    }
}
