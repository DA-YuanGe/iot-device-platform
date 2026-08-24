package com.iot.service.alarm;

import java.time.LocalDateTime;

/**
 * 设备告警。
 */
public class DeviceAlarm {

    /**
     * 告警唯一标识。
     */
    private final String alarmId;

    /**
     * 设备唯一标识。
     */
    private final String deviceId;

    /**
     * 告警类型。
     */
    private final String type;

    /**
     * 告警等级。
     */
    private final String level;

    /**
     * 告警状态。
     */
    private String status;

    /**
     * 告警产生时间。
     */
    private final LocalDateTime createdAt;

    /**
     * 告警恢复时间。
     */
    private LocalDateTime recoveredAt;

    public DeviceAlarm(
            String alarmId,
            String deviceId,
            String type,
            String level) {

        this.alarmId = alarmId;
        this.deviceId = deviceId;
        this.type = type;
        this.level = level;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public String getAlarmId() {
        return alarmId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getType() {
        return type;
    }

    public String getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRecoveredAt() {
        return recoveredAt;
    }

    /**
     * 恢复告警。
     */
    public void recover() {

        this.status = "RECOVERED";
        this.recoveredAt = LocalDateTime.now();
    }
}
