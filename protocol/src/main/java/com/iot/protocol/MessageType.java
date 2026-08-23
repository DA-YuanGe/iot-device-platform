package com.iot.protocol;

/**
 * 设备消息类型。
 */
public enum MessageType {

    /**
     * 设备登录。
     */
    LOGIN(0x01),

    /**
     * 设备心跳。
     */
    HEARTBEAT(0x02),

    /**
     * 设备遥测数据。
     */
    TELEMETRY(0x03),

    /**
     * 服务端确认。
     */
    ACK(0x04);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据协议中的类型编码查找消息类型。
     */
    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Unknown message type: " + code
        );
    }
}
