package cn.hjf.downloader;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
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

class FtpWorker extends Worker implements CustomFutureCallable<Task> {

    private static final String TAG = Debug.appLogPrefix + "FtpWorker";

    @Nullable
    private HttpResource httpResource;
    @Nullable
    private Progress progress;

    private volatile boolean executed;
    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];
    private long startTime;

    public FtpWorker(@NonNull Context context, @NonNull TaskManager taskManager, @NonNull Task task) {
        super(context, taskManager, task);
        this.httpResource = (HttpResource) task.getResource();
        this.progress = task.getProgress();
    }

    @Override
    public Task call() throws Exception {
        /** Mark this task be executed. Used by cancel task. */
        executed = true;

        /** Start, change status and notify event. */
        handleStart();

        HttpURLConnection connection = null;

        try {
            /** Downgrade download thread priority. */
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            /** Create dest file parent dirs. */
            if (!FileUtil.createParentDirs(task.getFilePath())) {
                /** File system error. */
                handleError(new IOException("cannot create parent dir"));
                return task;
            }

            /** Create http connection. */
            URL url = new URL(task.getUrlStr());
            connection = (HttpURLConnection) url.openConnection();

            /** Add header. */
            addHeader(connection);

            /** Response 206 or 200, download. */
            if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL
                    || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                /** Handle null progress. */
                if (progress == null) {
                    handleNullProgress(connection);
                }

                /** Download */
                readAndWrite(connection);

                /** Calculate whether finished or stopped. */
                if (progress.finished()) {
                    handleFinish();
                } else {
                    handleStop();
                }
                return task;
            }

            /** Server error.*/
            handleError(new IOException("Server Error!"));

        } catch (Exception e) {
            e.printStackTrace();
            /** Some other error.*/
            handleError(e);
        } finally {
            try {
                /** Close connection */
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return task;
    }

    @Override
    public RunnableFuture<Task> newTaskFor() {
        return new FutureTask<Task>(this) {
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

    /**
     * Handle start of this work.
     */
    private void handleStart() {
        /** Record start time. */
        startTime = SystemClock.elapsedRealtime();
        /** Mark task status to stopped. */
        taskManager.markRunning(task);
        /** Notify stop. */
        task.getListener().onStart(task);
    }

    /**
     * Handle stop of this work.
     */
    private void handleStop() {
        /** Mark task status to stopped. */
        taskManager.markStopped(task);
        /** Notify stop. */
        task.getListener().onStop(task);
        /** Log if necessary */
        if (Debug.debug)
            Log.e(TAG, "Task stopped, used time : " + (SystemClock.elapsedRealtime() - startTime) + " ms, task : " + task);
    }

    /**
     * Handle finish of this work.
     */
    private void handleFinish() {
        /** Mark task status to finished. */
        taskManager.markFinished(task);
        /** Notify finish. */
        task.getListener().onFinish(task);
        /** Log if necessary */
        if (Debug.debug)
            Log.e(TAG, "Task finished, used time : " + (SystemClock.elapsedRealtime() - startTime) + " ms, task : " + task);
    }

    /**
     * Handle error of this work.
     */
    private void handleError(Exception error) {
        /** Clear progress and resource info. */
        task.setResource(null);
        task.setProgress(null);
        /** Delete last download data if exist.*/
        FileUtil.deleteFile(task.getFilePath());
        /** Mark task status to finished. */
        taskManager.markError(task);
        /** Notify finish. */
        task.getErrorListener().onError(task, error);
    }

    /**
     * New task, have no last progress.
     *
     * @param connection
     * @throws Exception
     */
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

    /**
     * Add some header for partial request if necessary.
     *
     * @param connection
     */
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

    /**
     * Read from network and write to disk.
     *
     * @param connection
     * @return Total download count for this task.
     */
    @Nullable
    private Long readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;
        long lastNotifiedCount = 0;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

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

    /**
     * Control the progress update speed, notify on high speed will increase the burden of UI thread.
     *
     * @param total
     * @param lastNotifiedCount
     * @param downloadCount
     * @return
     */
    private boolean needNotify(long total, long lastNotifiedCount, long downloadCount) {
        return (downloadCount - lastNotifiedCount) >= total / 100;
    }

}