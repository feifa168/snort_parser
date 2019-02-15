使用说明
三个部分，一是采集程序，二是数据库，三是服务程序。

支持环境
采集端: Centos7X64
服务端: Centos7X64, Win7-Win10X64, Server2008X64
依赖 JDK1.8

重点强调 采集端配置的syslog发送端口要与服务器配置的接收端口一致，默认为514，若修改两端必须要一致，目前仅支持UDP，不支持TCP。

1. 安装配置
1.1 数据库配置
a.mysql安装配置
b.在mysql所在的机器上导入 snort_parser_result.sql
    
1.2 采集程序安装配置
a.拷贝程序到目标机器任意目录
b.解压
    tar zxvf ids.tar.gz
c.切换到目录ids
    cd ids/install
d.修改run.xml，修改 /run/params[@type=syslog]/param/items/item/中的项，其中ip要改为服务程序的ip地址，根据实际情况配置告警模块以及告警级别，目前仅处理UDP协议。
<run>
    <params>
        <param type="syslog">
            <items>
                <item>
                    <!-- * | auth | authpriv | daemon | user | local0 | local1 | local2 | local3 | local4 | local5 | local6 | local7 -->
                    <facility>*</facility>
                    <!-- * | emerg | alert | crit | err | warning | notice | info | debug -->
                    <level>*</level>
                    <proto>udp</proto>
                    <ip>172.16.39.21</ip>
                    <port>514</port>
                </item>
            </items>
        </param>
    </params>
</run>
e.修改执行权限
    chmod +x install.sh
f.执行安装命令，网卡就是待监测流量的网卡，可能有多个网卡，可以通过ifconfig查看网卡，选择需要监测的那个。
    ./install.sh 网卡
g.执行结束后会做以下修改，如果没有执行成功，请确认哪个步骤出错。
1) 检查rsyslog是否运行，执行 systemctl status rsyslog
2) 检查/etc/ld.so.conf文件是否包含 run.xml中的/run/params[@type=so]/items/item对应的值，默认为/usr/local/lib64
3) /etc/rc.d/init.d/下是否有 网卡接口.sh
4) 是否存在目录 /var/log/snort

1.3 服务程序安装配置
a.拷贝程序到目标机器
b.解压snort_parser.tar.gz
c.切换到目录
    cd snort_parser\snort_parser
d.修改配置文件 syslog.xml，修改syslog接收端口号，默认为514，可以修改为其他端口以免与默认的冲突，该端口要与采集端端口一致，目前只支持UDP，不支持TCP
    <syslog>
        <servers>
            <server>
                <!-- udp | tcp -->
                <protocol>udp</protocol>
                <!-- default port is 514 -->
                <port>514</port>
            </server>
        </servers>
    </syslog>

receivers下面是转换后的数据包装成自定义格式的syslog接收服务端，请根据实际情况修改ip和端口
    <syslog>
        <receivers>
            <server>
                <!-- udp | tcp -->
                <protocol>udp</protocol>
                <host>172.16.39.111</host>
                <!-- default port is 514 -->
                <port>514</port>
            </server>
        </receivers>
    </syslog>

sensor用于配置发送的syslog日志格式，name要根据实际情况修改，其他可以默认
    <syslog>
        <sensor>
            <!-- 格式：数据类型_单位编号_设备类型_流水号；如rz_xxxxxx_xxxxxx_00001 -->
            <name>rz_grxa_mysqlserver_00001</name>
            <!-- true表示使用本机ip，false表示使用日志源的ip -->
            <uselocalip>false</uselocalip>
            <source>GRXA</source>
            <type>syslog</type>
            <delimiter>^</delimiter>
            <tag>idsparser</tag>
        </sensor>
    </syslog>

e.修改配置文件 server.xml，修改<baseurl>http://hostip:port</baseurl> 为本机实际ip并指定端口号，不能指定localhost和127.0.0.1
<rest>
    <server>
        <baseurl>http://172.16.39.21:8888</baseurl>
    </server>
</rest>
f.修改config.properties，修改url中的hostip，port，databasename，修改username和password
    jdbc.driver = com.mysql.jdbc.Driver
    jdbc.url = jdbc:mysql://hostip:port/databasename?characterEncoding=UTF-8
    jdbc.username = user
    jdbc.password = passswd
g.执行安装命令
Linux安装
1) 进入install目录
    cd ..\install
2) Linux需要增加install.sh的可执行权限
    chmod +x install.sh
3) 执行命令 ./install.sh
Windows安装
1) 执行命令 install.bat
2) 防火墙设置允许通过的配置的端口号，参见service.xml，默认是8888

3. 运行程序
3.1 服务程序端
a.启动服务
    Linux下需要修改执行权限，执行./ids_server_run.sh
    windows下执行ids_server_run.bat
b.停止服务
    输入exit

3.2 采集端
a.第一次启动需执行/etc/rc.d/init.d/网卡接口.sh，已设置开机自启动，后续不用手动启动

4. 停止与卸载
4.1 停止监测
    # ps -A | grep snort
    529 pts/1    00:00:00 snort
    # kill 进程id，这里是529
4.2 停止服务
删除/etc/rc.d/init.d/网卡.sh，这个网卡.sh是安装时写入的，删除即可。
