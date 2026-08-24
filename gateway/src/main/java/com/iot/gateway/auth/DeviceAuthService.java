package com.iot.gateway.auth;

import java.util.Map;

/**
 * 设备身份认证服务。
 *
 * 当前使用内存中的设备凭证模拟设备注册中心。
 * 后续接入 MySQL 后可替换为数据库查询。
 */
public class DeviceAuthService {

    private final Map<String, String> deviceSecrets = Map.of(
            "DEVICE-001", "iot-demo-001",
            "DEVICE-002", "iot-demo-002",
            "DEVICE-003", "iot-demo-003"
    );

    /**
     * 验证设备身份。
     */
    public boolean authenticate(
            String deviceId,
            String secret) {

        if (deviceId == null || deviceId.isBlank()
                || secret == null || secret.isBlank()) {
            return false;
        }

        String expectedSecret =
                deviceSecrets.get(deviceId);

        return expectedSecret != null
                && expectedSecret.equals(secret);
    }
}
