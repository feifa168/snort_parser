package com.ids.syslog.client;

import com.ids.beans.IdsAlert;

public class CustomSyslog implements SyslogBuild {
    public CustomSyslog() {
        this.alert = null;
    }

    public CustomSyslog(IdsAlert alert) {
        this.alert = alert;
    }

    public void setAlert(IdsAlert alert) {
        this.alert = alert;
    }

    @Override
    public String build() {
        StringBuilder log = new StringBuilder(128);
        String separator = "^";
        log.append("<").append(alert.getPri()).append(">")
                //.append(" ").append(alert.getHost())
                .append(" ").append("custom:")
                .append(" ")
                .append(alert.getTime()).append(separator)
                .append("GRXA").append(separator)
                .append("syslog").append(separator)
                .append(alert.getPri()).append(separator)
                .append((alert.getHost()!=null)?alert.getHost():"").append(separator)
                .append((alert.getTag()!=null)?alert.getHost():"").append(separator)
                .append(alert.getGid()).append(separator)
                .append(alert.getSid()).append(separator)
                .append(alert.getRid()).append(separator)
                .append((alert.getMsg()!=null)?alert.getMsg():"").append(separator)
                .append(alert.getPriority()).append(separator)
                .append((alert.getProto()!=null)?alert.getProto():"").append(separator)
                .append((alert.getSip()!=null)?alert.getSip():"").append(separator)
                .append(alert.getSport()).append(separator)
                .append(alert.getIsLeft2Right()).append(separator)
                .append((alert.getDip()!=null)?alert.getDip():"").append(separator)
                .append(alert.getDport()).append(separator)
                ;

        return log.toString();
    }

    private IdsAlert alert;
}
