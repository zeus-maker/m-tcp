package com.marion.server.controller;

import com.marion.server.protocol.TcpProtocol;
import com.marion.server.service.ChannelService;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Marion
 * @date 2021/10/26 17:28
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ChannelService channelService;

    @GetMapping("/channels")
    public List<Channel> listChannels() {
        return channelService.listAllChannel();
    }

    @GetMapping("/channels/count")
    public Object countChannels() {
        return channelService.listAllChannel().size();
    }

    @GetMapping("/publish")
    public Boolean publish(@RequestParam String message) {
        channelService.publishAll(message);
        return true;
    }

    @GetMapping("/rpc")
    public Boolean rpc(@RequestParam String method,
                       @RequestParam String parameter) {
        channelService.publishAll(method, parameter);
        return true;
    }

}
