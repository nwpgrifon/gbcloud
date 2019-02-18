package ru.malnev.gbcloud.client.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.SneakyThrows;
import ru.malnev.gbcloud.common.transport.INetworkEndpoint;
import ru.malnev.gbcloud.common.transport.Netty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

@Netty
@ApplicationScoped
public class NettyClient implements INetworkEndpoint
{
    private ChannelFuture channelFuture;

    @Override
    @SneakyThrows
    public void start()
    {
        final EventLoopGroup group = new NioEventLoopGroup();
        try
        {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        protected void initChannel(final SocketChannel socketChannel)
                        {
                            socketChannel.pipeline().addLast(new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    CDI.current().select(NettyClientHandler.class).get());
                        }
                    });
            channelFuture = bootstrap.connect("localhost", 9999).sync();
            channelFuture.channel().closeFuture().sync();
        }
        finally
        {
            group.shutdownGracefully();
        }
    }

    @Override
    @SneakyThrows
    public void stop()
    {
        channelFuture.channel().close();
    }
}
