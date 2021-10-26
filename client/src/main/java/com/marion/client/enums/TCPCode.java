package com.marion.client.enums;

import lombok.Getter;

/**
 * @author Marion
 * @date 2021/10/26 17:56
 */
public enum TCPCode {

    /**
     * 类型
     */
    SUCCESS(200),
    BAD_REQUEST(400);

    @Getter
    private int value;

    TCPCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
