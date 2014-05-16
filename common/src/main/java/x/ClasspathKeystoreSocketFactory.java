package x;

import org.jpos.iso.ISOClientSocketFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOServerSocketFactory;
import org.jpos.util.SimpleLogSource;

import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

public class ClasspathKeystoreSocketFactory extends SimpleLogSource
        implements ISOServerSocketFactory,ISOClientSocketFactory
{
    public static final ClasspathKeystoreSocketFactory CLIENT = new ClasspathKeystoreSocketFactory();
    public static final ClasspathKeystoreSocketFactory SERVER = new ClasspathKeystoreSocketFactory();

    private SSLContext sslc=null;
    private SSLServerSocketFactory serverFactory=null;
    private SSLSocketFactory socketFactory=null;

    private String serverName;
    private boolean clientAuthNeeded=true;
    private boolean serverAuthNeeded=false;

    private static KeyStore ks = loadKeyStore(KeyLength.Key_2048.keystore);
    private static KeyManager[] kma = loadKeyManagers(ks);
    private static TrustManager[] tma = getTrustManagers(ks);

    static {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    }

    public static boolean clientSessionCacheEnabled = false;

    public enum KeyLength {
        Key_2048("keystore.jks"),
        Key_4096("keystore-4096.jks");

        private final String keystore;

        KeyLength(String keystore) {
            this.keystore = keystore;
        }
    }

    public static void setKeyLength(KeyLength keyLength) {
        ks = loadKeyStore(keyLength.keystore);
        kma = loadKeyManagers(ks);
        tma = getTrustManagers(ks);
    }

    private boolean shareClientSslContext;

    public void setShareClientSslContext(boolean shareClientSslContext) {
        this.shareClientSslContext = shareClientSslContext;
    }

    private static TrustManager[] getTrustManagers(KeyStore ks) {
//        if (false) {
//            TrustManagerFactory tm = TrustManagerFactory.getInstance("SunX509" );
//            tm.init( ks );
//            return tm.getTrustManagers();
//        } else {
            // Create a trust manager that does not validate certificate chains
            return new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[] {};
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
//        }
    }

    private SSLContext getSSLContext() throws ISOException {
        try{
            SSLContext sslc = SSLContext.getInstance( "SSL" );
//            sslc.getClientSessionContext().setSessionCacheSize(0);
            sslc.init( kma, tma, SecureRandom.getInstance("SHA1PRNG") );
            return sslc;
        } catch(Exception e) {
            throw new ISOException (e);
        }
    }

    private static KeyManager[] loadKeyManagers(KeyStore ks) {
        try {
            KeyManagerFactory km = KeyManagerFactory.getInstance("SunX509");
            km.init( ks, "password".toCharArray() );
            return km.getKeyManagers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore loadKeyStore(String keystore) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream fis = openKeystore(keystore);
            ks.load(fis,"password".toCharArray());
            fis.close();
            return ks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream openKeystore(String keystore) throws FileNotFoundException {
        return ClasspathKeystoreSocketFactory.class.getClassLoader().getResourceAsStream(keystore);
    }

    /**
     * Create a socket factory
     * @return the socket factory
     * @exception ISOException if an error occurs during server socket
     * creation
     */
    protected SSLServerSocketFactory createServerSocketFactory()
            throws ISOException
    {
        if(sslc==null) sslc=getSSLContext();
        return sslc.getServerSocketFactory();
    }

    /**
     * Create a socket factory
     * @return the socket factory
     * @exception ISOException if an error occurs during server socket
     * creation
     */
    protected SSLSocketFactory createSocketFactory()
            throws ISOException
    {
        SSLContext context;
        if (shareClientSslContext) {
            // sketchy with oodles of clients?
            if(sslc==null) sslc=getSSLContext();
            context = sslc;
        } else {
            context = getSSLContext();
        }
        return context.getSocketFactory();
    }

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     * @param  port the port number
     * @return the server socket on the specified port
     * @exception java.io.IOException should an I/O error occurs during
     * @exception ISOException should an error occurs during
     * creation
     */
    public ServerSocket createServerSocket(int port)
            throws IOException, ISOException
    {
        if(serverFactory==null) serverFactory=createServerSocketFactory();
        ServerSocket socket = serverFactory.createServerSocket(port, 50);
        SSLServerSocket serverSocket = (SSLServerSocket) socket;
        serverSocket.setNeedClientAuth(clientAuthNeeded);
//        serverSocket.setEnabledCipherSuites(enabledCipherSuites);
        return socket;
    }

    /**
     * Create a client socket connected to the specified host and port.
     * @param  host   the host name
     * @param  port   the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @exception ISOException should any other error occurs
     */
    public Socket createSocket(String host, int port)
            throws IOException, ISOException {
            SSLSocket s;
        if (clientSessionCacheEnabled) {
            if(socketFactory==null) socketFactory=createSocketFactory();
            s = (SSLSocket) socketFactory.createSocket(host,port);
        } else {
            // keeping a socket factory seems to enable session caching even when the cache size is set to zero...
            // verify by using -Djavax.net.debug=ssl and grep -i resum
            s = (SSLSocket) createSocketFactory().createSocket(host, port);
        }
        verifyHostname(s);
        return s;
    }

    /**
     * Verify that serverName and CN equals.
     *
     * <pre>
     * Origin:      jakarta-commons/httpclient
     * File:        StrictSSLProtocolSocketFactory.java
     * Revision:    1.5
     * License:     Apache-2.0
     * </pre>
     *
     * @param socket a SSLSocket value
     * @exception SSLPeerUnverifiedException  If there are problems obtaining
     * the server certificates from the SSL session, or the server host name
     * does not match with the "Common Name" in the server certificates
     * SubjectDN.
     * @exception java.net.UnknownHostException  If we are not able to resolve
     * the SSL sessions returned server host name.
     */
    private void verifyHostname(SSLSocket socket)
            throws SSLPeerUnverifiedException, UnknownHostException
    {
        if (!serverAuthNeeded) {
            return;
        }

        SSLSession session = socket.getSession();

        if (serverName==null || serverName.length()==0) {
            serverName = session.getPeerHost();
            try {
                InetAddress addr = InetAddress.getByName(serverName);
            } catch (UnknownHostException uhe) {
                throw new UnknownHostException("Could not resolve SSL " +
                        "server name " + serverName);
            }
        }


        X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs==null || certs.length==0)
            throw new SSLPeerUnverifiedException("No server certificates found");

        //get the servers DN in its string representation
        String dn = certs[0].getSubjectDN().getName();

        //get the common name from the first cert
        String cn = getCN(dn);
        if (!serverName.equalsIgnoreCase(cn)) {
            throw new SSLPeerUnverifiedException("Invalid SSL server name. "+
                    "Expected '" + serverName +
                    "', got '" + cn + "'");
        }
    }

    /**
     * Parses a X.500 distinguished name for the value of the
     * "Common Name" field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to RFC 2253.
     *
     * <pre>
     * Origin:      jakarta-commons/httpclient
     * File:        StrictSSLProtocolSocketFactory.java
     * Revision:    1.5
     * License:     Apache-2.0
     * </pre>
     *
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    private String getCN(String dn) {
        int i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }
        //get the remaining DN without CN=
        dn = dn.substring(i + 3);
        // System.out.println("dn=" + dn);
        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\') {
                break;
            }
        }
        return dn.substring(0, i);
    }

}
