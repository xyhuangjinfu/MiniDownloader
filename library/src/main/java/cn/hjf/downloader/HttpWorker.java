package cn.hjf.downloader;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

public class HttpWorker extends Worker implements CustomFutureCallable<Long> {

    @Nullable
    private HttpResource httpResource;
    @Nullable
    private Progress progress;

    private volatile boolean executed;
    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];

    public HttpWorker(@NonNull Context context, @NonNull Task task) {
        super(context, task);
        this.httpResource = (HttpResource) task.getResource();
        this.progress = task.getProgress();
    }

    @Override
    public Long call() throws Exception {
        /** Mark this task be executed. */
        executed = true;

        /** Downgrade download thread priority. */
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        /** Connect server. */
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        /** Add header. */
        addHeader(connection);

        /** Notify start. */
        task.setStatus(Task.Status.RUNNING);
        task.getListener().onStart(task);

        /** Remote resource modified.*/
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
            handlePreconditionFailed();
            return null;
        }

        /** Response 206 or 200, download. */
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL
                || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            /** Handle null progress. */
            if (progress == null) {
                try {
                    handleNullProgress(connection);
                } catch (Exception e) {
                    task.getErrorListener().onError(task, e);
                    return null;
                }
            }

            /** Download */
            readAndWrite(connection);

            /** Notify finish. */
            if (progress.finished()) {
                handleFinish();
            } else {
                handleStop();
            }
            return progress.getDownloaded();
        }

        /** Notify error. */
        task.getErrorListener().onError(task, new IOException("Server Error!"));

        return null;
    }

    @Override
    public RunnableFuture<Long> newTaskFor() {
        return new FutureTask<Long>(this) {
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

    private void handlePreconditionFailed() {
        /** Clear progress and resource info. */
        task.setResource(null);
        task.setProgress(null);
        /** Delete task if exist on disk. */
        FileUtil.deleteTask(context, task);
        /** Delete last download data if exist.*/
        FileUtil.deleteFile(task.getFilePath());
        /** Notify remote resource modified.*/
        task.getErrorListener().onResourceModified(task);
    }

    private void handleStop() {
        /** Notify stop. */
        task.setStatus(Task.Status.STOPPED);
        task.getListener().onStop(task);
        /** Save stopped task. */
        saveStoppedTask();
    }

    private void handleFinish() {
        /** Delete task from disk if exist.*/
        FileUtil.deleteTask(context, task);
        /** Notify finish. */
        task.setStatus(Task.Status.FINISH);
        task.getListener().onFinish(task);
    }

    private void handleNullProgress(HttpURLConnection connection) throws Exception {
        /** Get content Length. */
        String lenStr = connection.getHeaderField("Content-Length");
        if (lenStr == null || "".equals(lenStr)) {
            throw new IllegalStateException("Unknown Content-Length!");
        }
        progress = new Progress(Long.valueOf(lenStr));
        /** Set progress for task. */
        task.setProgress(progress);
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
        if (progress != null) {
            connection.setRequestProperty("Range", "bytes=" + (progress.getDownloaded()) + "-" + (progress.getTotal() - 1));
        }
    }

    @Nullable
    private Long readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;
        long lastNotifiedCount = 0;
        try {
            bis = new BufferedInputStream(connection.getInputStream(), 1024 * 1024);

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            if (progress != null) {
                downloadCount = progress.getDownloaded();
                lastNotifiedCount = downloadCount;
                randomAccessFile.seek(progress.getDownloaded());
            }

            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;

                /** Notify and update progress */
                progress.setDownloaded(downloadCount);
                if (needNotify(progress.getTotal(), lastNotifiedCount, downloadCount)) {
                    task.getListener().onProgress(task, progress);
                    lastNotifiedCount = downloadCount;
                }

                /** Time to quit. */
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

        return downloadCount;
    }

    private void saveStoppedTask() {
        /** Update progress and resource */
        task.setResource(httpResource);
        /** Save to disk. */
        FileUtil.saveTask(context, task);
    }

    private boolean needNotify(long total, long lastNotifiedCount, long downloadCount) {
        return (downloadCount - lastNotifiedCount) >= total / 100;
    }

}