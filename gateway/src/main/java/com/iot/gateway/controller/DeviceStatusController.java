package com.iot.gateway.controller;

import com.iot.service.status.DeviceStatus;
import com.iot.service.status.DeviceStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备状态查询接口。
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceStatusController {

    private final DeviceStatusService statusService;

    public DeviceStatusController(
            DeviceStatusService statusService) {

        this.statusService = statusService;
    }

    /**
     * 查询指定设备状态。
     */
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

    /**
     * 查询所有设备状态。
     */
    @GetMapping("/status")
    public Map<String, DeviceStatus> getAllStatuses() {

        return statusService.getAllStatuses();
    }
}
