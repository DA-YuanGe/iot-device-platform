package com.iot.service.telemetry;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备遥测数据服务。
 *
 * 负责维护设备最新遥测数据，
 * 同时将遥测历史数据持久化到 MySQL。
 */
@Service
public class TelemetryService {

    private final Map<String, DeviceTelemetry> latestTelemetry =
            new ConcurrentHashMap<>();

    private final TelemetryRepository telemetryRepository;

    public TelemetryService(
            TelemetryRepository telemetryRepository) {

        this.telemetryRepository = telemetryRepository;
    }

    /**
     * 接收设备遥测数据。
     *
     * 最新数据保存在内存中，
     * 历史数据写入 MySQL。
     */
    public void report(DeviceTelemetry telemetry) {

        if (telemetry == null
                || telemetry.getDeviceId() == null
                || telemetry.getDeviceId().isBlank()) {
            return;
        }

        latestTelemetry.put(
                telemetry.getDeviceId(),
                telemetry
        );

        telemetryRepository.save(telemetry);

        System.out.println(
                "[TELEMETRY] " + telemetry
        );
    }

    /**
     * 查询设备最新遥测数据。
     */
    public DeviceTelemetry getLatest(String deviceId) {

        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        return latestTelemetry.get(deviceId);
    }

    /**
     * 查询所有设备最新遥测数据。
     */
    public Map<String, DeviceTelemetry> getAllLatest() {

        return Map.copyOf(latestTelemetry);
    }
}
