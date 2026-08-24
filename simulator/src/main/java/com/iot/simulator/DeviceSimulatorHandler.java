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

    private ScheduledFuture<?> telemetryTask;

    private ChannelHandlerContext context;

    private volatile boolean authenticated = false;

    /**
     * 是否正在发送心跳。
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

        /*
         * 建立 TCP 连接后首先进行设备认证。
         */
        sendLogin(ctx);
    }

    /**
     * 发送 LOGIN。
     *
     * Payload:
     *
     * DEVICE-001|iot-demo-001
     */
    private void sendLogin(ChannelHandlerContext ctx) {

        String secret =
                getDeviceSecret(deviceId);

        String payload =
                deviceId + "|" + secret;

        DeviceMessage login =
                new DeviceMessage(
                        MessageType.LOGIN,
                        payload.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        ctx.writeAndFlush(login);

        System.out.println(
                "[" + deviceId +
                "] login sent"
        );
    }

    /**
     * 根据设备 ID 获取模拟设备密钥。
     */
    private String getDeviceSecret(String deviceId) {

        return switch (deviceId) {

            case "DEVICE-001" ->
                    "iot-demo-001";

            case "DEVICE-002" ->
                    "iot-demo-002";

            case "DEVICE-003" ->
                    "iot-demo-003";

            default ->
                    "invalid-secret";
        };
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
                "[" + deviceId +
                "] received: type=" +
                message.getType() +
                ", payload=" +
                payload
        );

        /*
         * 网关返回 ACK。
         */
        if (message.getType() == MessageType.ACK) {

            if ("AUTH_SUCCESS".equals(payload)) {

                authenticated = true;

                System.out.println(
                        "[" + deviceId +
                        "] authentication success"
                );

                startHeartbeat(ctx);
                startTelemetry(ctx);
            }

            return;
        }
    }

    /**
     * 认证成功后开始发送心跳。
     */
    private void startHeartbeat(
            ChannelHandlerContext ctx) {

        if (!authenticated) {
            return;
        }

        sendHeartbeat(ctx);

        heartbeatTask =
                ctx.executor().scheduleAtFixedRate(
                        () -> sendHeartbeat(ctx),
                        5,
                        5,
                        TimeUnit.SECONDS
                );
    }

    /**
     * 认证成功后开始发送遥测数据。
     */
    private void startTelemetry(
            ChannelHandlerContext ctx) {

        if (!authenticated) {
            return;
        }

        sendTelemetry(ctx);

        telemetryTask =
                ctx.executor().scheduleAtFixedRate(
                        () -> sendTelemetry(ctx),
                        5,
                        5,
                        TimeUnit.SECONDS
                );
    }

    /**
     * 发送心跳。
     */
    private void sendHeartbeat(
            ChannelHandlerContext ctx) {

        if (!ctx.channel().isActive()) {
            return;
        }

        if (!authenticated) {
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
                "[" + deviceId +
                "] heartbeat sent"
        );
    }

    /**
     * 发送遥测数据。
     *
     * Payload:
     *
     * temperature|humidity|voltage
     */
    private void sendTelemetry(
            ChannelHandlerContext ctx) {

        if (!ctx.channel().isActive()) {
            return;
        }

        if (!authenticated) {
            return;
        }

        String payload =
                "25.5|60.0|3.70";

        DeviceMessage telemetry =
                new DeviceMessage(
                        MessageType.TELEMETRY,
                        payload.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        ctx.writeAndFlush(telemetry);

        System.out.println(
                "[" + deviceId +
                "] telemetry sent: " +
                payload
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

        if (context != null && authenticated) {
            sendHeartbeat(context);
        }
    }

    @Override
    public void channelInactive(
            ChannelHandlerContext ctx) {

        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        if (telemetryTask != null) {
            telemetryTask.cancel(false);
        }

        authenticated = false;

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

        if (telemetryTask != null) {
            telemetryTask.cancel(false);
        }

        ctx.close();
    }
}
