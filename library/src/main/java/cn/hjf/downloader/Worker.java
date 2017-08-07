package cn.hjf.downloader;

import android.support.annotation.NonNull;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public abstract class Worker {

    @NonNull
    protected Task task;

    public Worker(@NonNull Task task) {
        this.task = task;
    }

    @NonNull
    public Task getTask() {
        return task;
    }
}
