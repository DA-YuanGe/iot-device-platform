package com.iot.gateway;

import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.LoggingDeviceEventListener;
import com.iot.service.status.DeviceStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 事件组件配置。
 */
@Configuration
public class GatewayEventConfig {

    /**
     * 设备状态服务。
     *
     * 由 Gateway 显式注册为 Spring Bean，
     * 供事件监听和 HTTP Controller 共同使用。
     */
    @Bean
    public DeviceStatusService deviceStatusService() {

        return new DeviceStatusService();
    }

    /**
     * 设备事件发布器。
     */
    @Bean
    public DeviceEventPublisher deviceEventPublisher(
            DeviceStatusService statusService) {

        DeviceEventPublisher publisher =
                new DeviceEventPublisher();

        publisher.addListener(
                new LoggingDeviceEventListener()
        );

        publisher.addListener(
                statusService
        );

        return publisher;
    }
}
