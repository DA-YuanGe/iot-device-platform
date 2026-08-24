package com.iot.service.telemetry;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 设备遥测数据持久化仓库。
 *
 * 负责将设备遥测历史数据写入 MySQL。
 */
@Repository
public class TelemetryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TelemetryRepository(
            JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存设备遥测数据。
     */
    public void save(DeviceTelemetry telemetry) {

        String sql =
                "INSERT INTO device_telemetry " +
                "(device_id, temperature, humidity, voltage, report_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                telemetry.getDeviceId(),
                telemetry.getTemperature(),
                telemetry.getHumidity(),
                telemetry.getVoltage(),
                telemetry.getReportTime()
        );
    }
}
