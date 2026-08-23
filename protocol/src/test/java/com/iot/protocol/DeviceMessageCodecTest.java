package com.iot.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DeviceMessageCodecTest {

    @Test
    void shouldEncodeAndDecodeMessage() {

        EmbeddedChannel channel = new EmbeddedChannel(
                new DeviceMessageEncoder(),
                new DeviceMessageDecoder()
        );

        byte[] payload =
                "DEVICE-001".getBytes(StandardCharsets.UTF_8);

        DeviceMessage original =
                new DeviceMessage(
                        MessageType.HEARTBEAT,
                        payload
                );

        assertTrue(channel.writeOutbound(original));

        ByteBuf encoded =
                channel.readOutbound();

        assertNotNull(encoded);

        assertTrue(channel.writeInbound(encoded));

        DeviceMessage decoded =
                channel.readInbound();

        assertNotNull(decoded);
        assertEquals(
                MessageType.HEARTBEAT,
                decoded.getType()
        );

        assertArrayEquals(
                payload,
                decoded.getPayload()
        );

        assertEquals(
                ProtocolConstants.VERSION,
                decoded.getVersion()
        );

        channel.finish();
    }

    @Test
    void shouldDecodeMultipleMessages() {

        EmbeddedChannel channel = new EmbeddedChannel(
                new DeviceMessageDecoder()
        );

        byte[] payload1 =
                "DEVICE-001".getBytes(StandardCharsets.UTF_8);

        byte[] payload2 =
                "DEVICE-002".getBytes(StandardCharsets.UTF_8);

        ByteBuf buffer = Unpooled.buffer();

        EmbeddedChannel encoder =
                new EmbeddedChannel(
                        new DeviceMessageEncoder()
                );

        encoder.writeOutbound(
                new DeviceMessage(
                        MessageType.HEARTBEAT,
                        payload1
                )
        );

        ByteBuf frame1 = encoder.readOutbound();

        encoder.writeOutbound(
                new DeviceMessage(
                        MessageType.TELEMETRY,
                        payload2
                )
        );

        ByteBuf frame2 = encoder.readOutbound();

        buffer.writeBytes(frame1);
        buffer.writeBytes(frame2);

        assertTrue(channel.writeInbound(buffer));

        DeviceMessage message1 =
                channel.readInbound();

        DeviceMessage message2 =
                channel.readInbound();

        assertNotNull(message1);
        assertNotNull(message2);

        assertEquals(
                MessageType.HEARTBEAT,
                message1.getType()
        );

        assertEquals(
                MessageType.TELEMETRY,
                message2.getType()
        );

        channel.finish();
        encoder.finish();
    }
}
