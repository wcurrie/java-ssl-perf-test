package x.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.jpos.iso.ISOException;
import x.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class NettyClientRunner {

    public static final String HOST = "192.168.0.6";

    private int pingCount;
    private SSLContext sharedSslContext = null;

    public static void main(String[] args) throws Exception {
        ClasspathKeystoreSocketFactory.setKeyLength(KeyLength.Key_2048);
        new NettyClientRunner().run();
    }

    private void run() throws Exception {
        pingCount = 1000;
        Client.ssl = true;
        ClasspathKeystoreSocketFactory.clientSessionCacheEnabled = true;

        StatsCollector.startMonitoring(HOST);
        StatsCollector.startLocalMonitoring();

        long t = System.currentTimeMillis();
        Results results = runTest();
        long elapsed = System.currentTimeMillis() - t;

        String serverCpuStats = StatsCollector.collectStats(HOST, t);
        String clientCpuStats = StatsCollector.collectLocalStats(t);

        String report = String.format("Took %dms%n%s", elapsed, results);
        System.out.println(report);
        String runName = runName();
        results.toCsv(runName + ".csv", t);
        FileUtils.writeStringToFile(new File(runName + ".txt"), report);
        FileUtils.writeStringToFile(new File(runName + "-server-cpu.csv"), serverCpuStats);
        FileUtils.writeStringToFile(new File(runName + "-client-cpu.csv"), clientCpuStats);
        System.out.println("wrote " + runName);
        GnuPlot.plot(runName);
    }

    private Results runTest() throws InterruptedException, ISOException {
        if (ClasspathKeystoreSocketFactory.clientSessionCacheEnabled) {
            sharedSslContext = ClasspathKeystoreSocketFactory.getSSLContext();
        }
        final Queue<Result> resultQueue = new ConcurrentLinkedQueue<Result>();
        EventLoopGroup group = new NioEventLoopGroup(50);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(HOST, 8976))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            SSLContext sslContext = sharedSslContext != null ? sharedSslContext : ClasspathKeystoreSocketFactory.getSSLContext();
                            SSLEngine sslEngine = sslContext.createSSLEngine();
                            sslEngine.setUseClientMode(true);

                            ChannelPipeline pipeline = ch.pipeline();
                            if (Client.ssl) {
                                pipeline.addFirst(new SslHandler(sslEngine));
                            }
                            pipeline.addLast(new ISOMsgEncoder());
                            pipeline.addLast(new ISOMsgDecoder());
                            pipeline.addLast(new EchoClientHandler(resultQueue));
                        }
                    });
            List<ChannelFuture> futures = new ArrayList<ChannelFuture>();
            for (int i = 0; i < pingCount; i++) {
                futures.add(b.connect());
            }
            System.out.println(new DateTime() + ": Waiting for closes");
            for (ChannelFuture f : futures) {
                f.channel().closeFuture().await();
            }
        } finally {
            System.out.println(new DateTime() + ": Shutting down");
            group.shutdownGracefully().sync();
            System.out.println(new DateTime() + ": Shut down");
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
