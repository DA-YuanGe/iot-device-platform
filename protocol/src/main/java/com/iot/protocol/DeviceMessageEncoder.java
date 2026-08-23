package com.iot.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * IoT设备协议编码器。
 *
 * 将 DeviceMessage 编码成二进制协议帧。
 */
public class DeviceMessageEncoder
        extends MessageToByteEncoder<DeviceMessage> {

    @Override
    protected void encode(
            ChannelHandlerContext ctx,
            DeviceMessage message,
            ByteBuf out) {

        byte[] payload = message.getPayload();

        if (payload == null) {
            payload = new byte[0];
        }

        if (payload.length > ProtocolConstants.MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException(
                    "Payload too large: " + payload.length
            );
        }

        /*
         * Header
         */
        out.writeShort(ProtocolConstants.MAGIC);
        out.writeByte(message.getVersion());
        out.writeByte(message.getType().getCode());
        out.writeShort(payload.length);

        /*
         * Payload
         */
        out.writeBytes(payload);

        /*
         * CRC16覆盖：
         *
         * Version
         * Type
         * Length
         * Payload
         *
         * 不包含 Magic 和 CRC 本身。
         */
        int crcStartIndex = out.writerIndex()
                - payload.length
                - 4;

        int crcLength = 4 + payload.length;

        int crc = Crc16.calculate(
                readBytes(out, crcStartIndex, crcLength)
        );

        out.writeShort(crc);
    }

    private byte[] readBytes(
            ByteBuf buffer,
            int index,
            int length) {

        byte[] bytes = new byte[length];

        buffer.getBytes(index, bytes);

        return bytes;
    }
}
