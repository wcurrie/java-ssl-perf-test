package x;

public class Result implements Comparable<Result> {

    private final long start;
    private final Throwable fail;
    private final long end;
    private final long connectTime;

    public Result(long start, long end, long connectTime) {
        this.start = start;
        this.end = end;
        this.connectTime = connectTime;
        this.fail = null;
    }

    public Result(long start, Throwable fail) {
        this.start = start;
        this.end = -1;
        this.connectTime = -1;
        this.fail = fail;
    }

    public boolean isSuccess() {
        return fail == null;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getRtt() {
        return end == -1 ? -1 : end - start;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public String getOutcome() {
        return isSuccess() ? "success" : fail.toString();
    }

    @Override
    public int compareTo(Result o) {
        return compare(this.getStart(), o.getStart());
    }

    // provide jdk 1.6 support ...
    private static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
