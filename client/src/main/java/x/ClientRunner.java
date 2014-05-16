package x;

import org.apache.commons.io.FileUtils;
import org.jpos.iso.channel.XMLChannel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class ClientRunner {

    public static final String HOST = "192.168.0.6";
    private int nThreads;
    private int pingCount;

    private CyclicBarrier cyclicBarrier;
    private ExecutorService executor;
    private ExecutorService cleaner;

    public static void main(String[] args) throws Exception {
        ClasspathKeystoreSocketFactory.setKeyLength(KeyLength.Key_2048);
        new ClientRunner().run();
    }

    private void run() throws Exception {
        nThreads = 100;
        pingCount = 10000;
        ClasspathKeystoreSocketFactory.clientSessionCacheEnabled = true;

        cyclicBarrier = new CyclicBarrier(nThreads);
        executor = Executors.newFixedThreadPool(nThreads);
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
        List<Future<Result>> rttFutures = kickOffPings(pingCount);
        Results results = summarise(rttFutures);
        long elapsed = System.currentTimeMillis() - t;

        System.out.printf("Took %dms%n", elapsed);
        System.out.println(results);
        String runName = String.format("results/%s-threads-%s-pings-%s-session-cache", nThreads, pingCount, ClasspathKeystoreSocketFactory.clientSessionCacheEnabled ? "with" : "no");
        results.toCsv(runName + ".csv");
        FileUtils.writeStringToFile(new File(runName + ".txt"), results.toString());
    }

    private Results summarise(List<Future<Result>> rttFutures) throws Exception {
        Results results = new Results();
        int remaining = pingCount;
        for (Future<Result> f : rttFutures) {
            try {
                results.add(f.get(5, TimeUnit.SECONDS));
            } catch (ExecutionException e) {
                results.addOutlier(e.getCause().toString());
            } catch (TimeoutException e) {
                results.addOutlier(e.toString());
            } finally {
                remaining--;
                if (remaining < nThreads) {
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
            XMLChannel channel = Client.newChannel(HOST);
            long t = System.currentTimeMillis();
            try {
                channel.connect();
                long connectTime = System.currentTimeMillis() - t;
                rallyBeforeACharge();
                long rtt = Client.timeToPing(channel);
                return new Result(t, rtt, connectTime);
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

}
