package com.ids.dao;

import com.ids.beans.IdsAlert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IdsAlertInterface {

    //select * from alert where id = #{value}
    IdsAlert getIdsAlertById(int id);
    List<IdsAlert> getAllIdsAlers();
    List<IdsAlert> getIdsAlersBetweenTime(@Param("from") String fromTime, @Param("to") String toTime);
    List<IdsAlert> getIdsAlersBetweenId(@Param("from") String fromId, @Param("to") String toId);
    List<IdsAlert> getIdsAlersByTable(@Param("name") String name);
    List<IdsAlert> getIdsAlersByNameValue(@Param("name") String name, @Param("value") String value);

    //insert into alert (time, pri, HOST, tag, gid, sid, rid, msg, priority, proto, sip, sport, isleft2right, dip, dport) values(#{time}, #{pri}, #{host}, #{tag}, #{gid}, #{sid}, #{rid}, #{msg}, #{priority}, #{proto}, #{sip}, #{sport}, #{isleft2right}, #{dip}, #{dport')
    void putIdsAlert(IdsAlert alert);
}
