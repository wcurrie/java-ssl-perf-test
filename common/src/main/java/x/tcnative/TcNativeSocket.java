package x.tcnative;

import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Sockaddr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcNativeSocket extends Socket {

    private final long clientSock;
    private String name;
    private int remotePort;
    private int localPort;

    public TcNativeSocket(long clientSock) {
        this.clientSock = clientSock;
        try {
            long sa = Address.get(org.apache.tomcat.jni.Socket.APR_REMOTE, clientSock);
            Sockaddr raddr = new Sockaddr();
            if (Address.fill(raddr, sa)) {
                this.name = Address.getip(sa);
                remotePort = raddr.port;
            }
            sa = Address.get(org.apache.tomcat.jni.Socket.APR_LOCAL, clientSock);
            Sockaddr laddr = new Sockaddr();
            if (Address.fill(laddr, sa)) {
                localPort = laddr.port;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InetAddress getInetAddress() {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    @Override
    public int getPort() {
        return remotePort;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new TcInputStream(clientSock);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new TcOutputStream(clientSock);
    }
}
