package com.iot.gateway;

import com.iot.protocol.DeviceMessage;
import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.DeviceEventType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

public class DeviceConnectionHandler
        extends ChannelInboundHandlerAdapter {

    private final DeviceSessionManager sessionManager =
            DeviceSessionManager.getInstance();

    private final DeviceEventPublisher eventPublisher;

    private String deviceId;

    public DeviceConnectionHandler(
            DeviceEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

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

        if (deviceId == null && !payload.isBlank()) {

            deviceId = payload;

            boolean alreadyOnline =
                    sessionManager.isOnline(deviceId);

            sessionManager.register(
                    deviceId,
                    ctx.channel()
            );

            eventPublisher.publish(
                    new DeviceEvent(
                            deviceId,
                            alreadyOnline
                                    ? DeviceEventType.RECONNECTED
                                    : DeviceEventType.ONLINE
                    )
            );
        }

        if (deviceId != null &&
                message.getType() ==
                        com.iot.protocol.MessageType.HEARTBEAT) {

            boolean wasOffline =
                    sessionManager.getSession(deviceId) != null &&
                    !sessionManager.getSession(deviceId).isOnline();

            sessionManager.heartbeat(deviceId);

            if (wasOffline) {

                eventPublisher.publish(
                        new DeviceEvent(
                                deviceId,
                                DeviceEventType.RECONNECTED
                        )
                );

            } else {

                eventPublisher.publish(
                        new DeviceEvent(
                                deviceId,
                                DeviceEventType.HEARTBEAT
                        )
                );
            }
        }
    }

    @Override
    public void channelInactive(
            ChannelHandlerContext ctx) {

        System.out.println(
                "[DEVICE] disconnected: " +
                ctx.channel().remoteAddress()
        );

        if (deviceId != null) {
            sessionManager.remove(deviceId, ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        cause.printStackTrace();

        if (deviceId != null) {
            sessionManager.remove(deviceId, ctx.channel());
        }

        ctx.close();
    }
}
