package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

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
            if ("1".equals(m.getString(70))) {
                cpuPoller = new CpuPoller();
                source.send(ack(m));
            }
            if ("2".equals(m.getString(70))) {
                cpuPoller.die();
                List<CpuPoller.Stat> stats = cpuPoller.since(Long.parseLong(m.getString("48.1")));
                ISOMsg response = ack(m);
                response.set("48.2", toCsv(stats));
                source.send(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private String toCsv(List<CpuPoller.Stat> stats) {
        StringBuilder b = new StringBuilder();
        for (CpuPoller.Stat s : stats) {
            b.append(s.getTime()).append(",").append(s.getLoadAverage()).append("\n");
        }
        return b.toString();
    }

    private ISOMsg ack(ISOMsg m) throws ISOException {
        ISOMsg r = (ISOMsg) m.clone();
        r.setResponseMTI();
        return r;
    }
}
