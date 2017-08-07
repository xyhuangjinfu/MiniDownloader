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
    protected Task task;

    public Worker(
            @NonNull Context context,
            @NonNull Task task) {
        this.context = context;
        this.task = task;
    }

    @NonNull
    public Task getTask() {
        return task;
    }
}
