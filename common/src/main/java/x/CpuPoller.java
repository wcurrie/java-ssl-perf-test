package x;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CpuPoller implements Runnable {

    private final MBeanServer mbs;
    private final ObjectName name;
    private final ExecutorService executorService;
    private final List<Stat> stats = new ArrayList<Stat>();
    private volatile boolean die;

    public CpuPoller() {
        mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
    }

    public List<Stat> since(long time) {
        for (int i = 0; i < stats.size(); i++) {
            Stat stat = stats.get(i);
            if (stat.getTime() >= time) {
                return stats.subList(i, stats.size());
            }
        }
        return Collections.emptyList();
    }

    public void die() {
        die = true;
        executorService.shutdown();
    }

    @Override
    public void run() {
        while (!die) {
            try {
                AttributeList list = mbs.getAttributes(name, new String[]{ "SystemLoadAverage" });
                Attribute attribute = (Attribute) list.get(0);
                double v = (Double) attribute.getValue();
                stats.add(new Stat(System.currentTimeMillis(), v));
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static class Stat {

        private final long time;
        private final double loadAverage;

        public Stat(long time, double loadAverage) {
            this.time = time;
            this.loadAverage = loadAverage;
        }

        public long getTime() {
            return time;
        }

        public double getLoadAverage() {
            return loadAverage;
        }

        @Override
        public String toString() {
            return "Stat{" +
                    "time=" + time +
                    ", loadAverage=" + loadAverage +
                    '}';
        }

    }
}
