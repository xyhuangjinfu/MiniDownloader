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
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Task manager need to manage tasks and their future, and handle some other things for task when task status be changed.
 * Task Manager also manage task caches, write unfinished tasks to and read them from disk.
 *
 * @author huangjinfu
 */

@ThreadSafe
final class TaskManager {

    private static final String TAG = Debug.appLogPrefix + "TaskManager";

    /**
     * Application context.
     */
    private Context context;
    /**
     * Task set, when a task transfer to MiniDownloader by {@link MiniDownloader#start(Task)}, it will be in this set,
     * or those unfinished task which be save to disk by {@link #saveAllUnfinishedTasks()}, will be in this set at next start up.
     * FINISHED and ERROR task will be removed from this set.
     */
    private Set<Task> taskSet;
    /**
     * Maps of task to their future, only waiting tasks and running tasks can be in this map.
     */
    private Map<Task, Future<Task>> runningTaskFutureMap;

    /**
     * Avoid from leak of TaskManager's monitor.
     */
    private Object lock = new Object();

    /**
     * Notify events of tasks to main thread.
     */
    private MainThreadEventNotifier eventNotifier;

    /**
     * Init TaskManager.
     *
     * @param context
     */
    public void init(Context context) {
        synchronized (lock) {
            this.context = context;

            taskSet = new HashSet<>();
            runningTaskFutureMap = new HashMap<>();
            eventNotifier = new MainThreadEventNotifier();

            /** Read all unfinished task. */
            taskSet.addAll(FileUtil.readTasksFromDisk(context));
            /** Clear cached task from disk. */
            FileUtil.clearAllTasks(context);
        }
    }

    /**
     * Task status need change to WAITING.
     *
     * @param task
     * @param future the future of this task.
     */
    public void handleWaiting(Task task, Future<Task> future) {
        synchronized (lock) {
            /** Worker thread may already set status to RUNNING, skip it.*/
            if (task.getStatus() != Task.Status.RUNNING) {
                /** Mark task status to WAITING. */
                task.setStatus(Task.Status.WAITING);
                /** Notify status changed. */
                eventNotifier.notifyWait(task);
            }
            /** Register task. */
            taskSet.add(task);
            /** Register future of this task. */
            runningTaskFutureMap.put(task, future);

            if (Debug.debug) {
                Log.d(TAG, "handleWaiting, task:" + task);
            }
        }
    }

    /**
     * Task status need change to RUNNING.
     *
     * @param task
     */
    public void handleRunning(Task task) {
        synchronized (lock) {
            /** Mark task status to RUNNING. */
            task.setStatus(Task.Status.RUNNING);
            /** Notify status changed. */
            eventNotifier.notifyStart(task);

            if (Debug.debug) {
                Log.d(TAG, "handleRunning, task:" + task);
            }
        }
    }

    /**
     * Task need to update progress.
     *
     * @param task
     */
    public void handleProgress(Task task) {
        synchronized (lock) {
            /** Notify status changed. */
            eventNotifier.notifyProgress(task);
        }
    }

    /**
     * Task status need change to STOPPED.
     *
     * @param task
     */
    public void handleStopped(Task task) {
        synchronized (lock) {
            /** Clear temporary network speed. */
            if (task.getProgress() != null) {
                task.getProgress().setNetworkSpeed(0);
            }
            /** Mark task's status to STOPPED. */
            task.setStatus(Task.Status.STOPPED);
            /** Notify status changed. */
            eventNotifier.notifyStop(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);

            if (Debug.debug) {
                Log.d(TAG, "handleStopped, task:" + task);
            }
        }
    }

    /**
     * Task status need change to ERROR.
     *
     * @param task
     */
    public void handleFinished(Task task) {
        synchronized (lock) {
            /** Clear temporary network speed. */
            if (task.getProgress() != null) {
                task.getProgress().setNetworkSpeed(0);
            }
            /** Mark task's status to FINISHED. */
            task.setStatus(Task.Status.FINISHED);
            /** Notify status changed. */
            eventNotifier.notifyFinish(task);
            /** Remove task. */
            taskSet.remove(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);

            if (Debug.debug) {
                Log.d(TAG, "handleFinished, task:" + task);
            }
        }
    }

    /**
     * Task status need change to DELETED, currently it's NEW, but we need do some extra work for this status.
     *
     * @param task
     */
    public void handleDeleted(Task task) {
        synchronized (lock) {
            /** Clear progress and resource info. */
            task.setProgress(null);
            task.setResource(null);
            /** Delete last download data if exist.*/
            FileUtil.deleteFile(task.getFilePath());
            /** Refresh task status. */
            task.setStatus(Task.Status.NEW);
            /** Notify task deleted. */
            eventNotifier.notifyDelete(task);
            /** Remove task. */
            taskSet.remove(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);

            if (Debug.debug) {
                Log.d(TAG, "handleDeleted, task:" + task);
            }
        }
    }

    /**
     * Mark the task's status to error
     *
     * @param task
     */
    public void handleError(Task task, Exception error) {
        synchronized (lock) {
            /** Clear progress and resource info. */
            task.setResource(null);
            task.setProgress(null);
            /** Delete last download data if exist.*/
            FileUtil.deleteFile(task.getFilePath());
            /** Mark task's status to FINISHED. */
            task.setStatus(Task.Status.ERROR);
            /** Notify status changed. */
            eventNotifier.notifyError(task, error);
            /** Remove task. */
            taskSet.remove(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);

            if (Debug.debug) {
                Log.d(TAG, "handleError, task:" + task + ", error:" + error);
            }
        }
    }

    /**
     * Get future by task.
     *
     * @param task
     * @return
     */
    public Future<Task> getFuture(Task task) {
        synchronized (lock) {
            return runningTaskFutureMap.get(task);
        }
    }

    /**
     * Get all futures for workers.
     *
     * @return
     */
    public Collection<Future<Task>> getAllFutures() {
        synchronized (lock) {
            return new ArrayList<>(runningTaskFutureMap.values());
        }
    }

    /**
     * Get all stopped tasks, commonly we called this method on start up to get those tasks which stopped in last run, so that we can restart them.
     *
     * @return
     */
    public List<Task> getAllStoppedTasks() {
        synchronized (lock) {
            List<Task> stoppedTasks = new ArrayList<>();
            for (Task task : taskSet) {
                if (task.getStatus() == Task.Status.STOPPED) {
                    stoppedTasks.add(task);
                }
            }

            if (Debug.debug) {
                Log.d(TAG, "getAllStoppedTasks : " + stoppedTasks);
            }

            return stoppedTasks;
        }
    }

    /**
     * Save all unfinished tasks to disk, on next start up, we can get those tasks and restart them.
     */
    public void saveAllUnfinishedTasks() {
        synchronized (lock) {
            List<Task> unfinishedTask = new ArrayList<>();
            for (Task task : taskSet) {
                if (task.getStatus() != Task.Status.FINISHED) {
                    unfinishedTask.add(task);
                }
            }
            FileUtil.saveTaskList(context, unfinishedTask);

            if (Debug.debug) {
                Log.d(TAG, "saveAllUnfinishedTasks : " + unfinishedTask);
            }
        }
    }
}
