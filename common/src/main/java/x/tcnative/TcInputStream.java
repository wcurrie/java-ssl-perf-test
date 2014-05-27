package x.tcnative;

import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.Socket;

import java.io.IOException;
import java.io.InputStream;

public class TcInputStream extends InputStream {

    private final long clientSock;

    public TcInputStream(long clientSock) {
        this.clientSock = clientSock;
    }

    @Override
    public int read() throws IOException {
        byte [] buf = new byte[1];
        System.out.println("reading");
        int ret = Socket.recv(clientSock, buf, 0, 1);
        System.out.println("ret = " + ret + " " + (char) buf[0]);
        if (ret != 1) {
            throw new IOException("Read failed (" + ret + ") " + SSL.getLastError());
        }
        return buf[0];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return Socket.recv(clientSock, b, off, len);
    }

    @Override
    public void close() throws IOException {
        Socket.close(clientSock);
    }
}
