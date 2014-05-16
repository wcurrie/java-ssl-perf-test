package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class Messages {

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
}
