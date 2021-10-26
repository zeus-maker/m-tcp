package com.marion.client.handler;


import com.marion.client.enums.TCPCode;
import com.marion.client.protocol.TcpProtocol;
import com.marion.client.utils.JsonUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Marion
 * @date 2021/10/26 11:22
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class TcpClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        log.info("[channelRead0] " + s);
        TcpProtocol tcpProtocol = JsonUtils.fromString(s, TcpProtocol.class);
        if (null != tcpProtocol) {
            log.info(s);
        }
    }

    /**
     * 当通道激活完成后，Netty会调用fireChannelActive()方法，触发通道激活事件，
     * 而在通道流水线注册过的入站处理器的channelActive()回调方法会被调用。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("[channelActive] " + ctx.name());
    }

    /**
     * 当连接被断开或者不可用时，Netty会调用fireChannelInactive()方法，
     * 触发连接不可用事件，而在通道流水线注册过的入站处理器的channelInactive()回调方法会被调用。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("[channelInactive] " + ctx.name());
        /**
         * 服务器断线重连
         */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.info("[exceptionCaught] " + ctx.name());
        ctx.close();
    }

}