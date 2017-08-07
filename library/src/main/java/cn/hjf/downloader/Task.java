package cn.hjf.downloader;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Task implements Serializable, Comparable<Task> {

    private static final long serialVersionUID = 1L;

    public enum Status {
        NEW,
        RUNNING,
        STOPPED,
        FINISH
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH
    }

    private final String urlStr;
    private final String filePath;
    private transient Listener listener;
    private transient ErrorListener errorListener;

    /**
     * Internal state of task.
     */
    private volatile Status status = Status.NEW;
    private Resource resource;
    private Progress progress;
    private Priority priority = Priority.NORMAL;

    public Task(String urlStr, String filePath, Listener listener, ErrorListener errorListener) {
        this(urlStr, filePath, listener, errorListener, Priority.NORMAL);
    }

    public Task(String urlStr, String filePath, Listener listener, ErrorListener errorListener, Priority priority) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.listener = listener;
        this.errorListener = errorListener;
        this.priority = priority;
    }

    public String getUrlStr() {
        return urlStr;
    }

    Resource getResource() {
        return resource;
    }

    void setResource(Resource resource) {
        this.resource = resource;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Progress getProgress() {
        return progress;
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

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Task)) {
            return false;
        }

        Task t = (Task) obj;

        return t.urlStr.equals(this.urlStr) && t.filePath.equals(this.filePath);

    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + urlStr.hashCode();
        result = 31 * result + filePath.hashCode();
        return result;
    }

    @Override
    public int compareTo(Task o) {
        return this.priority.ordinal() - o.priority.ordinal();
    }
}
