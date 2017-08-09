package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by huangjinfu on 2017/8/7.
 */

class HttpWorker extends Worker {

    @Nullable
    private HttpResource httpResource;
    private HttpURLConnection httpURLConnection;

    public HttpWorker(@NonNull Context context, @NonNull TaskManager taskManager, @NonNull Task task) {
        super(context, taskManager, task);
        this.httpResource = (HttpResource) task.getResource();
    }

    @Override
    protected void initNetworkConnect() throws Exception {
        /** Create http connection. */
        URL url = new URL(task.getUrlStr());
        httpURLConnection = (HttpURLConnection) url.openConnection();

        /** Add header. */
        addHeader();

        /** Response 206 or 200, download. */
        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL
                && httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server Error!");
        }
    }

    @Override
    protected void setProgressIfNecessary() throws Exception {
        if (task.getProgress() == null) {
            /** Get content Length. */
            String lenStr = httpURLConnection.getHeaderField("Content-Length");
            if (lenStr == null || "".equals(lenStr)) {
                throw new IllegalStateException("Unknown Content-Length!");
            }
            Progress progress = new Progress(Long.valueOf(lenStr));
            /** Set progress for task. */
            task.setProgress(progress);
        }
    }

    @Override
    protected InputStream getInputStream() throws Exception {
        return httpURLConnection.getInputStream();
    }

    @Override
    protected void closeNetworkConnect() {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    /**
     * Add some header for partial request if necessary.
     */
    private void addHeader() {
        if (httpResource != null) {
            if (httpResource.geteTag() != null && !"".equals(httpResource.geteTag())) {
                httpURLConnection.setRequestProperty("If-Match", httpResource.geteTag());
            }
            if (httpResource.getLastModified() != null && !"".equals(httpResource.getLastModified())) {
                httpURLConnection.setRequestProperty("If-Unmodified-Since", httpResource.geteTag());
            }
        }
        Progress progress = task.getProgress();
        if (progress != null) {
            httpURLConnection.setRequestProperty("Range", "bytes=" + (progress.getDownloaded()) + "-" + (progress.getTotal() - 1));
        }
    }

}