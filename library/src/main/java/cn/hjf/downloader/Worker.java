package cn.hjf.downloader;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.RunnableFuture;

/**
 * Created by huangjinfu on 2017/8/7.
 */

abstract class Worker implements CustomFutureCallable<Task> {

    private static final String TAG = Debug.appLogPrefix + "Worker";

    /**
     * Application context.
     */
    @NonNull
    protected Context context;
    /**
     * Global task manager.
     */
    @NonNull
    protected TaskManager taskManager;
    /**
     * Task to be downloaded
     */
    @NonNull
    protected Task task;
    /**
     * Mark this worker be executed, not in the task queue of thread pool.
     */
    private volatile boolean executed;
    /**
     * Mark it's time to quit, worker should stop download and return as quickly.
     */
    private volatile boolean quit;
    /**
     * Write buffer.
     */
    private byte[] buffer = new byte[1024 * 1024];
    /**
     * Worker start time, for log use only.
     */
    private long startTime;

    public Worker(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task) {
        this.context = context;
        this.taskManager = taskManager;
        this.task = task;
    }

    @CallSuper
    @Override
    public Task call() throws Exception {
        /** Mark this task be executed. Used by cancel task. */
        executed = true;
        /** Downgrade download thread priority. */
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        try {
            /** Create dest file parent dirs. */
            if (!FileUtil.createParentDirs(task.getFilePath())) {
                /** File system error. */
                throw new IOException("cannot create parent dir");
            }

            /** Start, change status and notify event. */
            handleStart();

            /** Create and initial network connect. */
            initNetworkConnect();
            /** Create new progress for task if it's a new task.*/
            setProgressIfNecessary();
            /** Download */
            readAndWrite(getInputStream());

            /** Calculate whether finished or stopped. */
            if (task.getProgress().finished()) {
                handleFinish();
            } else {
                handleStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleError(e);
        } finally {
            closeNetworkConnect();
        }

        return task;
    }

    @Override
    public RunnableFuture<Task> newTaskFor() {
        return new MDFutureTask<Task>(this, this);
    }

    protected abstract void initNetworkConnect() throws Exception;

    protected abstract void setProgressIfNecessary() throws Exception;

    protected abstract InputStream getInputStream() throws Exception;

    protected abstract void closeNetworkConnect();

    /**
     * Read from network and write to disk.
     *
     * @return Total download count for this task.
     */
    @Nullable
    protected Long readAndWrite(InputStream networkInputStream) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;
        long lastNotifiedCount = 0;
        try {
            bis = new BufferedInputStream(networkInputStream);

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            Progress progress = task.getProgress();
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
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

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

    /**
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

    boolean isExecuted() {
        return executed;
    }

    void setQuit(boolean quit) {
        this.quit = quit;
    }

    @NonNull
    Task getTask() {
        return task;
    }
}
