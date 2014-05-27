package x.jpos;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.XMLChannel;
import x.tcnative.HandshakeTiming;

import java.io.IOException;

public class HandshakeTimingXMLChannel extends XMLChannel {

    private boolean firstRead = true;

    public HandshakeTimingXMLChannel(ISOPackager packager) throws IOException {
        super(packager);
    }

    @Override
    public ISOMsg receive() throws IOException, ISOException {
        long start = System.currentTimeMillis();
        try {
            return super.receive();
        } finally {
            if (firstRead) {
                long end = System.currentTimeMillis();
                HandshakeTiming.record(start, end);
                firstRead = false;
            }
        }
    }
}
