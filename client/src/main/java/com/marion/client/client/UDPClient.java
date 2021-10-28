package com.marion.client.client;

import com.marion.client.handler.UDPClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * 启动一个UDP的客户端并且发送消息测试
 * @author Marion
 * @date 2021/10/26 19:23
 */
@Component
@Slf4j
public class UDPClient {

    public void send(String message, String ip, int port) {

        NioEventLoopGroup workers = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workers);
            bootstrap.channel(NioDatagramChannel.class);
            bootstrap.option(ChannelOption.SO_BROADCAST, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                protected void initChannel(NioDatagramChannel socketChannel) {
                    socketChannel.pipeline().addLast(new UDPClientHandler());
                }
            });

            Channel channel = bootstrap.bind(0).sync().channel();

            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                    new InetSocketAddress(ip, port))).sync();

            // 10s超时
            channel.closeFuture().sync().await(10000);

            ChannelFuture sync = bootstrap.connect(ip, port).sync();
            log.info("client start...");

            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("[TcpClient] interrupted e={}", e);
        } finally {
            workers.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            Executors.newFixedThreadPool(10).submit(() -> {
                UDPClient tcpClient = new UDPClient();
                tcpClient.send("123", "127.0.0.1", 6000);
            });
        }
    }
}
