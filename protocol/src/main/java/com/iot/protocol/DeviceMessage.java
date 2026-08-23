package com.iot.protocol;

import java.util.Arrays;

/**
 * IoT设备协议消息。
 */
public class DeviceMessage {

    /**
     * 协议版本。
     */
    private byte version;

    /**
     * 消息类型。
     */
    private MessageType type;

    /**
     * 消息Payload。
     */
    private byte[] payload;

    public DeviceMessage() {
    }

    public DeviceMessage(MessageType type, byte[] payload) {
        this(ProtocolConstants.VERSION, type, payload);
    }

    public DeviceMessage(
            byte version,
            MessageType type,
            byte[] payload) {

        this.version = version;
        this.type = type;
        this.payload = payload;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DeviceMessage{" +
                "version=" + version +
                ", type=" + type +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
