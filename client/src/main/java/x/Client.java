package x;

import org.apache.commons.io.FileUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class Client {

    public static final String HOST = "192.168.0.6";
    public static final int N_THREADS = 10;
    public static final int PING_COUNT = 1000;

    private CyclicBarrier cyclicBarrier;
    private ExecutorService executor;
    private ExecutorService cleaner;

    public static void main(String[] args) throws Exception {
        ClasspathKeystoreSocketFactory.setKeyLength(KeyLength.Key_2048);
        new Client().run();
    }

    private void run() throws Exception {
        cyclicBarrier = new CyclicBarrier(N_THREADS);
        executor = Executors.newFixedThreadPool(N_THREADS);
        cleaner = Executors.newSingleThreadExecutor();

        try {
            runTest();
        } finally {
            executor.shutdown();
            cleaner.shutdown();
        }
    }

    private void runTest() throws Exception {
        long t = System.currentTimeMillis();
        List<Future<Result>> rttFutures = kickOffPings(PING_COUNT);
        Results results = summarise(rttFutures);
        long elapsed = System.currentTimeMillis() - t;

        System.out.printf("Took %dms%n", elapsed);
        System.out.println(results);
        String runName = String.format("results/%s-threads-%s-pings-no-session-cache", N_THREADS, PING_COUNT);
        results.toCsv(runName + ".csv");
        FileUtils.writeStringToFile(new File(runName + ".txt"), results.toString());
    }

    private Results summarise(List<Future<Result>> rttFutures) throws Exception {
        Results results = new Results();
        int remaining = PING_COUNT;
        for (Future<Result> f : rttFutures) {
            try {
                results.add(f.get(5, TimeUnit.SECONDS));
            } catch (ExecutionException e) {
                results.addOutlier(e.getCause().toString());
            } catch (TimeoutException e) {
                results.addOutlier(e.toString());
            } finally {
                remaining--;
                if (remaining < N_THREADS) {
                    // don't let the final few get stuck waiting for a job that will never come
                    cyclicBarrier.reset();
                }
            }
        }
        return results;
    }

    private List<Future<Result>> kickOffPings(int clients) {
        List<Future<Result>> rttFutures = new ArrayList<Future<Result>>();
        for (int i = 0; i < clients; i++) {
            rttFutures.add(executor.submit(new ConnectAndPing()));
        }
        return rttFutures;
    }

    private class ConnectAndPing implements Callable<Result> {

        @Override
        public Result call() throws Exception {
            XMLChannel channel = null;
            long t = System.currentTimeMillis();
            try {
                channel = newChannel();
                rallyBeforeACharge();
                long pingStart = System.currentTimeMillis();
                channel.send(ping());
                channel.receive();
                long rtt = System.currentTimeMillis() - pingStart;
                return new Result(t, rtt);
            } catch (Exception e) {
                return new Result(t, e);
            } finally {
                if (channel != null) {
                    cleaner.submit(disconnect(channel));
                }
            }
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
