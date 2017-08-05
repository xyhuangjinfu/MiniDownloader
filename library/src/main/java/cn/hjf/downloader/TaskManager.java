package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class TaskManager {

    private Context appContext;

    private Set<Task> taskSet;

    private static class InstanceHolder {
        static TaskManager instance = new TaskManager();
    }

    public static TaskManager getInstance() {
        return InstanceHolder.instance;
    }

    private TaskManager() {
    }

    public void init(Context appContext) {
        this.appContext = appContext;

        taskSet = new HashSet<>();

        /* Read from disk. */
        taskSet.addAll(FileUtil.readTasksFromDisk(appContext));
    }

    public void createTask(@NonNull Task task) {
        task.setStatus(Task.Status.NEW);
        taskSet.add(task);
    }

    public void runTask(@NonNull Task task) {
        if (taskSet.contains(task)) {
            task.setStatus(Task.Status.RUNNING);
        }
    }

    public void pauseTask(@NonNull Task task) {
        if (taskSet.contains(task)) {
            task.setStatus(Task.Status.PAUSED);
        }
    }

    public void finishTask(@NonNull Task task) {
        if (taskSet.contains(task)) {
            task.setStatus(Task.Status.FINISH);
        }
    }

    public List<Task> getPausedTask() {
        List<Task> pausedTasks = new ArrayList<>();

        for (Task task : taskSet) {
            if (task.getStatus() == Task.Status.PAUSED) {
                pausedTasks.add(task);
            }
        }

        return pausedTasks;
    }
}
