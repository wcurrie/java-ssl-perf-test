package x;

import org.jpos.iso.ISOServer;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.ThreadPool;
import x.tcnative.TcNativeServerSocketFactory;
import x.tcnative.TcNativeXMLChannel;

public class TcNativeServer {
    static {
        System.setProperty("java.security.egd", "file:/dev/./urandom");
    }

    public static final int PORT = 8976;

    public static void main(String[] args) throws Exception {
        Logger logger = new Logger();
        logger.setName("logger");
        logger.addListener(new ErrorLogListener());

        XMLChannel channel = new TcNativeXMLChannel(new XMLPackager());
        if (ErrorLogListener.isDebug()) {
            channel.setLogger(logger, "server");
        }
        ISOServer server = new ISOServer(PORT, channel, new ThreadPool(100, 10000));
        server.setSocketFactory(new TcNativeServerSocketFactory());
        server.addISORequestListener(new PingListener());
        server.addISORequestListener(new StatsListener());
        server.setLogger(logger, "server");
        System.out.println("Listening on " + PORT + " ssl tomcat native");
        server.run();
    }

}
