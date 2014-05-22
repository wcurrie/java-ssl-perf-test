package x.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.io.FileUtils;
import x.*;

import javax.net.ssl.SSLEngine;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class NettyClientRunner {

    public static final String HOST = "192.168.0.6";

    private int pingCount;

    public static void main(String[] args) throws Exception {
        ClasspathKeystoreSocketFactory.setKeyLength(KeyLength.Key_2048);
        new NettyClientRunner().run();
    }

    private void run() throws Exception {
        pingCount = 1000;
        Client.ssl = false;
        ClasspathKeystoreSocketFactory.clientSessionCacheEnabled = true;

        StatsCollector.startMonitoring(HOST);

        long t = System.currentTimeMillis();
        Results results = runTest();
        long elapsed = System.currentTimeMillis() - t;

        String serverCpuStats = StatsCollector.collectStats(HOST, t);

        String report = String.format("Took %dms%n%s", elapsed, results);
        System.out.println(report);
        String runName = runName();
        results.toCsv(runName + ".csv", t);
        FileUtils.writeStringToFile(new File(runName + ".txt"), report);
        FileUtils.writeStringToFile(new File(runName + "-server-cpu.csv"), serverCpuStats);
    }

    private Results runTest() throws InterruptedException {
        final LinkedBlockingQueue<Result> resultQueue = new LinkedBlockingQueue<Result>();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(HOST, 8976))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            SSLEngine sslEngine = ClasspathKeystoreSocketFactory.getSSLContext().createSSLEngine();
                            sslEngine.setUseClientMode(true);

                            if (Client.ssl) {
                                ch.pipeline().addFirst(new SslHandler(sslEngine));
                            }
                            ch.pipeline().addLast(new EchoClientHandler(resultQueue));
                        }
                    });
            List<ChannelFuture> closeFutures = new ArrayList<ChannelFuture>();
            for (int i = 0; i < pingCount; i++) {
                ChannelFuture f = b.connect().channel().closeFuture();
                closeFutures.add(f);
            }
            System.out.println("Waiting for closes");
            for (ChannelFuture f : closeFutures) {
                f.await();
            }
        } finally {
            System.out.println("Shutting down");
            group.shutdownGracefully().sync();
            System.out.println("Shut down");
        }
        Results results = new Results();
        for (Result result : resultQueue) {
            results.add(result);
        }
        return results;
    }

    private String runName() {
        String sslConfig;
        if (Client.ssl) {
            sslConfig = (ClasspathKeystoreSocketFactory.clientSessionCacheEnabled ? "with" : "no") + "-session-cache";
        } else {
            sslConfig = "plaintext";
        }
        return String.format("results/netty-%s-pings-%s", pingCount, sslConfig);
    }

}
