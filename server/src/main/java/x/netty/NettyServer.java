package x.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import x.ClasspathKeystoreSocketFactory;
import x.PingListener;
import x.StatsListener;

import javax.net.ssl.SSLEngine;

public class NettyServer {

    private final boolean ssl = true;
    private final StatsListener statsListener = new StatsListener();
    private final PingListener pingListener = new PingListener();

    public static void main(String[] args) throws Exception{
        new NettyServer().run();
    }

    private void run() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .localAddress(8976)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (ssl) {
                            SSLEngine sslEngine = ClasspathKeystoreSocketFactory.getSSLContext().createSSLEngine();
                            sslEngine.setUseClientMode(false);
                            pipeline.addFirst(new SslHandler(sslEngine));
                        }
                        pipeline.addLast(new ISOMsgEncoder());
                        pipeline.addLast(new ISOMsgDecoder());
                        pipeline.addLast(new ISORequestListenerAdaptor(statsListener, pingListener));
                    }
                });
        b.bind().sync();
        System.out.println("Listening");
    }
}
