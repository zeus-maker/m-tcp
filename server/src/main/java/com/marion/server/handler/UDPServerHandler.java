package com.marion.server.handler;


import com.marion.server.service.ChannelService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Marion
 * @date 2021/10/26 11:22
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Autowired
    private ChannelService channelService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        log.info("[channelRead0] {}", packet);

        // 读取收到的数据
        ByteBuf buf = (ByteBuf) packet.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, CharsetUtil.UTF_8);
        System.out.println("【NOTE】>>>>>> 收到客户端的数据：" + body);
        // 回复一条信息给客户端
        ctx.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer("Hello，我是Server，我的时间戳是" + System.currentTimeMillis()
                        , CharsetUtil.UTF_8)
                , packet.sender())).sync();
    }

    /**
     * 当通道激活完成后，Netty会调用fireChannelActive()方法，触发通道激活事件，
     * 而在通道流水线注册过的入站处理器的channelActive()回调方法会被调用。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("[channelActive] " + ctx.name());
        channelService.addChannel(ctx.channel());
    }

    /**
     * 当连接被断开或者不可用时，Netty会调用fireChannelInactive()方法，
     * 触发连接不可用事件，而在通道流水线注册过的入站处理器的channelInactive()回调方法会被调用。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("[channelInactive] " + ctx.name());
        channelService.removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.info("[exceptionCaught] " + ctx.name());
        channelService.removeChannel(ctx.channel());
        ctx.close();
    }

}