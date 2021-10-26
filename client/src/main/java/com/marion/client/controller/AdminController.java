package com.marion.client.controller;

import com.marion.client.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Marion
 * @date 2021/10/26 17:28
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ChannelService channelService;

}
