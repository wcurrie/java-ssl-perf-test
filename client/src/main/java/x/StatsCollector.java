package x;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;

public class StatsCollector {

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
        return baseLineTimes(response.getString("48.2"), since);
    }

    public static String baseLineTimes(String csv, long t) {
        StringBuilder b = new StringBuilder();
        for (String line : csv.split("\n")) {
            String[] fields = line.split(",");
            b.append(Long.parseLong(fields[0]) - t);
            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                b.append(",").append(field);
            }
            b.append("\n");
        }
        return b.toString();
    }

    public static void main(String[] args) throws Exception {
        String host = "192.168.0.6";
        startMonitoring(host);
        Thread.sleep(1000);
        System.out.println(collectStats(host, System.currentTimeMillis() - 500));
    }
}
