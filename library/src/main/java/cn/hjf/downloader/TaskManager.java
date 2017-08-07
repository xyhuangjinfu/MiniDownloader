package cn.hjf.downloader;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by huangjinfu on 2017/8/7.
 */

final class TaskManager {

    private Set<Task> taskSet;

    public void init(Context context) {
        taskSet = new ConcurrentSkipListSet<>();
        /** Read stopped task. */
        taskSet.addAll(FileUtil.readTasksFromDisk(context));
    }

    public void runTask(Task task) {
        task.setStatus(Task.Status.RUNNING);
        taskSet.add(task);
    }

    public void stopTask(Task task) {
        task.setStatus(Task.Status.STOPPED);
    }

    public void finishTask(Task task) {
        task.setStatus(Task.Status.FINISH);
        taskSet.remove(task);
    }

    public List<Task> getStoppedTask() {
        List<Task> stoppedTask = new ArrayList<>();
        for (Task task : taskSet) {
            if (task.getStatus() == Task.Status.STOPPED) {
                stoppedTask.add(task);
            }
        }
        return stoppedTask;
    }
}
