package x.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import x.ClasspathKeystoreSocketFactory;
import x.Result;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class NettyClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 8976))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            SSLEngine sslEngine = ClasspathKeystoreSocketFactory.getSSLContext().createSSLEngine();
                            sslEngine.setUseClientMode(true);

                            ch.pipeline().addFirst(new SslHandler(sslEngine));
                            ch.pipeline().addLast(new EchoClientHandler(new LinkedBlockingQueue<Result>()));
                        }
                    });
            System.out.println("connecting");
            ChannelFuture f = b.connect().sync();
            System.out.println("connected");
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}
