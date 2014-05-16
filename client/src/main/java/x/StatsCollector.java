package x;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;

public class StatsCollector {
    public static void main(String[] args) throws Exception {
//        startMonitoring(ClientRunner.HOST);
//        Thread.sleep(1000);
        String stats = collectStats(ClientRunner.HOST, 0);
        System.out.println(stats);
    }

    private static String collectStats(String host, long since) throws Exception {
        XMLChannel channel = Client.newChannel(host);
        channel.connect();
        channel.send(Messages.collectStats(since));
        ISOMsg response = channel.receive();
        channel.disconnect();
        return response.getString("48.2");
    }

    public static void startMonitoring(String host) throws Exception {
        XMLChannel channel = Client.newChannel(host);
        channel.connect();
        channel.send(Messages.startMonitoring());
        channel.receive();
        channel.disconnect();
    }
}
