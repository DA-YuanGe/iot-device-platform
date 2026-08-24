package com.iot.gateway;

import com.iot.gateway.auth.DeviceAuthService;
import com.iot.service.alarm.DeviceAlarmEventListener;
import com.iot.service.alarm.DeviceAlarmService;
import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.LoggingDeviceEventListener;
import com.iot.service.status.DeviceStatusService;
import com.iot.service.telemetry.TelemetryRepository;
import com.iot.service.telemetry.TelemetryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Gateway 事件组件配置。
 */
@Configuration
public class GatewayEventConfig {

    @Bean
    public DeviceAuthService deviceAuthService() {
        return new DeviceAuthService();
    }

    @Bean
    public TelemetryService telemetryService(
            TelemetryRepository telemetryRepository) {

        return new TelemetryService(
                telemetryRepository
        );
    }

    @Bean
    public DeviceStatusService deviceStatusService(
            RedisTemplate<String, Object> redisTemplate) {

        return new DeviceStatusService(
                redisTemplate
        );
    }

    @Bean
    public DeviceEventPublisher deviceEventPublisher(
            DeviceStatusService deviceStatusService,
            DeviceAlarmService deviceAlarmService) {

        DeviceEventPublisher publisher =
                new DeviceEventPublisher();

        publisher.addListener(
                new LoggingDeviceEventListener()
        );

        publisher.addListener(
                deviceStatusService
        );

        publisher.addListener(
                new DeviceAlarmEventListener(
                        deviceAlarmService
                )
        );

        return publisher;
    }
}
