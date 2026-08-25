package com.iot.service.device;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 设备基础信息服务。
 *
 * 为 Web 监控端提供设备列表及设备详情查询。
 */
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * 查询全部设备。
     */
    public List<Map<String, Object>> getAllDevices() {
        return deviceRepository.findAll();
    }

    /**
     * 查询指定设备。
     */
    public Map<String, Object> getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }
}
