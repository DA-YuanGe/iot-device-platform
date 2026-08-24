package com.iot.simulator;

import com.iot.protocol.DeviceMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class DeviceSimulatorApplication {

    public static void main(String[] args) throws Exception {

        String deviceId = "DEVICE-001";

        EventLoopGroup group =
                new NioEventLoopGroup();

        try {

            final DeviceSimulatorHandler[] handlerHolder =
                    new DeviceSimulatorHandler[1];

            Bootstrap bootstrap =
                    new Bootstrap();

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(
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

            System.out.println(
                    "================================="
            );
            System.out.println(
                    " IoT Device Simulator"
            );
            System.out.println(
                    " Commands: pause / resume / exit"
            );
            System.out.println(
                    "================================="
            );

            Thread commandThread =
                    new Thread(
                            () -> {

                                Scanner scanner =
                                        new Scanner(System.in);

                                while (channel.isActive()) {

                                    if (!scanner.hasNextLine()) {
                                        break;
                                    }

                                    String command =
                                            scanner.nextLine()
                                                    .trim()
                                                    .toLowerCase();

                                    if ("pause".equals(command)) {

                                        handlerHolder[0]
                                                .pauseHeartbeat();

                                    } else if ("resume".equals(command)) {

                                        handlerHolder[0]
                                                .resumeHeartbeat();

                                    } else if ("exit".equals(command)) {

                                        channel.close();
                                        break;

                                    } else if (!command.isEmpty()) {

                                        System.out.println(
                                                "Unknown command: " +
                                                command
                                        );
                                    }
                                }
                            },
                            "device-command-thread"
                    );

            commandThread.setDaemon(true);
            commandThread.start();

            channel.closeFuture().sync();

        } finally {

            group.shutdownGracefully();
        }
    }
}
