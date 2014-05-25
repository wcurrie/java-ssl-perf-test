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
                stats.add(new Stat(
                        System.currentTimeMillis(),
                        getDouble("SystemLoadAverage"),
                        (int) (getDouble("SystemCpuLoad") * 100),
                        (int) (getDouble("ProcessCpuLoad") * 100)));
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private double getDouble(String name) throws InstanceNotFoundException, ReflectionException {
        AttributeList list = mbs.getAttributes(this.name, new String[]{name});
        Attribute attribute = (Attribute) list.get(0);
        return (Double) attribute.getValue();
    }

    public static class Stat {

        private final long time;
        private final double loadAverage;
        private final int systemCpuLoad;
        private final int processCpuLoad;

        public Stat(long time, double loadAverage, int systemCpuLoad, int processCpuLoad) {
            this.time = time;
            this.loadAverage = loadAverage;
            this.systemCpuLoad = systemCpuLoad;
            this.processCpuLoad = processCpuLoad;
        }

        public long getTime() {
            return time;
        }

        public double getLoadAverage() {
            return loadAverage;
        }

        public int getSystemCpuLoad() {
            return systemCpuLoad;
        }

        public int getProcessCpuLoad() {
            return processCpuLoad;
        }

        @Override
        public String toString() {
            return "Stat{" +
                    "time=" + time +
                    ", loadAverage=" + loadAverage +
                    '}';
        }

        public static String toCsv(List<CpuPoller.Stat> stats) {
            StringBuilder b = new StringBuilder();
            long firstTime = stats.get(0).getTime();
            for (CpuPoller.Stat s : stats) {
                b.append(s.getTime() - firstTime).append(",")
                        .append(s.getLoadAverage()).append(",")
                        .append(s.getSystemCpuLoad()).append(",")
                        .append(s.getProcessCpuLoad()).append("\n");
            }
            return b.toString();
        }
    }
}
