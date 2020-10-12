import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.logging.Level;

public class MainServer {
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolHandler(), new EchoProtocolHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(8787).sync();
            ServiceSQL.connect();
            LogHelper.protocolLogger.log(Level.INFO, "Server is ONLINE");
            LogHelper.echoLogger.log(Level.INFO, "Server is ONLINE");
            f.channel().closeFuture().sync();
        } finally {
            ServiceSQL.disconnect();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        LogHelper.startLog();
        try {
            new MainServer().run();
        } catch (Exception e) {
            LogHelper.protocolLogger.log(Level.WARNING, "Server is not start");
            LogHelper.echoLogger.log(Level.WARNING, "Server is not start");
        }

    }
}
