package com.iot.service.telemetry;

import java.time.LocalDateTime;

/**
 * 设备遥测数据。
 */
public class DeviceTelemetry {

    private String deviceId;

    private double temperature;

    private double humidity;

    private double voltage;

    private LocalDateTime reportTime;

    public DeviceTelemetry() {
    }

    public DeviceTelemetry(
            String deviceId,
            double temperature,
            double humidity,
            double voltage,
            LocalDateTime reportTime) {

        this.deviceId = deviceId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.voltage = voltage;
        this.reportTime = reportTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    @Override
    public String toString() {
        return "DeviceTelemetry{" +
                "deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", voltage=" + voltage +
                ", reportTime=" + reportTime +
                '}';
    }
}
