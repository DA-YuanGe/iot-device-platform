package com.iot.service.status;

import com.iot.service.event.DeviceEventType;

import java.time.LocalDateTime;

/**
 * 设备状态事件历史记录。
 */
public class DeviceEventRecord {

    private final String deviceId;

    private final DeviceEventType type;

    private final LocalDateTime eventTime;

    public DeviceEventRecord(
            String deviceId,
            DeviceEventType type,
            LocalDateTime eventTime) {

        this.deviceId = deviceId;
        this.type = type;
        this.eventTime = eventTime;
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
}
