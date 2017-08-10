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
     * Init TaskManager.
     *
     * @param context
     */
    public synchronized void init(Context context) {
        this.context = context;

        taskSet = new HashSet<>();
        runningTaskFutureMap = new HashMap<>();

        /** Read all unfinished task. */
        taskSet.addAll(FileUtil.readTasksFromDisk(context));
        /** Clear cached task from disk. */
        FileUtil.clearAllTasks(context);
    }

    /**
     * Mark the task's status to waiting, and hold the future to control task.
     *
     * @param task
     */
    public synchronized void markWaiting(Task task, Future<Task> future) {
        /** Mark task status to RUNNING. */
        task.setStatus(Task.Status.WAITING);
        /** Register task. */
        taskSet.add(task);
        /** Register running task. */
        runningTaskFutureMap.put(task, future);
    }

    /**
     * Mark the task's status to running.
     *
     * @param task
     */
    public synchronized void markRunning(Task task) {
        /** Mark task status to RUNNING. */
        task.setStatus(Task.Status.RUNNING);
    }

    /**
     * Mark the task's status to stopped.
     *
     * @param task
     */
    public synchronized void markStopped(Task task) {
        /** Mark task's status to STOPPED. */
        task.setStatus(Task.Status.STOPPED);
        /** Remove future of this task. */
        runningTaskFutureMap.remove(task);
    }

    /**
     * Mark the task's status to finished
     *
     * @param task
     */
    public synchronized void markFinished(Task task) {
        /** Mark task's status to FINISHED. */
        task.setStatus(Task.Status.FINISHED);
        /** Remove task. */
        taskSet.remove(task);
        /** Remove future of this task. */
        runningTaskFutureMap.remove(task);
    }

    /**
     * Mark the task's status to error
     *
     * @param task
     */
    public synchronized void markError(Task task) {
        /** Mark task's status to FINISHED. */
        task.setStatus(Task.Status.ERROR);
        /** Remove task. */
        taskSet.remove(task);
        /** Remove future of this task. */
        runningTaskFutureMap.remove(task);
    }

    /**
     * Get future by task.
     *
     * @param task
     * @return
     */
    public synchronized Future<Task> getFuture(Task task) {
        return runningTaskFutureMap.get(task);
    }

    /**
     * Get all futures for workers.
     *
     * @return
     */
    public synchronized Collection<Future<Task>> getAllFutures() {
        return new ArrayList<>(runningTaskFutureMap.values());
    }

    /**
     * Get all stopped tasks, commonly we called this method on start up to get those tasks which stopped in last run, so that we can restart them.
     *
     * @return
     */
    public synchronized List<Task> getStoppedTask() {
        List<Task> stoppedTask = new ArrayList<>();
        for (Task task : taskSet) {
            if (task.getStatus() == Task.Status.STOPPED) {
                stoppedTask.add(task);
            }
        }
        return stoppedTask;
    }

    /**
     * Save all unfinished tasks to disk, on next start up, we can get those tasks and restart them.
     */
    public synchronized void saveAllUnfinishedTasks() {
        List<Task> unfinishedTask = new ArrayList<>();
        for (Task task : taskSet) {
            if (task.getStatus() != Task.Status.FINISHED) {
                unfinishedTask.add(task);
            }
        }
        FileUtil.saveTaskList(context, unfinishedTask);
    }
}
