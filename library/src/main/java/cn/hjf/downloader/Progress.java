package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Progress {

    private long total;
    private Range downloadRange;

    public Progress(long total, Range downloadRange) {
        this.total = total;
        this.downloadRange = downloadRange;
    }

    public long getTotal() {
        return total;
    }

    public Range getDownloadRange() {
        return downloadRange;
    }

    void setDownloadRange(Range downloadRange) {
        this.downloadRange = downloadRange;
    }

    void setTotal(long total) {
        this.total = total;
    }
}
