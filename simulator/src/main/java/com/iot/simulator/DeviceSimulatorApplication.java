package com.iot.simulator;

import com.iot.protocol.DeviceMessageDecoder;
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

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9000;

    public static void main(String[] args) throws Exception {

        int deviceCount = parseDeviceCount(args);

        String[] deviceIds = generateDeviceIds(deviceCount);

        EventLoopGroup group = new NioEventLoopGroup();

        Map<String, Channel> channels = new HashMap<>();
        Map<String, DeviceSimulatorHandler> handlers = new HashMap<>();

        try {

            Bootstrap bootstrap = new Bootstrap();

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
                                        new DeviceSimulatorHandler(deviceId);

                                handlerHolder[0] = handler;

                                channel.pipeline()
                                        .addLast(new DeviceMessageDecoder())
                                        .addLast(new DeviceMessageEncoder())
                                        .addLast(handler);
                            }
                        }
                );

                System.out.println(
                        "[" + deviceId +
                        "] connecting to " +
                        HOST + ":" + PORT + "..."
                );

                Channel channel =
                        bootstrap.connect(HOST, PORT)
                                .sync()
                                .channel();

                channels.put(deviceId, channel);
                handlers.put(deviceId, handlerHolder[0]);
            }

            System.out.println("=================================");
            System.out.println(" IoT Device Simulator");
            System.out.println(" Device count: " + deviceCount);

            if (deviceCount <= 3) {
                System.out.println(
                        " Devices: DEVICE-001, DEVICE-002, DEVICE-003"
                );
            } else {
                System.out.println(
                        " Devices: TEST-001 ~ TEST-" +
                        String.format("%03d", deviceCount)
                );
            }

            System.out.println(" Commands:");
            System.out.println("   pause DEVICE-001");
            System.out.println("   resume DEVICE-001");
            System.out.println("   exit");
            System.out.println("=================================");

            Scanner scanner = new Scanner(System.in);

            while (true) {

                if (!scanner.hasNextLine()) {
                    break;
                }

                String command = scanner.nextLine().trim();

                if (command.isEmpty()) {
                    continue;
                }

                String[] parts = command.split("\\s+");

                if ("exit".equalsIgnoreCase(parts[0])) {
                    break;
                }

                if (parts.length == 2) {

                    String action = parts[0].toLowerCase();
                    String deviceId = parts[1].toUpperCase();

                    DeviceSimulatorHandler handler =
                            handlers.get(deviceId);

                    if (handler == null) {

                        System.out.println(
                                "Unknown device: " + deviceId
                        );

                        continue;
                    }

                    if ("pause".equals(action)) {

                        handler.pauseHeartbeat();

                    } else if ("resume".equals(action)) {

                        handler.resumeHeartbeat();

                    } else {

                        System.out.println(
                                "Unknown command: " + command
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

    /**
     * 解析模拟设备数量。
     *
     * 不传参数时保持原来的 3 台设备行为。
     */
    private static int parseDeviceCount(String[] args) {

        if (args.length == 0) {
            return 3;
        }

        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Usage: java -jar simulator.jar [deviceCount]"
            );
        }

        int count;

        try {
            count = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "deviceCount must be a positive integer",
                    e
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "deviceCount must be greater than 0"
            );
        }

        return count;
    }

    /**
     * 生成设备 ID。
     *
     * 3 台以内保持原有 DEVICE-001 ~ DEVICE-003，
     * 超过 3 台使用 TEST-001 ~ TEST-N。
     */
    private static String[] generateDeviceIds(int count) {

        String[] deviceIds = new String[count];

        if (count <= 3) {

            for (int i = 0; i < count; i++) {
                deviceIds[i] =
                        String.format("DEVICE-%03d", i + 1);
            }

        } else {

            for (int i = 0; i < count; i++) {
                deviceIds[i] =
                        String.format("TEST-%03d", i + 1);
            }
        }

        return deviceIds;
    }
}
