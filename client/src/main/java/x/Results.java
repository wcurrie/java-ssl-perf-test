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

    private final DescriptiveStatistics rtts;
    private final Frequency outcomes;
    private final List<Result> all;

    public Results() {
        rtts = new DescriptiveStatistics();
        outcomes = new Frequency();
        all = new ArrayList<Result>();
    }

    public void add(Result result) {
        if (result.isSuccess()) {
            rtts.addValue(result.getRtt());
        }
        outcomes.addValue(result.getOutcome());
        all.add(result);
    }

    public void addOutlier(String s) {
        outcomes.addValue("fail: " + s);
    }

    public String getPercentiles() {
        StringWriter writer = new StringWriter();
        for (Integer i : Arrays.asList(50, 80, 90, 95, 98, 99)) {
            double p = rtts.getPercentile(i);
            writer.write(String.format("%d%% = %f%n", i, p));
        }
        return writer.toString();
    }

    @Override
    public String toString() {
        return "rtts:\n" + rtts + "\npercentiles:\n" + getPercentiles() + "\noutcomes:" + outcomes;
    }

    public void toCsv(String file) throws IOException {
        Collections.sort(all);
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        long firstTime = all.get(0).getStart();
        try {
//            writer.println("time,rtt,outcome");
            for (Result result : all) {
                writer.printf("%d,%d,%s%n", result.getStart() - firstTime, result.getRtt(), result.getOutcome());
            }
        } finally {
            writer.close();
        }
    }
}
