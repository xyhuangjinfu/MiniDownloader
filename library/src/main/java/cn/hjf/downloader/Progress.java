package cn.hjf.downloader;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private Range downloadRange;

    public Progress() {
    }

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
