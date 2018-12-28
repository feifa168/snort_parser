package com.ids.syslog;

import com.ids.dao.IdsAlertInterface;
import com.ids.debug.DebugInformation;

public class AlertTaskImpl<T extends IdsSyslogParser, E extends IdsAlertInterface> implements IAlertTask<T>, Runnable {
    private T data;
    private E dao;
    private String name;

    public AlertTaskImpl() {
        init("", null, null);
    }

    public AlertTaskImpl(String name, T data) {
        init(name, data, null);
    }

    public AlertTaskImpl(String name, T data, E dao) {
        init(name, data, dao);
    }

    private void init(String name, T data, E dao) {
        this.name = name;
        this.data = data;
        this.dao  = dao;
    }

    public void run() {
        if (data != null) {
            if (DebugInformation.ifDisplayMsg.get()) {
                System.out.println(data.toString());
            }

            if (dao != null) {
                dao.putIdsAlert(data.getIdsAlert());
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
