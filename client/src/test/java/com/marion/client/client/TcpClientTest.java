package com.marion.client.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Marion
 * @date 2021/10/26 19:57
 */
@SpringBootTest
class TcpClientTest {

    @Autowired
    private TcpClient tcpClient;

    @Test
    void connect() {

        tcpClient.connect("127.0.0.1", 6000);

    }
}