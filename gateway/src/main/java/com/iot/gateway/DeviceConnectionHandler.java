package com.iot.gateway;

import com.iot.protocol.DeviceMessage;
import com.iot.protocol.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class DeviceConnectionHandler
        extends ChannelInboundHandlerAdapter {

    private final DeviceSessionManager sessionManager =
            DeviceSessionManager.getInstance();

    private String deviceId;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println(
                "[DEVICE] connected: " +
                ctx.channel().remoteAddress()
        );
    }

    @Override
    public void channelRead(
            ChannelHandlerContext ctx,
            Object msg) {

        if (!(msg instanceof DeviceMessage message)) {
            return;
        }

        String payload =
                new String(
                        message.getPayload(),
                        StandardCharsets.UTF_8
                );

        System.out.println(
                "[DEVICE] message received: " +
                "type=" + message.getType() +
                ", payload=" + payload
        );

        /*
         * 第一次收到设备消息时识别设备。
         */
        if (deviceId == null && !payload.isBlank()) {

            deviceId = payload;

            sessionManager.register(
                    deviceId,
                    ctx.channel()
            );

            return;
        }

        /*
         * 已经建立会话后，
         * 心跳只更新最近心跳时间。
         */
        if (deviceId != null
                && message.getType() == MessageType.HEARTBEAT) {

            sessionManager.heartbeat(deviceId);

            System.out.println(
                    "[SESSION] heartbeat updated: " +
                    deviceId
            );
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println(
                "[DEVICE] disconnected: " +
                ctx.channel().remoteAddress()
        );

        if (deviceId != null) {
            sessionManager.remove(deviceId);
        }
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        cause.printStackTrace();

        if (deviceId != null) {
            sessionManager.remove(deviceId);
        }

        ctx.close();
    }
}
