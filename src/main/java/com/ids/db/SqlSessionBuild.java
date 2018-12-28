package com.ids.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SqlSessionBuild {
    public static SqlSession createSqlSession(String config) throws IOException {
        // 得到配置文件流
        InputStream inputStream = null;
        // getResourceAsStream使用classpath下的路径，mybatis设置为resource="config.xml"，
        // 而FileInputStream使用本地文件，mybatis设置为相对路径url="file:./config.xml"
        //inputStream = Resources.getResourceAsStream(config);
        inputStream = new FileInputStream(config);
        // 创建会话工厂，传入mybatis的配置文件信息
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 通过工厂得到SqlSession
        //sqlSessionFactory.getConfiguration().addMapper(IdsAlertInterface.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession;
    }
}
