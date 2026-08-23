package com.iot.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * IoT设备协议解码器。
 *
 * 负责：
 * 1. 查找协议头 Magic
 * 2. 判断数据是否完整
 * 3. 根据 Length 判断完整帧
 * 4. CRC16 校验
 * 5. 将二进制数据转换成 DeviceMessage
 *
 * 协议帧：
 *
 * +--------+---------+------+--------+---------+-------+
 * | Magic  | Version | Type | Length | Payload | CRC16 |
 * +--------+---------+------+--------+---------+-------+
 * | 2 Byte | 1 Byte  | 1 B  | 2 Byte | N Byte  | 2 B   |
 * +--------+---------+------+--------+---------+-------+
 */
public class DeviceMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(
            ChannelHandlerContext ctx,
            ByteBuf in,
            List<Object> out) {

        /*
         * TCP 是字节流协议。
         *
         * 一次收到的数据：
         * 可能是一半消息
         * 可能是一条完整消息
         * 也可能是多条消息
         *
         * 所以这里不能假设一次 read 就是一条完整消息。
         */

        // 至少需要完整协议头 + CRC
        if (in.readableBytes() < ProtocolConstants.MIN_FRAME_LENGTH) {
            return;
        }

        /*
         * 第一步：寻找 Magic。
         *
         * 防止前面存在无效数据。
         */
        int startIndex = findMagic(in);

        if (startIndex < 0) {
            /*
             * 没找到 Magic。
             *
             * 保留最后一个字节，因为它可能是
             * AA，下一次数据过来以后组成 AA 55。
             */
            int readable = in.readableBytes();

            if (readable > 1) {
                in.skipBytes(readable - 1);
            }

            return;
        }

        /*
         * 丢弃 Magic 前面的无效数据。
         */
        if (startIndex > in.readerIndex()) {
            in.skipBytes(startIndex - in.readerIndex());
        }

        /*
         * 此时已经定位到 Magic。
         *
         * 再次确认数据长度。
         */
        if (in.readableBytes() < ProtocolConstants.HEADER_LENGTH) {
            return;
        }

        /*
         * 查看协议头。
         *
         * 使用 getXXX 而不是 readXXX，
         * 避免数据不完整时移动 readerIndex。
         */
        int magic = in.getUnsignedShort(in.readerIndex());

        if (magic != (ProtocolConstants.MAGIC & 0xFFFF)) {
            in.skipBytes(1);
            return;
        }

        int version = in.getUnsignedByte(in.readerIndex() + 2);
        int typeCode = in.getUnsignedByte(in.readerIndex() + 3);
        int payloadLength = in.getUnsignedShort(in.readerIndex() + 4);

        /*
         * 防止恶意/异常设备发送超大数据。
         */
        if (payloadLength > ProtocolConstants.MAX_PAYLOAD_LENGTH) {
            in.skipBytes(2);
            return;
        }

        /*
         * 完整帧长度：
         *
         * Header + Payload + CRC
         */
        int frameLength =
                ProtocolConstants.HEADER_LENGTH
                        + payloadLength
                        + ProtocolConstants.CRC_LENGTH;

        /*
         * 半包处理：
         *
         * 数据还没收完整。
         *
         * 不能清理 readerIndex，
         * 等下一次 TCP 数据到达。
         */
        if (in.readableBytes() < frameLength) {
            return;
        }

        /*
         * 现在确认是一条完整消息。
         */

        // Magic
        in.skipBytes(2);

        // Version
        byte messageVersion = in.readByte();

        // Type
        int messageTypeCode = in.readUnsignedByte();

        MessageType messageType;

        try {
            messageType = MessageType.fromCode(messageTypeCode);
        } catch (IllegalArgumentException e) {
            /*
             * 未知消息类型。
             */
            in.skipBytes(payloadLength + 2);
            return;
        }

        // Length
        int length = in.readUnsignedShort();

        // Payload
        byte[] payload = new byte[length];
        in.readBytes(payload);

        // CRC
        int receivedCrc = in.readUnsignedShort();

        /*
         * CRC 校验数据：
         *
         * Version
         * Type
         * Length
         * Payload
         */
        ByteBuf crcBuffer = ctx.alloc().buffer(4 + payload.length);

        try {
            crcBuffer.writeByte(messageVersion);
            crcBuffer.writeByte(messageTypeCode);
            crcBuffer.writeShort(length);
            crcBuffer.writeBytes(payload);

            byte[] crcData = new byte[crcBuffer.readableBytes()];
            crcBuffer.getBytes(
                    crcBuffer.readerIndex(),
                    crcData
            );

            int calculatedCrc = Crc16.calculate(crcData);

            if (receivedCrc != calculatedCrc) {
                System.err.println(
                        "[Protocol] CRC check failed. "
                                + "received="
                                + String.format("%04X", receivedCrc)
                                + ", calculated="
                                + String.format("%04X", calculatedCrc)
                );
                return;
            }

        } finally {
            crcBuffer.release();
        }

        /*
         * 构造 Java 消息对象。
         */
        DeviceMessage message =
                new DeviceMessage(
                        messageVersion,
                        messageType,
                        payload
                );

        /*
         * 交给 Netty Pipeline 后面的 Handler。
         */
        out.add(message);
    }

    /**
     * 查找 Magic。
     */
    private int findMagic(ByteBuf buffer) {

        int readerIndex = buffer.readerIndex();
        int writerIndex = buffer.writerIndex();

        for (int i = readerIndex; i < writerIndex - 1; i++) {

            int current = buffer.getUnsignedByte(i);
            int next = buffer.getUnsignedByte(i + 1);

            if (current == 0xAA && next == 0x55) {
                return i;
            }
        }

        return -1;
    }
}
