package cn.hjf.downloader;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class Range implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Range INVALID_RANGE = new Range(-1, -1);

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
