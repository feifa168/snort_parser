package com.ids.rest;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("ids")
public class IdsAlertResource {
    private SqlSession sqlSession = null;

    public IdsAlertResource() throws IOException {
        init();
    }
    public SqlSession getSqlSession() { return sqlSession; }

    public void init() throws IOException {
        // mybatis配置文件，这个地方的root地址为：resources，路径要对。
        String resource = "mybatis-config.xml";
        // 得到配置文件流
        InputStream inputStream = Resources.getResourceAsStream(resource);
        // 创建会话工厂，传入mybatis的配置文件信息
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 通过工厂得到SqlSession
        //sqlSessionFactory.getConfiguration().addMapper(IdsAlertInterface.class);
        sqlSession = sqlSessionFactory.openSession();
    }
    public void close() {
        if (sqlSession == null) {
            sqlSession.close();
        }
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String Hello() {
        return "Hello";
    }

    @GET
    @Path("alertxml")
    @Produces(MediaType.APPLICATION_XML)
    public IdsAlert getAllalert() {
        return new IdsAlert();
    }

    @GET
    @Path("alertjson")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getAllalerts() {
        List<IdsAlert> alerts = new ArrayList<>(3);
        alerts.add(new IdsAlert());
        alerts.add(new IdsAlert());
        alerts.add(new IdsAlert());
        return alerts;
    }

    @GET
    @Path("index")
    @Produces(MediaType.TEXT_HTML)
    public String getMain() {
        String htm  = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<script>\n" +
                "function loadXMLDoc()\n" +
                "{\n" +
                "  var x;\n" +
                "  x=document.getElementById(\"numb\").value;\n" +
                "  var xmlhttp;\n" +
                "  if (window.XMLHttpRequest)\n" +
                "  {\n" +
                "    // IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码\n" +
                "    xmlhttp=new XMLHttpRequest();\n" +
                "  }\n" +
                "  else\n" +
                "  {\n" +
                "    // IE6, IE5 浏览器执行代码\n" +
                "    xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
                "  }\n" +
                "  xmlhttp.onreadystatechange=function()\n" +
                "  {\n" +
                "    if (xmlhttp.readyState==4 && xmlhttp.status==200)\n" +
                "    {\n" +
                "    document.getElementById(\"myDiv\").innerHTML=xmlhttp.responseText;\n" +
                "    }\n" +
                "  }\n" +
                "  xmlhttp.open(\"GET\",\"./alert?id=\" + x,true);\n" +
                "  xmlhttp.send();\n" +
                "}\n" +

                "function getBetweenId()\n" +
                "{\n" +
                "  var x, y;\n" +
                "  x=document.getElementById(\"fromid\").value;\n" +
                "  y=document.getElementById(\"toid\").value;\n" +
                "  var xmlhttp;\n" +
                "  if (window.XMLHttpRequest)\n" +
                "  {\n" +
                "    // IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码\n" +
                "    xmlhttp=new XMLHttpRequest();\n" +
                "  }\n" +
                "  else\n" +
                "  {\n" +
                "    // IE6, IE5 浏览器执行代码\n" +
                "    xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
                "  }\n" +
                "  xmlhttp.onreadystatechange=function()\n" +
                "  {\n" +
                "    if (xmlhttp.readyState==4 && xmlhttp.status==200)\n" +
                "    {\n" +
                "    document.getElementById(\"ids\").innerHTML=xmlhttp.responseText;\n" +
                "    }\n" +
                "  }\n" +
                "  var url=\"./alerts/id?from=\"+x+\"&to=\"+y;\n" +
                "  xmlhttp.open(\"GET\",url,true);\n" +
                "  xmlhttp.send();\n" +
                "}\n" +

                "function getBetweenTime()\n" +
                "{\n" +
                "  var x, y;\n" +
                "  x=document.getElementById(\"fromtime\").value;\n" +
                "  y=document.getElementById(\"totime\").value;\n" +
                "  var xmlhttp;\n" +
                "  if (window.XMLHttpRequest)\n" +
                "  {\n" +
                "    // IE7+, Firefox, Chrome, Opera, Safari 浏览器执行代码\n" +
                "    xmlhttp=new XMLHttpRequest();\n" +
                "  }\n" +
                "  else\n" +
                "  {\n" +
                "    // IE6, IE5 浏览器执行代码\n" +
                "    xmlhttp=new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
                "  }\n" +
                "  xmlhttp.onreadystatechange=function()\n" +
                "  {\n" +
                "    if (xmlhttp.readyState==4 && xmlhttp.status==200)\n" +
                "    {\n" +
                "    document.getElementById(\"times\").innerHTML=xmlhttp.responseText;\n" +
                "    }\n" +
                "  }\n" +
                "  var url=\"./alerts/time?from=\"+x+\"&to=\"+y;\n" +
                "  xmlhttp.open(\"GET\",url,true);\n" +
                "  xmlhttp.send();\n" +
                "}\n" +

                "</script>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "getbyid<input id=\"numb\">\n" +
                "<button type=\"button\" onclick=\"loadXMLDoc()\">submit</button>\n" +
                "<div id=\"myDiv\"></div>\n" +
                "<br/>==================================<br/>"+
                "getbetweenid fromid<input id=\"fromid\"> toid<input id=\"toid\">\n" +
                "<button type=\"button\" onclick=\"getBetweenId()\">submit2</button>\n" +
                "<div id=\"ids\"></div>\n" +
                "<br/>==================================<br/>"+
                "getbetweentime fromtime<input id=\"fromtime\" type=\"datetime-local\" name=\"bdaytime1\"> totime<input id=\"totime\" type=\"datetime-local\" name=\"bdaytime2\">\n" +
                "<button type=\"button\" onclick=\"getBetweenTime()\">submit3</button>\n" +
                "<div id=\"times\"></div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        return htm;
    }

    @GET
    @Path("alert")
    @Produces(MediaType.APPLICATION_JSON)
    public IdsAlert getIdsAlertById(@QueryParam("id") int id) {
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        IdsAlert alert = dao.getIdsAlertById(id);
        return alert;
    }

    @GET
    @Path("alerts/time")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getIdsAlersBetweenTime(@QueryParam("from") String fromTime, @QueryParam("to") String toTime) {
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        List<IdsAlert> alerts = dao.getIdsAlersBetweenTime(fromTime, toTime);
        return alerts;
    }

    @GET
    @Path("alerts/id")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getIdsAlersBetweenId(@QueryParam("from") String fromId, @QueryParam("to") String toId) {
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        List<IdsAlert> alerts = dao.getIdsAlersBetweenId(fromId, toId);
        return alerts;
    }

    @GET
    @Path("alerts/custom/table")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getIdsAlersByTable(@QueryParam("name") String name) {
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        List<IdsAlert> alerts = dao.getIdsAlersByTable(name);
        return alerts;
    }

    @GET
    @Path("alerts/custom")
    @Produces(MediaType.APPLICATION_JSON)
    public List<IdsAlert> getIdsAlersByNameValue(@QueryParam("name") String name, @QueryParam("value") String value) {
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        List<IdsAlert> alerts = dao.getIdsAlersByNameValue(name, value);
        return alerts;
    }
}
