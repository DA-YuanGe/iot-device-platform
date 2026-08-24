package com.iot.gateway.controller;

import com.iot.service.telemetry.DeviceTelemetry;
import com.iot.service.telemetry.TelemetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备实时遥测数据查询接口。
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceTelemetryController {

    private final TelemetryService telemetryService;

    public DeviceTelemetryController(
            TelemetryService telemetryService) {

        this.telemetryService = telemetryService;
    }

    /**
     * 查询指定设备最新遥测数据。
     */
    @GetMapping("/{deviceId}/telemetry")
    public ResponseEntity<DeviceTelemetry> getLatestTelemetry(
            @PathVariable String deviceId) {

        DeviceTelemetry telemetry =
                telemetryService.getLatest(deviceId);

        if (telemetry == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(telemetry);
    }

    /**
     * 查询所有设备最新遥测数据。
     */
    @GetMapping("/telemetry")
    public Map<String, DeviceTelemetry> getAllLatestTelemetry() {

        return telemetryService.getAllLatest();
    }
}
