package com.iot.simulator;

import com.iot.protocol.DeviceMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class DeviceSimulatorApplication {

    public static void main(String[] args) throws Exception {

        String deviceId = "DEVICE-001";

        EventLoopGroup group = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) {

                            channel.pipeline()
                                    .addLast(new DeviceMessageEncoder())
                                    .addLast(new DeviceSimulatorHandler(deviceId));
                        }
                    });

            System.out.println(
                    "[" + deviceId + "] connecting to 127.0.0.1:9000..."
            );

            Channel channel =
                    bootstrap.connect(
                            "127.0.0.1",
                            9000
                    ).sync().channel();

            channel.closeFuture().sync();

        } finally {

            group.shutdownGracefully();
        }
    }
}
