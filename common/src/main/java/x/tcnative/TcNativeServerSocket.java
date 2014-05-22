package x.tcnative;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.jni.*;

import java.io.*;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import static org.apache.tomcat.jni.Socket.*;

public class TcNativeServerSocket extends ServerSocket {

    private final long serverPool;
    private final long serverCtx;
    private long pool;
    private long serverSock;

    public TcNativeServerSocket(int port) throws IOException {
        serverPool = Pool.create(0);
        try {
            serverCtx = SSLContext.make(serverPool, SSL.SSL_PROTOCOL_ALL, SSL.SSL_MODE_SERVER);
            /* List the ciphers that the client is permitted to negotiate. */
            SSLContext.setCipherSuite(serverCtx, "ALL");
            /* Load Server key and certificate */
            SSLContext.setCertificate(serverCtx, toPath("cert.crt"), toPath("key.pem"), "password", SSL.SSL_AIDX_RSA);
            SSLContext.setVerify(serverCtx, SSL.SSL_CVERIFY_NONE, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        bind(port);
    }

    private String toPath(String s) throws IOException {
        InputStream in = TcNativeServerSocket.class.getClassLoader().getResourceAsStream(s);
        java.io.File tempFile = File.createTempFile("tc-socket", "");
        tempFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(in, tempFile);
        in.close();
        return tempFile.getAbsolutePath();
    }

    private void bind(int port) {
        pool = Pool.create(serverPool);
        try {
            long inetAddress = Address.info("127.0.0.1", APR_INET, port, 0, pool);
            serverSock = create(APR_INET, SOCK_STREAM, APR_PROTO_TCP, pool);
            int rc = org.apache.tomcat.jni.Socket.bind(serverSock, inetAddress);
            if (rc != 0) {
                throw new Exception("Can't create Acceptor: bind: " + org.apache.tomcat.jni.Error.strerror(rc));
            }
            listen(serverSock, 5);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bind(SocketAddress endpoint) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket accept() throws IOException {
        try {
            long clientSock = org.apache.tomcat.jni.Socket.accept(serverSock);
            org.apache.tomcat.jni.Socket.timeoutSet(clientSock, -1);
            SSLSocket.attach(serverCtx, clientSock);
            int i = SSLSocket.handshake(clientSock);
            if (i != 0) {
                org.apache.tomcat.jni.Socket.destroy(clientSock);
                throw new RuntimeException("Handshake error: " + SSL.getLastError());
            }
            return new TcNativeSocket(clientSock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
