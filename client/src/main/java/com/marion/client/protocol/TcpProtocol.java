package com.marion.client.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义TCP协议
 * @author Marion
 * @date 2021/10/26 17:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TcpProtocol {

    /**
     *      {
     *      "code": 200-成功，400-失败",
     *      "method", "className#methodName",
     *      "parameter": "id=1&name=张三",
     *      "body": "{"data": false}"
     *      }
     */

    private int code;

    private String method;

    private String parameter;

    private Object body;

}
