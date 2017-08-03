package cn.hjf.downloader;

import android.support.v4.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
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

    private final String urlStr;
    private final List<Pair<Long, Long>> ranges;

    private final String filePath;

    private transient Listener listener;
    private transient ErrorListener errorListener;

    public Task(String urlStr, String filePath) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        ranges = new ArrayList<>();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public List<Pair<Long, Long>> getRanges() {
        return ranges;
    }

    public String getFilePath() {
        return filePath;
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
