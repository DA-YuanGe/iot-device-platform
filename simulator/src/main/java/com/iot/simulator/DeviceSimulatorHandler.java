package com.iot.simulator;

import com.iot.protocol.DeviceMessage;
import com.iot.protocol.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DeviceSimulatorHandler
        extends ChannelInboundHandlerAdapter {

    private final String deviceId;

    private ScheduledFuture<?> heartbeatTask;

    private ChannelHandlerContext context;

    /**
     * 是否正在发送心跳。
     *
     * 默认开启，模拟真实设备持续在线。
     */
    private volatile boolean heartbeatEnabled = true;

    public DeviceSimulatorHandler(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        this.context = ctx;

        System.out.println(
                "[" + deviceId + "] connected to gateway"
        );

        sendHeartbeat(ctx);

        heartbeatTask = ctx.executor().scheduleAtFixedRate(
                () -> sendHeartbeat(ctx),
                5,
                5,
                TimeUnit.SECONDS
        );
    }

    private void sendHeartbeat(ChannelHandlerContext ctx) {

        if (!ctx.channel().isActive()) {
            return;
        }

        if (!heartbeatEnabled) {
            return;
        }

        DeviceMessage heartbeat =
                new DeviceMessage(
                        MessageType.HEARTBEAT,
                        deviceId.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        ctx.writeAndFlush(heartbeat);

        System.out.println(
                "[" + deviceId + "] heartbeat sent"
        );
    }

    /**
     * 暂停心跳。
     */
    public void pauseHeartbeat() {

        if (!heartbeatEnabled) {
            return;
        }

        heartbeatEnabled = false;

        System.out.println(
                "[" + deviceId +
                "] heartbeat simulation stopped"
        );
    }

    /**
     * 恢复心跳。
     */
    public void resumeHeartbeat() {

        if (heartbeatEnabled) {
            return;
        }

        heartbeatEnabled = true;

        System.out.println(
                "[" + deviceId +
                "] heartbeat simulation resumed"
        );

        if (context != null) {
            sendHeartbeat(context);
        }
    }

    @Override
    public void channelInactive(
            ChannelHandlerContext ctx) {

        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        System.out.println(
                "[" + deviceId +
                "] disconnected"
        );
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        cause.printStackTrace();

        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        ctx.close();
    }
}
