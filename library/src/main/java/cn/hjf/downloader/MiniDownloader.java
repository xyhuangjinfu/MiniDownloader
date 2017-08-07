package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public final class MiniDownloader {

    private Context context;
    private final ExecutorService workExecutor;

    private final List<Worker> workerList;
    private final List<Future> workerFutureList;

    public MiniDownloader(Context context) {
        this.context = context.getApplicationContext();
        this.workExecutor = new ThreadPoolExecutor(6, 6, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                if (callable instanceof CustomFutureCallable) {
                    return ((CustomFutureCallable) callable).newTaskFor();
                }
                return super.newTaskFor(callable);
            }
        };
        workerList = new ArrayList<>();
        workerFutureList = new ArrayList<>();
    }

    public void start(@NonNull Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null!");
        }

        if (task.getUrlStr().toUpperCase().startsWith("HTTP")) {
            HttpWorker httpWorker = new HttpWorker(context, task);
            workerList.add(httpWorker);
            workerFutureList.add(workExecutor.submit(httpWorker));
        }
    }

    public void stop(@NonNull Task task) {
        if (task == null) {
            throw new IllegalArgumentException("task must not be null!");
        }

        int index = -1;
        for (int i = 0; i < workerList.size(); i++) {
            if (task.equals(workerList.get(i).getTask())) {
                index = i;
            }
        }

        if (index != -1) {
            workerFutureList.get(index).cancel(false);
            workerFutureList.remove(index);
            workerList.remove(index);
        }

    }
}
