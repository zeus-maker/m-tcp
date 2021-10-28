package com.marion.server;

import com.marion.server.handler.UDPServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * 自定义TCP服务器
 * @author Marion
 * @date 2021/10/26 11:03
 */
@Component
@Slf4j
public class UdpServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

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
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootStrap = new Bootstrap();
            bootStrap.group(group)
                    // 指定传输数据包，可支持UDP
                    .channel(NioDatagramChannel.class)
                    // 广播模式
                    .option(ChannelOption.SO_BROADCAST, true)
                    // 线程池复用缓冲区
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(applicationContext.getBean(UDPServerHandler.class));
            Channel channel = bootStrap.bind(ip, port).sync().channel();

            log.info("[UdpServer] application server start ....");

            this.serverChannel = channel;
            channel.closeFuture().await();
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
            group.shutdownGracefully();

            log.info("[UdpServer] thread end ....");
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
