package com.iot.gateway.session;

import io.netty.channel.Channel;

import java.time.LocalDateTime;

public class DeviceSession {

    /**
     * 设备唯一标识
     */
    private final String deviceId;

    /**
     * Netty连接
     */
    private final Channel channel;

    /**
     * 设备连接地址
     */
    private final String remoteAddress;

    /**
     * 建立连接时间
     */
    private final LocalDateTime connectedAt;

    /**
     * 最近一次心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 在线状态
     */
    private boolean online;

    public DeviceSession(
            String deviceId,
            Channel channel) {

        this.deviceId = deviceId;
        this.channel = channel;
        this.remoteAddress =
                String.valueOf(channel.remoteAddress());
        this.connectedAt = LocalDateTime.now();
        this.lastHeartbeatTime = LocalDateTime.now();
        this.online = true;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public LocalDateTime getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public boolean isOnline() {
        return online;
    }

    public void heartbeat() {
        this.lastHeartbeatTime = LocalDateTime.now();
    }

    public void offline() {
        this.online = false;
    }
}
