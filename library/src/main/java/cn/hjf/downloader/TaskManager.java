package cn.hjf.downloader;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangjinfu on 2017/8/4.
 */

public class TaskManager {

    private Context context;

    private Set<Task> newTasks;
    private Set<Task> runningTasks;
    private Set<Task> pausedTasks;
    private Set<Task> finishedTasks;

    public void init(Context context) {
        this.context = context;

        newTasks = new HashSet<>();
        runningTasks = new HashSet<>();
        pausedTasks = new HashSet<>();
        finishedTasks = new HashSet<>();
    }

    private void readTasksFromDisk() {

    }
}
