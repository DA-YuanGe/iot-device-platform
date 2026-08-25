package com.iot.gateway;

import com.iot.gateway.auth.DeviceAuthService;
import com.iot.protocol.DeviceMessage;
import com.iot.protocol.MessageType;
import com.iot.service.event.DeviceEvent;
import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.DeviceEventType;
import com.iot.service.telemetry.DeviceTelemetry;
import com.iot.service.telemetry.TelemetryService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class DeviceConnectionHandler
        extends ChannelInboundHandlerAdapter {

    private final DeviceSessionManager sessionManager =
            DeviceSessionManager.getInstance();

    private final DeviceEventPublisher eventPublisher;

    private final DeviceAuthService authService;

    private final TelemetryService telemetryService;

    private String deviceId;

    private boolean authenticated = false;

    public DeviceConnectionHandler(
            DeviceEventPublisher eventPublisher,
            DeviceAuthService authService,
            TelemetryService telemetryService) {

        this.eventPublisher = eventPublisher;
        this.authService = authService;
        this.telemetryService = telemetryService;
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

        /*
         * 未认证状态下，只允许 LOGIN。
         */
        if (!authenticated) {

            if (message.getType() != MessageType.LOGIN) {

                sendAck(ctx, "AUTH_REQUIRED");

                System.out.println(
                        "[AUTH] rejected unauthenticated message"
                );

                ctx.close();
                return;
            }

            authenticate(ctx, payload);
            return;
        }

        /*
         * 已认证设备不允许重复 LOGIN。
         */
        if (message.getType() == MessageType.LOGIN) {

            sendAck(ctx, "ALREADY_AUTHENTICATED");
            return;
        }

        /*
         * 心跳处理。
         */
        if (message.getType() == MessageType.HEARTBEAT) {

            boolean wasOffline =
                    sessionManager.getSession(deviceId) != null
                    && !sessionManager
                            .getSession(deviceId)
                            .isOnline();

            sessionManager.heartbeat(deviceId);

            eventPublisher.publish(
                    new DeviceEvent(
                            deviceId,
                            wasOffline
                                    ? DeviceEventType.RECONNECTED
                                    : DeviceEventType.HEARTBEAT
                    )
            );

            return;
        }

        /*
         * 遥测数据处理。
         *
         * Payload:
         * temperature|humidity|voltage
         */
        if (message.getType() == MessageType.TELEMETRY) {

            handleTelemetry(ctx, payload);
        }
    }

    /**
     * 处理设备登录。
     *
     * Payload:
     *
     * DEVICE-001|iot-demo-001
     */
    private void authenticate(
            ChannelHandlerContext ctx,
            String payload) {

        String[] parts = payload.split("\\|", 2);

        if (parts.length != 2) {

            System.out.println(
                    "[AUTH] invalid login payload"
            );

            sendAck(ctx, "AUTH_FAILED");
            ctx.close();
            return;
        }

        String loginDeviceId = parts[0].trim();
        String secret = parts[1].trim();

        boolean success =
                authService.authenticate(
                        loginDeviceId,
                        secret
                );

        if (!success) {

            System.out.println(
                    "[AUTH] failed, device=" +
                    loginDeviceId
            );

            sendAck(ctx, "AUTH_FAILED");
            ctx.close();
            return;
        }

        this.deviceId = loginDeviceId;
        this.authenticated = true;

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

        sendAck(ctx, "AUTH_SUCCESS");

        System.out.println(
                "[AUTH] success, device=" +
                deviceId
        );
    }

    /**
     * 处理遥测数据。
     *
     * Payload:
     *
     * temperature|humidity|voltage
     */
    private void handleTelemetry(
            ChannelHandlerContext ctx,
            String payload) {

        String[] parts = payload.split("\\|");

        if (parts.length != 3) {

            System.out.println(
                    "[TELEMETRY] invalid payload: " +
                    payload
            );

            sendAck(ctx, "TELEMETRY_FAILED");
            return;
        }

        try {

            double temperature =
                    Double.parseDouble(parts[0].trim());

            double humidity =
                    Double.parseDouble(parts[1].trim());

            double voltage =
                    Double.parseDouble(parts[2].trim());

            DeviceTelemetry telemetry =
                    new DeviceTelemetry(
                            deviceId,
                            temperature,
                            humidity,
                            voltage,
                            LocalDateTime.now()
                    );

            telemetryService.report(telemetry);

            sendAck(ctx, "TELEMETRY_SUCCESS");

        } catch (NumberFormatException e) {

            System.out.println(
                    "[TELEMETRY] invalid numeric data: " +
                    payload
            );

            sendAck(ctx, "TELEMETRY_FAILED");
        }
    }

    /**
     * 向设备发送 ACK。
     */
    private void sendAck(
            ChannelHandlerContext ctx,
            String result) {

        DeviceMessage ack =
                new DeviceMessage(
                        MessageType.ACK,
                        result.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        ctx.writeAndFlush(ack);
    }

    @Override
    public void channelInactive(
            ChannelHandlerContext ctx) {

        System.out.println(
                "[DEVICE] disconnected: " +
                ctx.channel().remoteAddress()
        );

        if (deviceId != null) {

        /*
         * 只有当前有效会话断开时，
         * 才发布 OFFLINE。
         *
         * 防止旧连接断开时误将新连接标记为 OFFLINE。
         */
        var currentSession =
                sessionManager.getSession(deviceId);

        boolean currentChannel =
                currentSession != null
                && currentSession.getChannel()
                        == ctx.channel();

        if (currentChannel) {

            eventPublisher.publish(
                    new DeviceEvent(
                            deviceId,
                            DeviceEventType.OFFLINE
                    )
            );
        }

        sessionManager.remove(
                deviceId,
                ctx.channel()
            );
        }
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx,
            Throwable cause) {

        cause.printStackTrace();

        if (deviceId != null) {
            sessionManager.remove(
                    deviceId,
                    ctx.channel()
            );
        }

        ctx.close();
    }
}
