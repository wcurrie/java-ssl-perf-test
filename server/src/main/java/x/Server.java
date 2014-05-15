package x;

import org.jpos.iso.ISOServer;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

public class Server {
    static {
        System.setProperty("java.security.egd", "file:/dev/./urandom");
    }
    public static void main(String[] args) throws Exception {
        Logger logger = new Logger();
        logger.setName("logger");
        logger.addListener(new SimpleLogListener());

        ISOServer server = new ISOServer(8976, new XMLChannel(new XMLPackager()), new ThreadPool(100, 100));
        server.setSocketFactory(ClasspathKeystoreSocketFactory.SERVER);
        server.addISORequestListener(new PingListener());
        server.setLogger(logger, "server");
        server.run();
    }

}
