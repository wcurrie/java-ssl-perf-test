package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.XMLPackager;

import java.util.Date;

public class Messages {

    static final ISOPackager PACKAGER = newPackager();

    public static ISOMsg startMonitoring() throws ISOException {
        ISOMsg msg = new ISOMsg("0900");
        msg.set(70, "1");
        return msg;
    }

    public static ISOMsg collectStats(long since) throws ISOException {
        ISOMsg msg = new ISOMsg("0900");
        msg.set(70, "2");
        msg.set("48.1", String.valueOf(since));
        return msg;
    }

    public static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48", "Hi " + new Date());
        return msg;
    }

    public static byte[] pack(ISOMsg msg) {
        try {
            return PACKAGER.pack(msg);
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ISOMsg unpack(byte[] bytes) {
        try {
            ISOMsg m = new ISOMsg();
            PACKAGER.unpack(m, bytes);
            return m;
        } catch (ISOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ISOPackager newPackager() {
        try {
            return new XMLPackager();
        } catch (ISOException e) {
            throw new RuntimeException();
        }
    }

    public static ISOMsg pong(ISOMsg m) throws ISOException {
        ISOMsg response = (ISOMsg) m.clone();
        response.setResponseMTI();
        return response;
    }
}
