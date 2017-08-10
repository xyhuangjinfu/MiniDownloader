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
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjinfu
 */

public final class MiniDownloader {

    /**
     * Application context
     */
    private Context appContext;
    /**
     * Executor to execute Workers.
     */
    private ExecutorService workExecutor;
    /**
     * The map that Task to future of this task.
     */
    private Map<Task, Future<Task>> taskFutureMap;
    /**
     * TaskManager to manage all task status.
     */
    private TaskManager taskManager;
    /**
     * Executor to execute command, prohibit to block UI thread.
     */
    private ExecutorService commandExecutor;

    private static class InstanceHolder {
        static MiniDownloader instance = new MiniDownloader();
    }

    public static MiniDownloader getInstance() {
        return InstanceHolder.instance;
    }

    private MiniDownloader() {
    }

    /**
     * Initial MiniDownloader.
     *
     * @param context
     */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
        /** Create work executor. */
        this.workExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                if (callable instanceof CustomFutureCallable) {
                    return ((CustomFutureCallable) callable).newTaskFor();
                }
                return super.newTaskFor(callable);
            }
        };
        /** Create command executor. */
        this.commandExecutor = Executors.newSingleThreadExecutor();
        /** Create and initial task manager. */
        taskManager = new TaskManager();
        taskManager.init(context);
    }

    /**
     * Quit downloader. All unfinished tasks will be stored to disk, you can get them on next start up by call {@link #getStoppedTaskList()}.
     */
    public void quit() {
        commandExecutor.submit(new Runnable() {
            @Override
            public void run() {
                /** Cancel all workers. */
                for (Future<Task> f : taskManager.getAllFutures()) {
                    f.cancel(false);
                }
                /** Close worker executor. */
                workExecutor.shutdown();
                /** Wait for worker executor close. */
                try {
                    workExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /** Save all unfinished tasks to disk. */
                taskManager.saveAllUnfinishedTasks();
            }
        });
    }

    /**
     * Start download of this task, before be passed in, the status of this task can only be {@link cn.hjf.downloader.Task.Status#NEW} or {@link cn.hjf.downloader.Task.Status#STOPPED}.
     *
     * @param task
     */
    public void start(@NonNull final Task task) {
        /** Check task and fields. */
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task ,urlStr, filePath, listener, errorListener must not be null!");
        }
        /** Check task status. */
        if (task.getStatus() != Task.Status.NEW
                && task.getStatus() != Task.Status.STOPPED
                && task.getStatus() != Task.Status.ERROR) {
            throw new IllegalStateException("Task status not NEW or STOPPED or ERROR!");
        }

        final Worker worker;
        if (task.getUrlStr().toUpperCase().startsWith("HTTP")) {
            worker = new HttpWorker(appContext, taskManager, task);
        } else if (task.getUrlStr().toUpperCase().startsWith("FTP")) {
            worker = new FtpWorker(appContext, taskManager, task);
        } else {
            throw new IllegalArgumentException("Unsupported protocol.");
        }

        commandExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    taskManager.handleWaiting(task, workExecutor.submit(worker));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Stop a task, before be passed in, the status of this task can only be {@link cn.hjf.downloader.Task.Status#WAITING} or {@link cn.hjf.downloader.Task.Status#RUNNING}.
     *
     * @param task
     */
    public void stop(@NonNull final Task task) {
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task ,urlStr, filePath, listener, errorListener must not be null!");
        }

        if (task.getStatus() != Task.Status.WAITING && task.getStatus() != Task.Status.RUNNING) {
            throw new IllegalStateException("Task status not WAITING or RUNNING!");
        }

        commandExecutor.submit(new Runnable() {
            @Override
            public void run() {
                /** Cancel the worker of this  task. */
                Future<Task> future = taskManager.getFuture(task);
                if (future != null) {
                    future.cancel(false);
                }
            }
        });
    }

    /**
     * Delete a task from MiniDownloader, if you want to stop a task and never continue it forever, you can call this method.
     * FINISHED task will be remove from MiniDownloader automatically.
     * Delete a task will delete the file which already downloaded. And status of this task will be set to {@link cn.hjf.downloader.Task.Status#NEW},
     * it seems like that this task is new created and never passed into MiniDownloader.
     *
     * @param task
     */
    public void delete(@NonNull final Task task) {
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task ,urlStr, filePath, listener, errorListener must not be null!");
        }
        commandExecutor.submit(new Runnable() {
            @Override
            public void run() {
                /** Cancel the worker of this  task. */
                Future<Task> future = taskManager.getFuture(task);
                if (future != null) {
                    future.cancel(false);
                    /** Block until worker return, and then delete downloaded data. */
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /** Not need wait to cancel. Try to delete downloaded data directly. */
                FileUtil.deleteFile(task.getFilePath());

                taskManager.handleDeleted(task);

            }
        });
    }

    /**
     * Get all stopped tasks, commonly we called this method on start up to get those tasks which stopped in last run, so that we can restart them.
     *
     * @return
     */
    public List<Task> getStoppedTaskList() {
        return taskManager.getStoppedTask();
    }

    /**
     * Set debuggable flag.
     *
     * @param debuggable
     */
    public void setDebuggable(boolean debuggable) {
        Debug.debug = debuggable;
    }

    /**
     * Check task whether valid.
     *
     * @param task
     * @return
     */
    private boolean checkTask(@NonNull Task task) {
        if (task == null
                || task.getUrlStr() == null
                || task.getFilePath() == null
                || task.getListener() == null
                || task.getErrorListener() == null) {
            return false;
        }
        return true;
    }
}
