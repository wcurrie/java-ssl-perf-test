import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import x.ClasspathKeystoreSocketFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class Client {

    public static final String HOST = "192.168.0.6";
    public static final int N_THREADS = 20;
    public static final int PING_COUNT = 1000;

    private CyclicBarrier cyclicBarrier;
    private ExecutorService executor;
    private ExecutorService cleaner;
    private DescriptiveStatistics rtts;
    private Frequency outcomes;

    public static void main(String[] args) throws Exception {
        ClasspathKeystoreSocketFactory.setKeyLength(KeyLength.Key_2048);
        new Client().run();
    }

    private void run() throws Exception {
        cyclicBarrier = new CyclicBarrier(N_THREADS);
        executor = Executors.newFixedThreadPool(N_THREADS);
        cleaner = Executors.newSingleThreadExecutor();
        rtts = new DescriptiveStatistics();
        outcomes = new Frequency();
        try {
            runTest();
        } finally {
            executor.shutdown();
            cleaner.shutdown();
        }
    }

    private void runTest() throws Exception {
        long t = System.currentTimeMillis();
        List<Future<Long>> rttFutures = kickOffPings(PING_COUNT);
        summarise(rttFutures);
        long elapsed = System.currentTimeMillis() - t;

        System.out.printf("Took %dms%n", elapsed);
        System.out.println(rtts);
        for (Integer i : Arrays.asList(50, 80, 90, 95, 98, 99)) {
            double p = rtts.getPercentile(i);
            System.out.printf("%d%% = %f%n", i, p);
        }
        System.out.println(outcomes);
    }

    private void summarise(List<Future<Long>> rttFutures) throws Exception {
        int remaining = PING_COUNT;
        for (Future<Long> f : rttFutures) {
            try {
                Long rtt = f.get(5, TimeUnit.SECONDS);
                rtts.addValue(rtt);
                outcomes.addValue("success");
            } catch (ExecutionException e) {
                outcomes.addValue(e.getCause().toString());
            } catch (TimeoutException e) {
                outcomes.addValue(e.toString());
            } finally {
                remaining--;
                if (remaining < N_THREADS) {
                    // don't let the final few get stuck waiting for a job that will never come
                    cyclicBarrier.reset();
                }
            }
        }
    }

    private List<Future<Long>> kickOffPings(int clients) {
        List<Future<Long>> rttFutures = new ArrayList<Future<Long>>();
        for (int i = 0; i < clients; i++) {
            rttFutures.add(executor.submit(new ConnectAndPing()));
        }
        return rttFutures;
    }

    private class ConnectAndPing implements Callable<Long> {

        @Override
        public Long call() throws Exception {
            XMLChannel channel = newChannel();
            rallyBeforeACharge();
            long t = System.currentTimeMillis();
            channel.send(ping());
            channel.receive();
            long rtt = System.currentTimeMillis() - t;
            cleaner.submit(disconnect(channel));
            return rtt;
        }

        private void rallyBeforeACharge() throws InterruptedException {
            try {
                cyclicBarrier.await();
            } catch (BrokenBarrierException ignored) {
            }
        }
    }

    private Runnable disconnect(final XMLChannel channel) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    channel.disconnect();
                } catch (IOException ignored) {
                }
            }
        };
    }

    private static XMLChannel newChannel() throws ISOException, IOException {
        XMLChannel channel = new XMLChannel(HOST, 8976, new XMLPackager());
        channel.setSocketFactory(ClasspathKeystoreSocketFactory.CLIENT);
        channel.setTimeout(5000);
        channel.connect();
        return channel;
    }

    private static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48", "Hi " + new Date());
        return msg;
    }

}
