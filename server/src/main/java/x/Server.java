package x;

import org.jpos.iso.ISOServer;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static x.ClasspathKeystoreSocketFactory.KeyLength;

public class Server {
    static {
        System.setProperty("java.security.egd", "file:/dev/./urandom");
    }

    public static final int PORT = 8976;

    public static void main(String[] args) throws Exception {
        KeyLength keyLength = null;
        if (args.length > 0) {
            keyLength = KeyLength.valueOf(args[0]);
        }
        Logger logger = new Logger();
        logger.setName("logger");
        logger.addListener(new ErrorLogListener());

        XMLChannel channel = new XMLChannel(new XMLPackager());
        if (ErrorLogListener.isDebug()) {
            channel.setLogger(logger, "server");
        }
        ISOServer server = new ISOServer(PORT, channel, new ThreadPool(100, 10000));
        if (keyLength != null) {
            ClasspathKeystoreSocketFactory.setKeyLength(keyLength);
            server.setSocketFactory(ClasspathKeystoreSocketFactory.SERVER);
        }
        server.addISORequestListener(new PingListener());
        server.addISORequestListener(new StatsListener());
        server.setLogger(logger, "server");
        System.out.println("Listening on " + PORT + " " + (keyLength == null ? "plain" : "ssl " + keyLength));
        server.run();
    }

}
