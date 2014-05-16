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

        ISOServer server = new ISOServer(PORT, new XMLChannel(new XMLPackager()), new ThreadPool(100, 10000));
        if (keyLength != null) {
            ClasspathKeystoreSocketFactory.setKeyLength(keyLength);
            server.setSocketFactory(ClasspathKeystoreSocketFactory.SERVER);
        }
        server.addISORequestListener(new PingListener());
        server.setLogger(logger, "server");
        System.out.println("Listening on " + PORT + " " + (keyLength == null ? "plain" : "ssl " + keyLength));
        server.run();
    }

    private static class ErrorLogListener extends SimpleLogListener {
        private static final Set<String> EXCLUDED_TAGS = new HashSet<String>(Arrays.asList("session-start", "session-end"));

        @Override
        public synchronized LogEvent log(LogEvent ev) {
            if (shouldLog(ev)) {
                return super.log(ev);
            }
            return ev;
        }

        private boolean shouldLog(LogEvent ev) {
            return !EXCLUDED_TAGS.contains(ev.getTag());
        }
    }
}
