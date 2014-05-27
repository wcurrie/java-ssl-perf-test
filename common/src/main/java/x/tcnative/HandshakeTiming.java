package x.tcnative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandshakeTiming {

    private static final List<Timing> TIMINGS = Collections.synchronizedList(new ArrayList<Timing>());

    public static void record(long start, long end) {
        TIMINGS.add(new Timing(start, end));
    }

    public static void clear() {
        TIMINGS.clear();
    }

    public static String toCsv(long baseLine) {
        StringBuilder b = new StringBuilder();
        for (Timing timing : TIMINGS) {
            b.append(timing.start - baseLine).append(',')
                    .append(timing.end - baseLine).append(',')
                    .append(timing.getElapsed()).append('\n');
        }
        return b.toString();
    }

    private static class Timing {
        private final long start;
        private final long end;

        private Timing(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getElapsed() {
            return end - start;
        }
    }
}
