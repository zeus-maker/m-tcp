//package com.marion.server;
//
//import com.marion.server.handler.TcpServerHandler;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.string.StringDecoder;
//import io.netty.handler.codec.string.StringEncoder;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.timeout.IdleStateHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.event.ContextClosedEvent;
//import org.springframework.stereotype.Component;
//
//import java.net.InetSocketAddress;
//
///**
// * 自定义TCP服务器
// * @author Marion
// * @date 2021/10/26 11:03
// */
//@Component
//@Slf4j
//public class TcpServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {
//
//    /**
//     * IP
//     */
//    @Value("${netty.tcp.ip}")
//    private String ip;
//
//    @Value("${netty.tcp.port}")
//    private int port;
//
//    /**
//     * 保存启动的SocketChannel
//     */
//    private Channel serverChannel;
//
//    /**
//     * 注入Application，方便加载所有Bean
//     */
//    private ApplicationContext applicationContext;
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        /**
//         * 1. 创建Bootstrap
//         * 2. 通过ServerBootstrap创建启动服务器类，指定group
//         * 3. 定义channel
//         * 4. 绑定IP和端口
//         * 5. 设置ChannelInitializer
//         */
//        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        NioEventLoopGroup workersGroup = new NioEventLoopGroup();
//
//        try {
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            serverBootstrap.group(bossGroup, workersGroup);
//            serverBootstrap.channel(NioServerSocketChannel.class);
//            serverBootstrap.localAddress(new InetSocketAddress(ip, port));
//            serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
//            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                protected void initChannel(SocketChannel socketChannel) throws Exception {
//                    ChannelPipeline pipeline = socketChannel.pipeline();
//                    pipeline.addLast(new IdleStateHandler(30, 0, 0));
//                    pipeline.addLast(new StringEncoder());
//                    pipeline.addLast(new StringDecoder());
//                    pipeline.addLast(applicationContext.getBean(TcpServerHandler.class));
//                }
//            });
//
//
//            Channel channel = serverBootstrap.bind().sync().channel();
//
//            log.info("[TcpBootRunner] application server start ....");
//
//            this.serverChannel = channel;
//            channel.closeFuture().sync();
//        } finally {
//            /**
//             * 调用shutdownGracefully()方法只要满足下列任一条件既能从循环跳出：
//             *
//             * 1.执行完普通任务且静默时间为0
//             * 2.没有普通任务，执行完shutdownHook任务且静默时间为0
//             * 3.静默期间没有任务提交
//             * 4.优雅关闭截至时间已到
//             *
//             * 我们可以将静默时间看作为一段观察期，在此期间如果没有任务执行，说明可以跳出循环；如果此期间有任务执行，
//             * 执行完后立即进入下一个观察期继续观察；如果连续多个观察期一直有任务执行，那么截止时间到则跳出循环。
//             *
//             */
//            bossGroup.shutdownGracefully();
//            workersGroup.shutdownGracefully();
//
//            log.info("[TcpBootRunner] thread end ....");
//        }
//
//    }
//
//    @Override
//    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
//        if (serverChannel != null) {
//            serverChannel.close();
//        }
//        log.info("[TcpBootRunner] context close ....");
//    }
//
//}
