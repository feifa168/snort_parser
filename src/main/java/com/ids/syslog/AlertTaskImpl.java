package com.ids.syslog;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import com.ids.debug.DebugInformation;
import com.ids.syslog.client.CustomSyslog;
import com.ids.syslog.client.NettySyslogClient;

import java.util.List;

public class AlertTaskImpl<T extends IdsSyslogParser, E extends IdsAlertInterface> implements IAlertTask<T>, Runnable {
    private T data;
    private E dao;
    private String name;
    List<NettySyslogClient> syslogClients;

    public AlertTaskImpl() {
        init("", null, null);
    }

    public AlertTaskImpl(String name, T data) {
        init(name, data, null);
    }

    public AlertTaskImpl(String name, T data, E dao) {
        init(name, data, dao);
    }

    public AlertTaskImpl(String name, T data, E dao, List<NettySyslogClient> syslogClients) {
        init(name, data, dao, syslogClients);
    }

    private void init(String name, T data, E dao) {
        this.name = name;
        this.data = data;
        this.dao  = dao;
        this.syslogClients = null;
    }

    private void init(String name, T data, E dao, List<NettySyslogClient> syslogClients) {
        this.name = name;
        this.data = data;
        this.dao  = dao;
        this.syslogClients = syslogClients;
    }

    public void run() {
        if (data != null) {
            if (DebugInformation.ifDisplayMsg.get()) {
                System.out.println(data.toString());
            }

            IdsAlert alert = data.getIdsAlert();
            if (dao != null) {
                dao.putIdsAlert(alert);
            }

            if (syslogClients != null) {
                for (NettySyslogClient syslogClient : syslogClients) {
                    syslogClient.sendMessage(new CustomSyslog(alert));
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public E getDao() {
        return dao;
    }

    public void setDao(E dao) {
        this.dao = dao;
    }
}
