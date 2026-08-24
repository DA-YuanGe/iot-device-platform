package com.iot.gateway.controller;

import com.iot.service.alarm.DeviceAlarm;
import com.iot.service.alarm.DeviceAlarmService;
import com.iot.service.status.DeviceStatus;
import com.iot.service.status.DeviceStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备状态与告警查询接口。
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceStatusController {

    private final DeviceStatusService statusService;

    private final DeviceAlarmService alarmService;

    public DeviceStatusController(
            DeviceStatusService statusService,
            DeviceAlarmService alarmService) {

        this.statusService = statusService;
        this.alarmService = alarmService;
    }

    @GetMapping("/{deviceId}/status")
    public ResponseEntity<DeviceStatus> getStatus(
            @PathVariable String deviceId) {

        DeviceStatus status =
                statusService.getStatus(deviceId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public Map<String, DeviceStatus> getAllStatuses() {

        return statusService.getAllStatuses();
    }

    @GetMapping("/{deviceId}/events")
    public List<Object> getEventHistory(
            @PathVariable String deviceId) {

        return statusService.getEventHistory(deviceId);
    }

    /**
     * 查询所有当前未恢复告警。
     */
    @GetMapping("/alarms")
    public Map<String, DeviceAlarm> getActiveAlarms() {

        return alarmService.getActiveAlarms();
    }

    /**
     * 查询指定设备当前未恢复告警。
     */
    @GetMapping("/{deviceId}/alarm")
    public ResponseEntity<DeviceAlarm> getActiveAlarm(
            @PathVariable String deviceId) {

        DeviceAlarm alarm =
                alarmService.getActiveAlarm(deviceId);

        if (alarm == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(alarm);
    }
}
