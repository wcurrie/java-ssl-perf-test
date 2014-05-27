package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import x.tcnative.HandshakeTiming;

import java.io.IOException;
import java.util.List;

public class StatsListener implements ISORequestListener {

    private CpuPoller cpuPoller;

    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        if (!"0900".equals(m.getString(0))) {
            return false;
        }
        try {
            String nmic = m.getString(70);
            if ("1".equals(nmic)) {
                cpuPoller = new CpuPoller();
                source.send(ack(m));
            } else if ("2".equals(nmic)) {
                cpuPoller.die();
                List<CpuPoller.Stat> stats = cpuPoller.since(Long.parseLong(m.getString("48.1")));
                ISOMsg response = ack(m);
                response.set("48.2", CpuPoller.Stat.toCsv(stats));
                source.send(response);
            } else if ("3".equals(nmic)) {
                HandshakeTiming.clear();
                source.send(ack(m));
            } else if ("4".equals(nmic)) {
                ISOMsg response = ack(m);
                response.set("48.1", HandshakeTiming.toCsv(Long.parseLong(m.getString("48.1"))));
                source.send(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private ISOMsg ack(ISOMsg m) throws ISOException {
        ISOMsg r = (ISOMsg) m.clone();
        r.setResponseMTI();
        return r;
    }
}
