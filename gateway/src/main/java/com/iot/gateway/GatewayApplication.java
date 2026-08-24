package com.iot.gateway;

import com.iot.service.event.DeviceEventPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {

        var context =
                SpringApplication.run(
                        GatewayApplication.class,
                        args
                );

        DeviceEventPublisher eventPublisher =
                context.getBean(
                        DeviceEventPublisher.class
                );

        DeviceOfflineDetector offlineDetector =
                new DeviceOfflineDetector(
                        eventPublisher
                );

        offlineDetector.start();

        NettyTcpServer server =
                context.getBean(NettyTcpServer.class);

        try {

            server.start();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            offlineDetector.stop();

            throw new RuntimeException(
                    "Netty server interrupted",
                    e
            );
        }
    }
}
