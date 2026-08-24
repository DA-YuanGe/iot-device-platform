package com.iot.gateway;

import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.event.LoggingDeviceEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 事件组件配置。
 */
@Configuration
public class GatewayEventConfig {

    @Bean
    public DeviceEventPublisher deviceEventPublisher() {

        DeviceEventPublisher publisher =
                new DeviceEventPublisher();

        publisher.addListener(
                new LoggingDeviceEventListener()
        );

        return publisher;
    }
}
