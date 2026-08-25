package com.iot.gateway.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 设备身份认证服务。
 *
 * 设备凭证从 MySQL 查询，
 * 不再使用内存中的固定设备凭证。
 */
@Service
public class DeviceAuthService {

    private final JdbcTemplate jdbcTemplate;

    public DeviceAuthService(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

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

        String sql =
                "SELECT COUNT(*) " +
                "FROM device " +
                "WHERE device_id = ? " +
                "AND device_secret = ?";

        Integer count =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        deviceId,
                        secret
                );

        return count != null && count > 0;
    }
}
