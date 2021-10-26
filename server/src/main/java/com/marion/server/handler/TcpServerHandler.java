package com.marion.server.handler;


import com.marion.server.service.ChannelService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;
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
public class TcpServerHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private ChannelService channelService;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        log.info("[channelRead0] " + s);
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    log.info("[userEventTriggered] READER_IDLE evt={}", evt);
                    ctx.channel().close();
                    break;
                case WRITER_IDLE:
                    break;
                case ALL_IDLE:
                    break;
                default:
                    break;
            }
        }
        log.info("[userEventTriggered] evt={}", evt);
    }
}