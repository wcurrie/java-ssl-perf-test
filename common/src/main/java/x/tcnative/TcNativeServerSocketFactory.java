package x.tcnative;

import org.apache.tomcat.jni.Library;
import org.apache.tomcat.jni.SSL;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class TcNativeServerSocketFactory implements ISOServerSocketFactory {
    public TcNativeServerSocketFactory() throws Exception {
        Library.initialize(null);
        SSL.initialize(null);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException, ISOException {
        return new TcNativeServerSocket(port);
    }
}
