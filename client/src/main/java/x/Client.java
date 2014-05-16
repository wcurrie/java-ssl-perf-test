package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;

import java.io.IOException;
import java.util.Date;

public class Client {

    public static void main(String[] args) throws IOException, ISOException {
        Logger logger = new Logger();
        logger.setName("logger");

        XMLChannel channel = newChannel("localhost");
        channel.setLogger(logger, "client");
        channel.connect();
        long l = timeToPing(channel);
        channel.disconnect();

        System.out.printf("%dms%n", l);
    }

    public static XMLChannel newChannel(String host) {
        try {
            XMLChannel channel = new XMLChannel(host, 8976, new XMLPackager());
//            channel.setSocketFactory(ClasspathKeystoreSocketFactory.CLIENT);
            channel.setTimeout(5000);
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
