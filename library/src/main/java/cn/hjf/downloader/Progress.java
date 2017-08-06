package cn.hjf.downloader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private long download;
    private List<Range> downloadedRanges;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getDownload() {
        return download;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public List<Range> getDownloadedRanges() {
        return downloadedRanges;
    }

    public void setDownloadedRanges(List<Range> downloadedRanges) {
        this.downloadedRanges = downloadedRanges;
    }

    public boolean finish() {
        return total == download;
    }
}
