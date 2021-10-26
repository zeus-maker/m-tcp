package com.marion.client.client;

import com.marion.client.handler.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

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
