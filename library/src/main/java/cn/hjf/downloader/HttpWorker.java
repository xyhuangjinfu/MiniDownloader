package cn.hjf.downloader;

import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class HttpWorker extends Worker implements CustomFutureCallable<Range> {

    @Nullable
    private HttpResource httpResource;
    @Nullable
    private Progress lastProgress;
    @Nullable
    private Range taskRange;
    private Range downloadedRange;

    private volatile boolean executed;
    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];
    private long contentLength;

    public HttpWorker(@NonNull Task task) {
        super(task);

        this.httpResource = (HttpResource) task.getResource();

        this.lastProgress = task.getProgress();
        if (lastProgress != null) {
            Progress downloaded = task.getProgress();
            taskRange = new Range(downloaded.getDownloadRange().getEnd() + 1, downloaded.getTotal() - 1);
        }
    }

    @Override
    public Range call() throws Exception {
        executed = true;
        /* Downgrade download thread priority. */
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);


        /* Connect server. */
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        /* Add header. */
        addHeader(connection);

        /* Notify start. */
        task.getListener().onStart(task);

        /* Check response. */
        /* Notify remote resource modified.*/
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
            task.getErrorListener().onResourceModified(task);
            return null;
        }

        /* Response 206 or 200, download. */
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL
                || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            /* Get content Length. */
            String lenStr = connection.getHeaderField("Content-Length");
            if (lenStr == null || "".equals(lenStr)) {
                task.getErrorListener().onError(task, new IllegalStateException("Unknown Content-Length!"));
                return null;
            }
            contentLength = Long.valueOf(lenStr);

            /* Download */
            downloadedRange = readAndWrite(connection);

            /* Notify finish. */
            if (downloadedRange != null && downloadedRange.getEnd() == contentLength - 1) {
                task.getListener().onFinish(task);
            } else {
                /* Notify stop. */
                task.getListener().onStop(task);
                /* Save stopped task. */
                saveStoppedTask();
            }

            return downloadedRange;
        }

        /* Notify error. */
        task.getErrorListener().onError(task, new IOException("Server Error!"));

        return null;
    }

    @Override
    public RunnableFuture<Range> newTaskFor() {
        return new FutureTask<Range>(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (executed) {
                    quit = true;
                    return true;
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    private void addHeader(HttpURLConnection connection) {
        if (httpResource != null) {
            if (httpResource.geteTag() != null && !"".equals(httpResource.geteTag())) {
                connection.setRequestProperty("If-Match", httpResource.geteTag());
            }
            if (httpResource.getLastModified() != null && !"".equals(httpResource.getLastModified())) {
                connection.setRequestProperty("If-Unmodified-Since", httpResource.geteTag());
            }
        }
        if (taskRange != null) {
            connection.setRequestProperty("Range", "bytes=" + taskRange.getStart() + "-" + taskRange.getEnd());
        }
    }

    @Nullable
    private Range readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            if (taskRange != null) {
                randomAccessFile.seek(taskRange.getStart());
            }

            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;
                task.getListener().onProgress(task, contentLength, downloadCount);

                /* Time to quit. */
                if (quit) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (downloadCount == 0) {
            return null;
        }
        if (taskRange == null) {
            return new Range(0, downloadCount - 1);
        }
        return new Range(0, taskRange.getStart() + downloadCount - 1);
    }

    private void saveStoppedTask() {
        /* Update progress and resource */
        Progress newProgress = new Progress(contentLength, downloadedRange);
        task.setProgress(newProgress);
        task.setResource(httpResource);
    }

}