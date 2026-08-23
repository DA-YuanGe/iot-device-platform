package com.iot.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import com.iot.protocol.DeviceMessageDecoder;
import org.springframework.stereotype.Component;

@Component
public class NettyTcpServer {

    private static final int PORT = 9000;

    public void start() throws InterruptedException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) {

                            channel.pipeline()
                                    .addLast(new DeviceMessageDecoder())
                                    .addLast(new DeviceConnectionHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = bootstrap.bind(PORT).sync().channel();

            System.out.println("=================================");
            System.out.println("   IoT Device Gateway");
            System.out.println("   Netty TCP Server started");
            System.out.println("   Port: " + PORT);
            System.out.println("=================================");

            channel.closeFuture().sync();

        } finally {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
