package cn.hjf.downloader;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String urlStr;
    private final String filePath;
    private Listener listener;
    private ErrorListener errorListener;

    /**
     * Used to continue download
     */
    private Resource resource;
    private Progress progress;

    public Task(String urlStr, String filePath, Listener listener, ErrorListener errorListener) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.listener = listener;
        this.errorListener = errorListener;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public Resource getResource() {
        return resource;
    }

    void setResource(Resource resource) {
        this.resource = resource;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
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
}
