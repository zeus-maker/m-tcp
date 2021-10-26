package com.marion.server.service;

import com.marion.server.enums.TCPCode;
import com.marion.server.protocol.TcpProtocol;
import com.marion.server.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户长连接通道
 * 1. 添加channel
 * 2. 删除channel
 * 3. 查找channel
 * 4. 广播消息
 *
 * @author Marion
 * @date 2021/10/26 17:15
 */
@Service
@Slf4j
public class ChannelService {

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    public void addChannel(Channel channel) {
        channels.add(channel);
        channelMap.put(channel.id().asShortText(), channel);
        log.info("[addChannel] channel map, key={}", channel.id().asShortText());
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel);
        channelMap.remove(channel.id().asShortText());
        log.info("[removeChannel] channel remove, key={}", channel.id().asShortText());
    }

    public Channel findChannel(String id) {
        return channelMap.get(id);
    }

    public void publishAll(Object message) {
        channels.writeAndFlush(message);
        log.info("[publishAll] publish message={}", message);
    }

    /**
     * 查询所有在线channel
     */
    public List<Channel> listAllChannel() {
        return new ArrayList<>(channels);
    }

    /**
     * 广播消息
     */
    public void publishAll(String method, String parameter) {
        /**
         * 1. 解析方法和参数，获取返回值
         */
        TcpProtocol protocol = TcpProtocol.builder()
                .code(TCPCode.SUCCESS.getValue())
                .method(method)
                .parameter(parameter)
                .build();

        publishAll(JsonUtils.toString(protocol));
    }
}
