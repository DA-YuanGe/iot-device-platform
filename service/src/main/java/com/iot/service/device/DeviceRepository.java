package com.iot.service.device;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 设备基础信息查询仓库。
 *
 * 负责从 MySQL 查询设备基础信息，
 * 为 Web 监控端提供设备列表数据。
 */
@Repository
public class DeviceRepository {

    private final JdbcTemplate jdbcTemplate;

    public DeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询全部设备。
     */
    public List<Map<String, Object>> findAll() {

        String sql =
                "SELECT " +
                "id, " +
                "device_id, " +
                "device_name, " +
                "device_type, " +
                "status, " +
                "last_online_time, " +
                "last_offline_time " +
                "FROM device " +
                "ORDER BY id";

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 查询指定设备。
     */
    public Map<String, Object> findByDeviceId(String deviceId) {

        String sql =
                "SELECT " +
                "id, " +
                "device_id, " +
                "device_name, " +
                "device_type, " +
                "status, " +
                "last_online_time, " +
                "last_offline_time " +
                "FROM device " +
                "WHERE device_id = ?";

        List<Map<String, Object>> result =
                jdbcTemplate.queryForList(sql, deviceId);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }
}
