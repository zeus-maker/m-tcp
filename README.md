## 需求一、基于Netty自定义TCP协议

### 1. 概述
1. 需要熟悉Netty核心组件，TCP/IP协议相关知识
2. 基于Netty开发自定义TCP协议完成客户端与服务端通信
2. 解决TCP接收端粘包和半包问题
3. 自定义协议解析，编解码器，编解码处理器
4. 单机长连接支持百万连接
5. 支持服务器广播下发消息，所有正在连接的客户端可以收到
6. 支持服务端单点消息，发送给指定客户端
7. 服务器重启支持百万连接重新连接，并不影响服务器稳定性
8. 客户端长连接支持断线重连
9. 服务器心跳检测客户端是否存活，并移除
10. 可查询在线长连接数量
11. 用SpringBoot集成Netty实现

### 2. 需求分析
1. SpringBoot集成Netty，在启动的生命周期中启动TCP服务器
2. 定义配置

### 3. 流程设计
#### 3.1 服务器端
1. SpringBoot集成Netty，在启动的生命周期中启动TCP服务器
2. 定义Reactor模型，boss和woker
2. 自定义编解码，测试并解决粘包/半包问题
3. 自定义入站处理器，集成SimpleChannelInboundHandler，不返回消息
4. 启动TCP服务器，将启动对象保存，在Sprintboot停止的时候关闭channel
5. 服务器代码如下：
```
package io.springboot.netty.tcp;

/**
 * 自定义TCP服务器
 * @author Marion
 * @date 2021/10/26 11:03
 */
@Component
@Slf4j
public class TcpBootRunner implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

    /**
     * IP
     */
    @Value("${netty.tcp.ip}")
    private String ip;

    @Value("${netty.tcp.port}")
    private int port;

    /**
     * 保存启动的SocketChannel
     */
    private Channel serverChannel;

    /**
     * 注入Application，方便加载所有Bean
     */
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /**
         * 1. 创建Bootstrap
         * 2. 通过ServerBootstrap创建启动服务器类，指定group
         * 3. 定义channel
         * 4. 绑定IP和端口
         * 5. 设置ChannelInitializer
         */
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workersGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workersGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(ip, port));
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(new StringDecoder());
                    pipeline.addLast(applicationContext.getBean(MyTcpServiceHandler.class));
                }
            });

            Channel channel = serverBootstrap.bind().sync().channel();

            log.info("[TcpBootRunner] application server start ....");

            this.serverChannel = channel;
            channel.closeFuture().sync();
        } finally {
            /**
             * 调用shutdownGracefully()方法只要满足下列任一条件既能从循环跳出：
             *
             * 1.执行完普通任务且静默时间为0
             * 2.没有普通任务，执行完shutdownHook任务且静默时间为0
             * 3.静默期间没有任务提交
             * 4.优雅关闭截至时间已到
             *
             * 我们可以将静默时间看作为一段观察期，在此期间如果没有任务执行，说明可以跳出循环；如果此期间有任务执行，
             * 执行完后立即进入下一个观察期继续观察；如果连续多个观察期一直有任务执行，那么截止时间到则跳出循环。
             *
             */
            bossGroup.shutdownGracefully();
            workersGroup.shutdownGracefully();

            log.info("[TcpBootRunner] thread end ....");
        }

    }

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        if (serverChannel != null) {
            serverChannel.close();
        }
        log.info("[TcpBootRunner] context close ....");
    }

}

```
6. 保存连接的客户端到ChannelGroup，同时保存Map的映射，用户查询指定客户端
7. 定义JSON字符串协议，调用本地方法
```
// 请求协议
{
    "code": 200-成功，400-失败",
    "method", "className#methodName",
    "parameter": "id=1&name=张三",
    "body": "{"data": false}"
}
```

#### 3.2 客户端
1. 自定义编解码，测试并解决粘包/半包问题
2. 自定义入站处理器，集成SimpleChannelInboundHandler，不返回消息
3. 定义客户端
```
/**
 * @author Marion
 * @date 2021/10/26 19:23
 */
@Component
@Slf4j
public class TcpClient {

    public void connect(String ip, int port) {

        NioEventLoopGroup workers = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workers);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.handler(new LoggingHandler(LogLevel.INFO));
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(new StringDecoder());
                    pipeline.addLast(new TcpClientHandler());
                }
            });

            ChannelFuture sync = bootstrap.connect(ip, port).sync();
            log.info("client start...");

            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("[TcpClient] interrupted e={}", e);
        }  finally {
            workers.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                TcpClient tcpClient = new TcpClient();
                tcpClient.connect("127.0.0.1", 6000);
            }).start();
        }
    }
}

```
4. 启动客户端，监听服务器消息

#### 3.3 测试
1. 循环发送消息，测试粘包/半包
2. 最大连接数
3. 断线检测
4. 自动重连

### 4. 数据库设计

### 5. 接口设计与文档

### 6. 后台管理

### 7. 透传消息

### 8. 定时任务

### 9. 高并发高可用、易维护性、可扩展性

### 10. 技术难点、优化及其他

### 11. 时间评估

### 12. 测试说明

### 13. 复盘