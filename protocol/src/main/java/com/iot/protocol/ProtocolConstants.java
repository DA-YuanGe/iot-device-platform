package com.iot.protocol;

/**
 * IoT设备通信协议常量。
 *
 * 协议帧格式：
 *
 * +--------+---------+------+--------+---------+-------+
 * | Magic  | Version | Type | Length | Payload | CRC16 |
 * +--------+---------+------+--------+---------+-------+
 * | 2 Byte | 1 Byte  | 1 B  | 2 Byte | N Byte  | 2 B   |
 * +--------+---------+------+--------+---------+-------+
 */
public final class ProtocolConstants {

    private ProtocolConstants() {
    }

    /**
     * 协议魔数，用于识别一个合法的数据帧。
     */
    public static final short MAGIC = (short) 0xAA55;

    /**
     * 当前协议版本。
     */
    public static final byte VERSION = 0x01;

    /**
     * 协议头长度：
     * Magic(2) + Version(1) + Type(1) + Length(2)
     */
    public static final int HEADER_LENGTH = 6;

    /**
     * CRC16长度。
     */
    public static final int CRC_LENGTH = 2;

    /**
     * 最大Payload长度，第一版暂定4KB。
     */
    public static final int MAX_PAYLOAD_LENGTH = 4096;

    /**
     * 最小完整帧长度：
     * Header + CRC。
     */
    public static final int MIN_FRAME_LENGTH =
            HEADER_LENGTH + CRC_LENGTH;
}
