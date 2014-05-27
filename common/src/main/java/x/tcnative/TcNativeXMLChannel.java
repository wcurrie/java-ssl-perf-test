package x.tcnative;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;

import java.io.IOException;

public class TcNativeXMLChannel extends XMLChannel {
    private boolean sslHandshakeDone;

    public TcNativeXMLChannel(XMLPackager packager) throws IOException {
        super(packager);
    }

    @Override
    public ISOMsg receive() throws IOException, ISOException {
        if (!sslHandshakeDone) {
            System.out.println(Thread.currentThread() + " handshaking");
            TcNativeSocket socket = (TcNativeSocket) getSocket();
            socket.doSslHandshake();
            sslHandshakeDone = true;
        }
        return super.receive();
    }
}
