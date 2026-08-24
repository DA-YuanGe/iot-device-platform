package com.iot.service.status;

import com.iot.service.event.DeviceEventType;

import java.time.LocalDateTime;

/**
 * 设备运行状态。
 *
 * ONLINE / OFFLINE 表示设备当前运行状态；
 * HEARTBEAT / RECONNECTED 属于设备事件，不直接作为最终状态。
 */
public class DeviceStatus {

    private final String deviceId;

    /**
     * 设备当前运行状态。
     *
     * 只允许 ONLINE / OFFLINE。
     */
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

    /**
     * 根据设备事件更新运行状态。
     *
     * ONLINE：
     *   设备上线，状态变为 ONLINE。
     *
     * HEARTBEAT：
     *   仅更新最后心跳时间，不改变当前运行状态。
     *
     * OFFLINE：
     *   设备离线，状态变为 OFFLINE。
     *
     * RECONNECTED：
     *   设备重新上线，状态恢复为 ONLINE。
     */
    public void update(
            DeviceEventType eventType,
            LocalDateTime eventTime) {

        if (eventType == null || eventTime == null) {
            return;
        }

        this.lastEventTime = eventTime;

        switch (eventType) {

            case ONLINE:
                this.state = DeviceEventType.ONLINE;
                this.connectedAt = eventTime;
                break;

            case HEARTBEAT:
                this.lastHeartbeatTime = eventTime;
                break;

            case OFFLINE:
                this.state = DeviceEventType.OFFLINE;
                break;

            case RECONNECTED:
                this.state = DeviceEventType.ONLINE;
                this.connectedAt = eventTime;
                this.lastHeartbeatTime = eventTime;
                break;

            default:
                break;
        }
    }
}
