package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public abstract class Worker {

    @NonNull
    protected Context context;
    @NonNull
    protected TaskManager taskManager;
    @NonNull
    protected Task task;

    public Worker(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task) {
        this.context = context;
        this.taskManager = taskManager;
        this.task = task;
    }
}
