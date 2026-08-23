package com.iot.simulator;

import com.iot.protocol.DeviceMessage;
import com.iot.protocol.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class DeviceSimulatorHandler
        extends ChannelInboundHandlerAdapter {

    private final String deviceId;

    public DeviceSimulatorHandler(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println(
                "[" + deviceId + "] connected to gateway"
        );

        DeviceMessage heartbeat = new DeviceMessage(
                MessageType.HEARTBEAT,
                deviceId.getBytes(StandardCharsets.UTF_8)
        );

        ctx.writeAndFlush(heartbeat);

        System.out.println(
                "[" + deviceId + "] heartbeat sent"
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println(
                "[" + deviceId + "] disconnected"
        );
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        cause.printStackTrace();
        ctx.close();
    }
}
