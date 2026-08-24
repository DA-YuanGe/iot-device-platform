package com.iot.simulator;

import com.iot.protocol.DeviceMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DeviceSimulatorApplication {

    public static void main(String[] args) throws Exception {

        String[] deviceIds = {
                "DEVICE-001",
                "DEVICE-002",
                "DEVICE-003"
        };

        EventLoopGroup group =
                new NioEventLoopGroup();

        Map<String, Channel> channels =
                new HashMap<>();

        Map<String, DeviceSimulatorHandler> handlers =
                new HashMap<>();

        try {

            Bootstrap bootstrap =
                    new Bootstrap();

            bootstrap.group(group)
                    .channel(NioSocketChannel.class);

            for (String deviceId : deviceIds) {

                final DeviceSimulatorHandler[] handlerHolder =
                        new DeviceSimulatorHandler[1];

                bootstrap.handler(
                        new ChannelInitializer<SocketChannel>() {

                            @Override
                            protected void initChannel(
                                    SocketChannel channel) {

                                DeviceSimulatorHandler handler =
                                        new DeviceSimulatorHandler(
                                                deviceId
                                        );

                                handlerHolder[0] = handler;

                                channel.pipeline()
                                        .addLast(
                                                new DeviceMessageEncoder()
                                        )
                                        .addLast(handler);
                            }
                        }
                );

                System.out.println(
                        "[" + deviceId +
                        "] connecting to 127.0.0.1:9000..."
                );

                Channel channel =
                        bootstrap.connect(
                                "127.0.0.1",
                                9000
                        ).sync().channel();

                channels.put(deviceId, channel);
                handlers.put(deviceId, handlerHolder[0]);
            }

            System.out.println(
                    "================================="
            );
            System.out.println(
                    " IoT Device Simulator"
            );
            System.out.println(
                    " Devices: DEVICE-001, DEVICE-002, DEVICE-003"
            );
            System.out.println(
                    " Commands:"
            );
            System.out.println(
                    "   pause DEVICE-001"
            );
            System.out.println(
                    "   resume DEVICE-001"
            );
            System.out.println(
                    "   exit"
            );
            System.out.println(
                    "================================="
            );

            Scanner scanner =
                    new Scanner(System.in);

            while (true) {

                if (!scanner.hasNextLine()) {
                    break;
                }

                String command =
                        scanner.nextLine()
                                .trim();

                if (command.isEmpty()) {
                    continue;
                }

                String[] parts =
                        command.split("\\s+");

                if ("exit".equalsIgnoreCase(parts[0])) {

                    break;
                }

                if (parts.length == 2) {

                    String action =
                            parts[0].toLowerCase();

                    String deviceId =
                            parts[1].toUpperCase();

                    DeviceSimulatorHandler handler =
                            handlers.get(deviceId);

                    if (handler == null) {

                        System.out.println(
                                "Unknown device: " +
                                deviceId
                        );

                        continue;
                    }

                    if ("pause".equals(action)) {

                        handler.pauseHeartbeat();

                    } else if ("resume".equals(action)) {

                        handler.resumeHeartbeat();

                    } else {

                        System.out.println(
                                "Unknown command: " +
                                command
                        );
                    }

                } else {

                    System.out.println(
                            "Usage: pause DEVICE-001 | " +
                            "resume DEVICE-001 | exit"
                    );
                }
            }

        } finally {

            for (Channel channel : channels.values()) {

                if (channel != null) {
                    channel.close();
                }
            }

            group.shutdownGracefully();
        }
    }
}
