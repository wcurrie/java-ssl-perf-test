package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;

import java.io.IOException;
import java.util.Date;

public class Client {

    private static final ISOPackager PACKAGER = newPackager();
    public static boolean ssl;

    private static ISOPackager newPackager() {
        try {
            return new XMLPackager();
        } catch (ISOException e) {
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) throws IOException, ISOException {
        Logger logger = new Logger();
        logger.setName("logger");

        XMLChannel channel = newChannel("192.168.0.6");
        channel.setLogger(logger, "client");
        channel.connect();
        long l = timeToPing(channel);
        channel.disconnect();

        System.out.printf("%dms%n", l);
    }

    public static XMLChannel newChannel(String host) {
        try {
            XMLChannel channel = new XMLChannel(host, 8976, PACKAGER);
            if (ssl) {
                channel.setSocketFactory(ClasspathKeystoreSocketFactory.CLIENT);
            }
            channel.setTimeout(5000);
            channel.setSoLinger(true, 0); // disconnect quickly
            return channel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long timeToPing(XMLChannel channel) throws IOException, ISOException {
        long pingStart = System.currentTimeMillis();
        channel.send(ping());
        channel.receive();
        return System.currentTimeMillis() - pingStart;
    }

    private static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48", "Hi " + new Date());
        return msg;
    }
}
