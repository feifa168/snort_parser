package com.ids.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SqlSessionBuild {
    public static SqlSession createSqlSession(String config) throws IOException {
        // 得到配置文件流
        InputStream inputStream = Resources.getResourceAsStream(config);
        // 创建会话工厂，传入mybatis的配置文件信息
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 通过工厂得到SqlSession
        //sqlSessionFactory.getConfiguration().addMapper(IdsAlertInterface.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession;
    }
}
