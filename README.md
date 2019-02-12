## 简介
> snort_parser用于二次解析snort分析结果，通过mybatis~~或JDBC~~写入数据库，提供restful访问接口。

## 依赖
* jersey-server，用于提供restful服务。
* jersey-media-json-jackson，用于解析jersey的json类型注解。
* jersey-container-grizzly2-http，用于提供servlet。
* mybatis，用于操作数据库。
* commons-beanutils，用于字符操作。
* mysql-connector-java，用于操作mysql数据库。
* dom4j，用于解析xml。
* jaxen，用于支持xpath操作。
* junit，用于测试。
* netty，用于发送syslog日志

## 模块
* syslogServer，接收514端口的UDP数据，这里用于接收syslog格式的日志。
* syslog日志解析，这里只支持snort告警日志格式。
* syslog.client，用于发送syslog格式的日志。
* dao模块，用于操作Mysql数据库，有原生态的jdbc操作，也有mybatis操作，推荐mybatis操作，有时间可以改成spring+mybatis。
* restful服务器，通过jersey+grizzly2提供支持查询解析后的日志。

## 知识点
* 线程池，使用原生态的线程池，根据CPU核数设置线程池核心数和最大数，来不及处理的任务存入阻塞队列，pool.execute将任务提交给线程池。线程池有好几种执行策略，这里不做深入分析，有时间专门写个线程池的用法。
```java
        final int processors = Runtime.getRuntime().availableProcessors();
        final int corePoolSize = processors + 1;
        final int maximumPoolSize corePoolSize*2;
        final int queueSize = 5;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(queueSize), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy()) {

            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                System.out.println("准备执行：" + ((AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>) r).getName());
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                System.out.println("执行完成： " + ((AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>) r).getName());
            }
            @Override
            protected void terminated() {
                System.out.println("线程池退出");
            }
        };
        
        pool.execute(new AlertTaskImpl<IdsSyslogParser, IdsAlertInterface>("task"+(i+1), log));
```
* 并发队列，使用ConcurrentLinkedDeque，适用于并发非阻塞操作，并发队列核心是使用atomi系列操作。
* syslog，既是一种协议，又是一种日志格式，常用于日志记录。
* restful，一种实现了rest风格的操作，简化web服务器的开发，风格更好。
* 数据库操作，java使用jdbc封装支持各种数据库，但jdbc比较基础，很原始，有很多框架可以更加友好以及方便的支持该操作，比如hibernate,mybatis,spring。
* 正则表达式，java中随处可见正则表达式，而且支持组名查找
* netty，高性能网络开发框架

## 为什么用java
* JDK本身封装了很多实用的库，较之c++方便很多。
* 很多框架，功能也很强大，方便。
* 支持反射，在反射的基础上，才有了这众多的框架。
* 设计模式，java语言本身以及框架隐含了很多好的设计理念。
* 灵活，简单。
* 丰富且强大的生态系统，是其他语言所没有的。
