package x.tcnative;

import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLSocket;
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

    @Override
    public void shutdownOutput() throws IOException {
    }

    @Override
    public synchronized void close() throws IOException {
        org.apache.tomcat.jni.Socket.close(clientSock);
    }

    // so it's not on the server accept thread. good idea?
    public void doSslHandshake() {
        long start = System.currentTimeMillis();
        int i = SSLSocket.handshake(clientSock);
        if (i != 0) {
            org.apache.tomcat.jni.Socket.close(clientSock);
            throw new RuntimeException("Handshake error: " + SSL.getLastError());
        }
        long end = System.currentTimeMillis();
        HandshakeTiming.record(start, end);
    }
}
