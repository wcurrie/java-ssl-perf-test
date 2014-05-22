package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.IOException;

public class Client {

    public static boolean ssl = true;

    public static void main(String[] args) throws IOException, ISOException {
        Logger logger = new Logger();
        logger.setName("logger");
        logger.addListener(new SimpleLogListener());

        XMLChannel channel = newChannel("localhost");
        channel.setLogger(logger, "client");
        channel.connect();
        long pingStart = System.currentTimeMillis();
        ping(channel);
        long l = System.currentTimeMillis() - pingStart;
        channel.disconnect();

        System.out.printf("%dms%n", l);
    }

    public static XMLChannel newChannel(String host) {
        try {
            XMLChannel channel = new XMLChannel(host, 8976, Messages.PACKAGER);
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

    public static void ping(XMLChannel channel) throws IOException, ISOException {
        channel.send(Messages.ping());
        channel.receive();
    }

}
