package com.iot.service.status;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 设备运行状态持久化仓库。
 *
 * 负责将设备当前运行状态及上线/离线时间同步到 MySQL。
 */
@Repository
public class DeviceStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    public DeviceStatusRepository(
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 更新设备上线状态。
     */
    public void markOnline(
            String deviceId,
            LocalDateTime eventTime) {

        String sql =
                "UPDATE device " +
                "SET status = ?, " +
                "last_online_time = ? " +
                "WHERE device_id = ?";

        jdbcTemplate.update(
                sql,
                "ONLINE",
                eventTime,
                deviceId
        );
    }

    /**
     * 更新设备离线状态。
     */
    public void markOffline(
            String deviceId,
            LocalDateTime eventTime) {

        String sql =
                "UPDATE device " +
                "SET status = ?, " +
                "last_offline_time = ? " +
                "WHERE device_id = ?";

        jdbcTemplate.update(
                sql,
                "OFFLINE",
                eventTime,
                deviceId
        );
    }
}
