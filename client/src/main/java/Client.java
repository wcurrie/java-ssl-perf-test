import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import x.ClasspathKeystoreSocketFactory;

import java.util.Date;

public class Client {

    public static void main(String[] args) throws Exception {
        XMLChannel channel = new XMLChannel("192.168.0.6", 8976, new XMLPackager());
        channel.setSocketFactory(ClasspathKeystoreSocketFactory.newClientSocketFactory());
        channel.connect();
        long t = System.currentTimeMillis();
        channel.send(ping());
        channel.receive();
        long rtt = System.currentTimeMillis() - t;
        System.out.printf("Took %dms%n", rtt);
    }

    private static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48", "Hi " + new Date());
        return msg;
    }
}
