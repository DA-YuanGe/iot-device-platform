package com.iot.gateway;

import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.LoggingDeviceEventListener;
import com.iot.service.status.DeviceStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Gateway 事件组件配置。
 */
@Configuration
public class GatewayEventConfig {

    @Bean
    public DeviceStatusService deviceStatusService(
            RedisTemplate<String, Object> redisTemplate) {

        return new DeviceStatusService(
                redisTemplate
        );
    }

    @Bean
    public DeviceEventPublisher deviceEventPublisher(
            DeviceStatusService deviceStatusService) {

        DeviceEventPublisher publisher =
                new DeviceEventPublisher();

        publisher.addListener(
                new LoggingDeviceEventListener()
        );

        publisher.addListener(
                deviceStatusService
        );

        return publisher;
    }
}
