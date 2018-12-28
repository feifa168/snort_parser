package ids;

import com.ids.beans.IdsAlert;
import com.ids.dao.IdsAlertInterface;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestMybatis {

    public SqlSession sqlSession = null;

    public void init() throws IOException {
        // mybatis配置文件，这个地方的root地址为：resources，路径要对。
        String resource = "mybatis-config.xml";
        // 得到配置文件流
        InputStream inputStream = null;
        String urlXml = System.getProperty("user.dir")+System.getProperty("file.separator")+resource;
        inputStream = new FileInputStream(urlXml);
        //inputStream = Resources.getResourceAsStream(resource);
        // 创建会话工厂，传入mybatis的配置文件信息
        SqlSessionFactory sqlSessionFactory = null;
        //sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        // 通过工厂得到SqlSession
        //sqlSessionFactory.getConfiguration().addMapper(IdsAlertInterface.class);
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void testGetIdsAlertById() {
        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        IdsAlert alert = dao.getIdsAlertById(921);
        System.out.println(alert.toString());
    }

    @Test
    public void testInsertIdsAlert() {
        try {
            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IdsAlertInterface dao = sqlSession.getMapper(IdsAlertInterface.class);
        IdsAlert alert = new IdsAlert( 8,"2018-11-02 17:25:43", 139, "172.16.5.37", "snort", 4, 8, 2, "this is for test", 3, "TCP", "sip", 30, false, "dip", 555);
        dao.putIdsAlert(alert);
        System.out.println("insert is " + alert.toString());
    }
}
