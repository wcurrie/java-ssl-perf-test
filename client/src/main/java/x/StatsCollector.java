package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;

import java.io.IOException;

public class StatsCollector {

    private static CpuPoller cpuPoller;

    public static void startMonitoring(String host) throws Exception {
        send(host, Messages.startMonitoring());
    }

    public static String collectCpuStats(String host, long since) throws Exception {
        ISOMsg response = send(host, Messages.collectStats(since));
        return response.getString("48.2");
    }

    public static void clearHandshakeTimings(String host) throws Exception {
        send(host, Messages.clearHandshakeTimings());
    }

    public static String collectHandshakeTimings(String host, long since) throws Exception {
        ISOMsg response = send(host, Messages.collectHandshakeTimings(since));
        return response.getString("48.1");
    }

    private static ISOMsg send(String host, ISOMsg request) throws IOException, ISOException {
        XMLChannel channel = Client.newChannel(host);
        channel.connect();
        channel.send(request);
        ISOMsg response = channel.receive();
        channel.disconnect();
        return response;
    }

    public static void startLocalMonitoring() {
        cpuPoller = new CpuPoller();
    }

    public static String collectLocalCpuStats(long since) {
        try {
            cpuPoller.die();
            return CpuPoller.Stat.toCsv(cpuPoller.since(since));
        } finally {
            cpuPoller = null;
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "192.168.0.6";
        startMonitoring(host);
        Thread.sleep(1000);
        System.out.println(collectCpuStats(host, System.currentTimeMillis() - 500));
    }
}
