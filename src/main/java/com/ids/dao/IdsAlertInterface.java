package com.ids.dao;

import com.ids.beans.IdsAlert;

public interface IdsAlertInterface {

    //select * from alert where id = #{value}
    IdsAlert getIdsAlertById(int id);

    //insert into alert (time, pri, HOST, tag, gid, sid, rid, msg, priority, proto, sip, sport, isleft2right, dip, dport) values(#{time}, #{pri}, #{host}, #{tag}, #{gid}, #{sid}, #{rid}, #{msg}, #{priority}, #{proto}, #{sip}, #{sport}, #{isleft2right}, #{dip}, #{dport')
    void putIdsAlert(IdsAlert alert);
}
