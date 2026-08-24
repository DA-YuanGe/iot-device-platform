package com.iot.service.status;

import com.iot.service.event.DeviceEventType;

import java.time.LocalDateTime;

/**
 * 设备运行状态。
 */
public class DeviceStatus {

    private final String deviceId;

    private DeviceEventType state;

    private LocalDateTime connectedAt;

    private LocalDateTime lastHeartbeatTime;

    private LocalDateTime lastEventTime;

    public DeviceStatus(String deviceId) {
        this.deviceId = deviceId;
        this.state = DeviceEventType.OFFLINE;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public DeviceEventType getState() {
        return state;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public LocalDateTime getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public LocalDateTime getLastEventTime() {
        return lastEventTime;
    }

    public void update(DeviceEventType eventType,
                       LocalDateTime eventTime) {

        this.state = eventType;
        this.lastEventTime = eventTime;

        if (eventType == DeviceEventType.ONLINE
                || eventType == DeviceEventType.RECONNECTED) {

            this.connectedAt = eventTime;
        }

        if (eventType == DeviceEventType.HEARTBEAT) {
            this.lastHeartbeatTime = eventTime;
        }
    }
}
