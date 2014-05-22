package x.tcnative;

import org.apache.tomcat.jni.Socket;

import java.io.IOException;
import java.io.OutputStream;

public class TcOutputStream extends OutputStream {
    private final long clientSock;

    public TcOutputStream(long clientSock) {
        this.clientSock = clientSock;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int i = Socket.send(clientSock, b, off, len);
        if (i != len) {
            throw new IOException("Write failed " + i);
        }
    }

    @Override
    public void close() throws IOException {
        Socket.destroy(clientSock);
    }
}
