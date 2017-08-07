package cn.hjf.downloader;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long total;
    private volatile long downloaded;

    public Progress(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public long getDownloaded() {
        return downloaded;
    }

    void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    boolean finished() {
        return total == downloaded;
    }
}
