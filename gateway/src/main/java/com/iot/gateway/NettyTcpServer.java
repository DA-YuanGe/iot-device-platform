package com.iot.gateway;

import com.iot.gateway.auth.DeviceAuthService;
import com.iot.protocol.DeviceMessageDecoder;
import com.iot.protocol.DeviceMessageEncoder;
import com.iot.service.event.DeviceEventPublisher;
import com.iot.service.telemetry.TelemetryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

@Component
public class NettyTcpServer {

    private static final int PORT = 9000;

    private final DeviceEventPublisher eventPublisher;

    private final TelemetryService telemetryService;

    private final DeviceAuthService authService;

    public NettyTcpServer(
            DeviceEventPublisher eventPublisher,
            DeviceAuthService authService,
            TelemetryService telemetryService) {

        this.eventPublisher = eventPublisher;
        this.authService = authService;
        this.telemetryService = telemetryService;
    }

    public void start() throws InterruptedException {

        NioEventLoopGroup bossGroup =
                new NioEventLoopGroup(1);

        NioEventLoopGroup workerGroup =
                new NioEventLoopGroup();

        try {

            ServerBootstrap bootstrap =
                    new ServerBootstrap();

            bootstrap.group(
                            bossGroup,
                            workerGroup
                    )
                    .channel(NioServerSocketChannel.class)
                    .handler(
                            new LoggingHandler(
                                    LogLevel.INFO
                            )
                    )
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {

                                @Override
                                protected void initChannel(
                                        SocketChannel channel) {

                                    channel.pipeline()
                                            .addLast(
                                                    new DeviceMessageEncoder()
                                            )
                                            .addLast(
                                                    new DeviceMessageDecoder()
                                            )
                                            .addLast(
                                                    new DeviceConnectionHandler(
                                                            eventPublisher,
                                                            authService,
                                                            telemetryService
                                                    )
                                            );
                                }
                            }
                    )
                    .childOption(
                            ChannelOption.SO_KEEPALIVE,
                            true
                    );

            Channel channel =
                    bootstrap.bind(PORT)
                            .sync()
                            .channel();

            System.out.println(
                    "================================="
            );
            System.out.println(
                    "   IoT Device Gateway"
            );
            System.out.println(
                    "   Netty TCP Server started"
            );
            System.out.println(
                    "   Port: " + PORT
            );
            System.out.println(
                    "================================="
            );

            channel.closeFuture().sync();

        } finally {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
