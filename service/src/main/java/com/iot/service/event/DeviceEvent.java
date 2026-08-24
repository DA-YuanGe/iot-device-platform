package com.iot.service.event;

import java.time.LocalDateTime;

/**
 * 设备状态事件。
 */
public class DeviceEvent {

    /**
     * 设备唯一标识。
     */
    private final String deviceId;

    /**
     * 事件类型。
     */
    private final DeviceEventType type;

    /**
     * 事件发生时间。
     */
    private final LocalDateTime eventTime;

    public DeviceEvent(
            String deviceId,
            DeviceEventType type) {

        this.deviceId = deviceId;
        this.type = type;
        this.eventTime = LocalDateTime.now();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public DeviceEventType getType() {
        return type;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "DeviceEvent{" +
                "deviceId='" + deviceId + '\'' +
                ", type=" + type +
                ", eventTime=" + eventTime +
                '}';
    }
}
