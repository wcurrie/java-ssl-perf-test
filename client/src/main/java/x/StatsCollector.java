package x;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;

public class StatsCollector {

    private static CpuPoller cpuPoller;

    public static void startMonitoring(String host) throws Exception {
        XMLChannel channel = Client.newChannel(host);
        channel.connect();
        channel.send(Messages.startMonitoring());
        channel.receive();
        channel.disconnect();
    }

    public static String collectStats(String host, long since) throws Exception {
        XMLChannel channel = Client.newChannel(host);
        channel.connect();
        channel.send(Messages.collectStats(since));
        ISOMsg response = channel.receive();
        channel.disconnect();
        return response.getString("48.2");
    }

    public static void startLocalMonitoring() {
        cpuPoller = new CpuPoller();
    }

    public static String collectLocalStats(long since) {
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
        System.out.println(collectStats(host, System.currentTimeMillis() - 500));
    }
}
