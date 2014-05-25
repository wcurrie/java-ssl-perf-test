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
        final LinkedBlockingQueue<Result> results = new LinkedBlockingQueue<Result>();

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

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addFirst(new SslHandler(sslEngine));
                            pipeline.addLast(new ISOMsgEncoder());
                            pipeline.addLast(new ISOMsgDecoder());
                            pipeline.addLast(new EchoClientHandler(results));
                        }
                    });
            System.out.println("connecting");
            ChannelFuture f = b.connect().sync();
            System.out.println("connected");
            f.channel().closeFuture().sync();
        } finally {
            System.out.println("shutting down");
            group.shutdownGracefully().sync();
        }

        System.out.println("awaiting result");
        Result result = results.take();
        System.out.println(result);
    }

}
