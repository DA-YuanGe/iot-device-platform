package com.iot.gateway;

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

        NettyTcpServer server =
                context.getBean(NettyTcpServer.class);

        try {

            server.start();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Netty server interrupted",
                    e
            );
        }
    }
}
