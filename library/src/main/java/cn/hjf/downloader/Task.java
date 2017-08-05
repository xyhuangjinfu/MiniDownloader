package cn.hjf.downloader;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        NEW,
        RUNNING,
        PAUSED,
        FINISH
    }

    private String urlStr;
    private String filePath;
    private Status status;

    private transient Listener listener;
    private transient ErrorListener errorListener;

    private Progress progress;
    private Resource resource;

    public Task(
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.listener = listener;
        this.errorListener = errorListener;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public String getFilePath() {
        return filePath;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public Listener getListener() {
        return listener;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    public Progress getProgress() {
        return progress;
    }

    Status getStatus() {
        return status;
    }

    Resource getResource() {
        return resource;
    }

    void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Task)) {
            return false;
        }
        Task t = (Task) obj;

        return this.urlStr.equals(t.urlStr)
                && this.filePath.equals(t.filePath);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + urlStr.hashCode();
        result = 31 * result + filePath.hashCode();
        return result;
    }
}
