package cn.hjf.downloader;

import android.support.v4.util.Pair;

import java.util.List;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public class Task {

    public enum Status {
        NEW,
        RUNNING,
        PAUSE
    }

    private String urlStr;
    private String filePath;
    private Listener listener;
    private ErrorListener errorListener;

    private Status status;

    private List<Pair<Long, Long>> ranges;
    private ResourceInfo resourceInfo;

    public String getUrlStr() {
        return urlStr;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Pair<Long, Long>> getRanges() {
        return ranges;
    }

    public void setRanges(List<Pair<Long, Long>> ranges) {
        this.ranges = ranges;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }
}
