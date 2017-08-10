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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by huangjinfu on 2017/8/7.
 */

@ThreadSafe
final class TaskManager {

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
     * Mark the task's status to waiting, and hold the future to control task.
     *
     * @param task
     */
    public void handleWaiting(Task task, Future<Task> future) {
        synchronized (lock) {
            /** Mark task status to WAITING. */
            task.setStatus(Task.Status.WAITING);
            /** Notify status changed. */
            eventNotifier.notifyWait(task);
            /** Register task. */
            taskSet.add(task);
            /** Register future of this task. */
            runningTaskFutureMap.put(task, future);
        }
    }

    /**
     * Mark the task's status to running.
     *
     * @param task
     */
    public void handleRunning(Task task) {
        synchronized (lock) {
            /** Mark task status to RUNNING. */
            task.setStatus(Task.Status.RUNNING);
            /** Notify status changed. */
            eventNotifier.notifyStart(task);
        }
    }

    /**
     * Mark the task's status to running.
     *
     * @param task
     */
    public void handleProgress(Task task, Progress progress) {
        synchronized (lock) {
            /** Notify status changed. */
            eventNotifier.notifyProgress(task, progress);
        }
    }

    /**
     * Mark the task's status to stopped.
     *
     * @param task
     */
    public void handleStopped(Task task) {
        synchronized (lock) {
            /** Mark task's status to STOPPED. */
            task.setStatus(Task.Status.STOPPED);
            /** Notify status changed. */
            eventNotifier.notifyStop(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);
        }
    }

    /**
     * Mark the task's status to finished
     *
     * @param task
     */
    public void handleFinished(Task task) {
        synchronized (lock) {
            /** Mark task's status to FINISHED. */
            task.setStatus(Task.Status.FINISHED);
            /** Notify status changed. */
            eventNotifier.notifyFinish(task);
            /** Remove task. */
            taskSet.remove(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);
        }
    }

    /**
     * Mark the task's status to finished
     *
     * @param task
     */
    public void handleDeleted(Task task) {
        synchronized (lock) {
            /** Refresh task status. */
            task.setStatus(Task.Status.NEW);
            task.setProgress(null);
            task.setResource(null);
            /** Notify task deleted. */
            eventNotifier.notifyDelete(task);
            /** Remove task. */
            taskSet.remove(task);
            /** Remove future of this task. */
            runningTaskFutureMap.remove(task);
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
    public List<Task> getStoppedTask() {
        synchronized (lock) {
            List<Task> stoppedTask = new ArrayList<>();
            for (Task task : taskSet) {
                if (task.getStatus() == Task.Status.STOPPED) {
                    stoppedTask.add(task);
                }
            }
            return stoppedTask;
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
        }
    }
}
