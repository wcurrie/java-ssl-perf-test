package x;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Results {

    private final Frequency outcomes;
    private final List<Result> all;

    public Results() {
        outcomes = new Frequency();
        all = new ArrayList<Result>();
    }

    public void add(Result result) {
        outcomes.addValue(result.getOutcome());
        all.add(result);
    }

    public void addOutlier(String s) {
        outcomes.addValue("fail: " + s);
    }

    public static String getPercentiles(DescriptiveStatistics statistics) {
        StringWriter writer = new StringWriter();
        for (Integer i : Arrays.asList(50, 80, 90, 95, 98, 99)) {
            double p = statistics.getPercentile(i);
            writer.write(String.format("%d%% = %d%n", i, (int) p));
        }
        return writer.toString();
    }

    public DescriptiveStatistics getConnectTimes() {
        DescriptiveStatistics s = new DescriptiveStatistics();
        for (Result result : all) {
            if (result.isSuccess()) {
                s.addValue(result.getConnectTime());
            }
        }
        return s;
    }

    public DescriptiveStatistics getRtts() {
        DescriptiveStatistics s = new DescriptiveStatistics();
        for (Result result : all) {
            if (result.isSuccess()) {
                s.addValue(result.getRtt());
            }
        }
        return s;
    }

    @Override
    public String toString() {
        return "rtts:\n" + format(getRtts()) +
                "connects:\n" + format(getConnectTimes()) +
                "\noutcomes:" + outcomes;
    }

    private String format(DescriptiveStatistics s) {
        return  "n: " + (int) s.getN() + "\n" +
                "min: " + (int) s.getMin() + "\n" +
                "max: " + (int) s.getMax() + "\n" +
                "mean: " + (int) s.getMean() + "\n" +
                getPercentiles(s);
    }

    public void toCsv(String file, long t) throws IOException {
        Collections.sort(all);
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
//            writer.println("time,rtt,outcome");
            for (Result result : all) {
                writer.printf("%d,%d,%s,%d%n", result.getStart() - t, result.getRtt(), result.getOutcome(), result.getEnd() == -1 ? -1 : result.getEnd() - t);
            }
        } finally {
            writer.close();
        }
    }
}
