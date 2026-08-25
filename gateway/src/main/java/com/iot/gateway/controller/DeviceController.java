package com.iot.gateway.controller;

import com.iot.service.device.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Web设备基础信息接口。
 *
 * 为设备监控端提供设备列表和设备详情。
 */
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 查询全部设备。
     */
    @GetMapping
    public List<Map<String, Object>> getAllDevices() {
        return deviceService.getAllDevices();
    }

    /**
     * 查询指定设备基础信息。
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<Map<String, Object>> getDevice(
            @PathVariable String deviceId) {

        Map<String, Object> device =
                deviceService.getDevice(deviceId);

        if (device == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(device);
    }
}
