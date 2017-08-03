package cn.hjf.downloader;

import android.support.v4.util.Pair;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        NEW,
        RUNNING,
        PAUSE,
        FINISH
    }

    private Status status;

    private String urlStr;
    private List<Pair<Long, Long>> ranges;

    private String filePath;

    private transient Listener listener;
    private transient ErrorListener errorListener;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

    public List<Pair<Long, Long>> getRanges() {
        return ranges;
    }

    public void setRanges(List<Pair<Long, Long>> ranges) {
        this.ranges = ranges;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }
}
