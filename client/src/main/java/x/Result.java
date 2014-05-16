package x;

public class Result implements Comparable<Result> {

    private final long start;
    private final Exception fail;
    private final long rtt;
    private final long connectTime;

    public Result(long start, long rtt, long connectTime) {
        this.start = start;
        this.rtt = rtt;
        this.connectTime = connectTime;
        this.fail = null;
    }

    public Result(long start, Exception fail) {
        this.start = start;
        this.rtt = -1;
        this.connectTime = -1;
        this.fail = fail;
    }

    public boolean isSuccess() {
        return fail == null;
    }

    public long getStart() {
        return start;
    }

    public long getRtt() {
        return rtt;
    }

    public long getConnectTime() {
        return connectTime;
    }

    public String getOutcome() {
        return isSuccess() ? "success" : fail.toString();
    }

    @Override
    public int compareTo(Result o) {
        return Long.compare(this.getStart(), o.getStart());
    }
}
