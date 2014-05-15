import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import x.ClasspathKeystoreSocketFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class Client {

    public static final String HOST = "192.168.0.6";

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        try {
            runTest(executor);
        } finally {
            executor.shutdown();
        }
    }

    private static void runTest(ExecutorService executor) throws Exception {
        long t = System.currentTimeMillis();
        List<Future<Long>> rttFutures = kickOffPings(executor, 1000);
        SummaryStatistics statistics = summarise(rttFutures);
        long elapsed = System.currentTimeMillis() - t;

        System.out.printf("Took %dms%n", elapsed);
        System.out.println(statistics);
    }

    private static SummaryStatistics summarise(List<Future<Long>> rttFutures) throws Exception {
        SummaryStatistics statistics = new SummaryStatistics();
        for (Future<Long> f : rttFutures) {
            Long rtt = f.get(5, TimeUnit.SECONDS);
            statistics.addValue(rtt);
        }
        return statistics;
    }

    private static List<Future<Long>> kickOffPings(ExecutorService executor, int clients) {
        List<Future<Long>> rttFutures = new ArrayList<Future<Long>>();
        for (int i = 0; i < clients; i++) {
            rttFutures.add(executor.submit(new ConnectAndPing()));
        }
        return rttFutures;
    }

    private static class ConnectAndPing implements Callable<Long> {
        @Override
        public Long call() throws Exception {
            XMLChannel channel = new XMLChannel(HOST, 8976, new XMLPackager());
            channel.setSocketFactory(ClasspathKeystoreSocketFactory.CLIENT);
            channel.setTimeout(5000);
            channel.connect();
            long t = System.currentTimeMillis();
            channel.send(ping());
            channel.receive();
            long rtt = System.currentTimeMillis() - t;
            channel.disconnect();
            return rtt;
        }
    }

    private static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48", "Hi " + new Date());
        return msg;
    }
}
