/*
 * Copyright 2017 huangjinfu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hjf.downloader;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.RunnableFuture;

/**
 * Worker are used to execute download task for different protocols. Concrete subclass should handle concrete protocol.
 *
 * @author huangjinfu
 */

abstract class Worker implements CustomFutureCallable<Task>, ProgressUpdater.Target {

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
     * Provide progress update service.
     */
    private ProgressUpdater progressUpdater;
    /**
     * Download count since last progress update.
     */
    private volatile long lastUpdatedDownloadedCount = 0;
    /**
     * Time of last progress update.
     */
    private volatile long lastUpdatedTime;

    public Worker(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task,
            @NonNull ProgressUpdater progressUpdater) {
        checkParams(context, taskManager, task, progressUpdater);
        this.context = context;
        this.taskManager = taskManager;
        this.task = task;
        this.progressUpdater = progressUpdater;
    }

    private void checkParams(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task,
            @NonNull ProgressUpdater progressUpdater) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        if (taskManager == null) {
            throw new IllegalArgumentException("TaskManager must not be null");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task must not be null");
        }
        if (progressUpdater == null) {
            throw new IllegalArgumentException("ProgressUpdater must not be null");
        }
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
            /** Start progress and network speed updater. */
            startProgressUpdater();
            /** Download */
            readAndWrite(getInputStream());

            /** Calculate whether finished or stopped. */
            if (taskFinished()) {
                handleFinish();
            } else {
                handleStop();
            }

        } catch (Exception e) {
            e.printStackTrace();
            handleError(e);
        } finally {
            /** Stop progress and network speed updater. */
            stopProgressUpdater();
            /** Close network connection. */
            closeNetworkConnect();
        }

        return task;
    }

    @Override
    public RunnableFuture<Task> newTaskFor() {
        return new MDFutureTask<>(this, this);
    }

    /**
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

    /**
     * Handle start of this work.
     */
    private void handleStart() {
        /** Mark task status to stopped. */
        taskManager.handleRunning(task);
    }

    /**
     * Handle stop of this work.
     */
    private void handleStop() {
        /** Mark task status to stopped. */
        taskManager.handleStopped(task);
    }

    /**
     * Handle finish of this work.
     */
    private void handleFinish() {
        /** Mark task status to finished. */
        taskManager.handleFinished(task);
    }

    /**
     * Handle error of this work.
     */
    private void handleError(Exception error) {
        /** Mark task status to finished. */
        taskManager.handleError(task, error);
    }

    /**
     * Initial network connect for concrete protocol.
     *
     * @throws Exception network exception occurs.
     */
    protected abstract void initNetworkConnect() throws Exception;

    /**
     * Continue start a stopped task, set partial download parameters for concrete protocol.
     *
     * @throws Exception set progress parameter fail.
     */
    protected abstract void setProgressIfNecessary() throws Exception;

    /**
     * Get download InputStream of concrete protocol.
     *
     * @return
     * @throws Exception Some error occurred.
     */
    protected abstract InputStream getInputStream() throws Exception;

    /**
     * Close network connection of concrete protocol, ignore exceptions.
     */
    protected abstract void closeNetworkConnect();

    /**
     * Start fixed rate progress update service.
     */
    private void stopProgressUpdater() {
        progressUpdater.stopUpdateService(this);
    }

    /**
     * Stop fixed rate progress update service.
     */
    private void startProgressUpdater() {
        progressUpdater.startUpdateService(this);
    }

    /**
     * Read from network and write to disk.
     *
     * @return Total download count for this task.
     */
    @Nullable
    private void readAndWrite(InputStream networkInputStream) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;

        try {
            /** Init I/O stream */
            bis = new BufferedInputStream(networkInputStream);
            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");

            /** Seek to last progress. */
            Progress progress = task.getProgress();
            if (progress != null) {
                downloadCount = progress.getDownloaded();
                lastUpdatedDownloadedCount = downloadCount;
                randomAccessFile.seek(progress.getDownloaded());
            }
            /** Init lastUpdatedTime. */
            lastUpdatedTime = SystemClock.uptimeMillis();

            /** Read from network and write to disk. */
            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;

                /** Save real time downloaded progress */
                task.getProgress().setDownloaded(downloadCount);

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
    }

    /**
     * Calculate this task whether finished.
     *
     * @return true if task already finished.
     */
    private boolean taskFinished() {
        return task.getProgress().finished();
    }

    /**
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

    @Override
    public void updateProgress() {
        Progress progress = task.getProgress();
        /** Snapshot of progress status. */
        long time = SystemClock.uptimeMillis();
        long downloaded = progress.getDownloaded();
        /** Calculate network speed. */
        double speed = (downloaded - lastUpdatedDownloadedCount) / (time - lastUpdatedTime) / 1.024;
        progress.setNetworkSpeed(NumberUtil.scale(speed, 2));
        /** Update marker. */
        lastUpdatedTime = time;
        lastUpdatedDownloadedCount = downloaded;
        /** Update progress info. */
        taskManager.handleProgress(task);
    }

    /**
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

    /**
     * Indicate this task is really be executed by executor, Only used for cancel operation now.
     *
     * @return true if this task is really be executed by executor.
     */
    boolean isExecuted() {
        return executed;
    }

    /**
     * Set quit flag.
     *
     * @param quit if true, indicate that this task should stop download.
     */
    void setQuit(boolean quit) {
        this.quit = quit;
    }

    @NonNull
    Task getTask() {
        return task;
    }

    /**
     * ********************************************************************************************************************************************
     * ********************************************************************************************************************************************
     */

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Worker)) {
            return false;
        }

        Worker w = (Worker) obj;
        return w.task.equals(this.task);
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }
}
