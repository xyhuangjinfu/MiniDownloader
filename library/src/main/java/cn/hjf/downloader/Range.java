package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Range {

    public static final Range INVALID = new Range(-1, -1);

    private final long start;
    private final long end;

    public Range(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
