package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public final class MiniDownloader {

    private Context context;
    private ExecutorService workExecutor;
    private CompletionService<Task> workCompletionService;

    private ExecutorService waitFinishExecutor;

    private Map<Task, Future<Task>> workerFutureList;

    private TaskManager taskManager;

    private volatile boolean quit;

    private static class InstanceHolder {
        static MiniDownloader instance = new MiniDownloader();
    }

    public static MiniDownloader getInstance() {
        return InstanceHolder.instance;
    }

    private MiniDownloader() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();

        this.workExecutor = new ThreadPoolExecutor(6, 6, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>()) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                if (callable instanceof CustomFutureCallable) {
                    return ((CustomFutureCallable) callable).newTaskFor();
                }
                return super.newTaskFor(callable);
            }
        };
        this.workCompletionService = new ExecutorCompletionService(workExecutor);

        this.waitFinishExecutor = Executors.newSingleThreadExecutor();
        waitFinishExecutor.submit(waitFinishTask);

        workerFutureList = new HashMap<>();

        taskManager = new TaskManager();
        taskManager.init(context);
    }

    public void quit() {
        for (Future<Task> f : workerFutureList.values()) {
            f.cancel(false);
        }
        workExecutor.shutdown();

        quit = true;
        waitFinishExecutor.shutdown();
    }

    public void start(@NonNull Task task) {
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task ,urlStr, filePath, listener, errorListener must not be null!");
        }

        if (task.getUrlStr().toUpperCase().startsWith("HTTP")) {
            HttpWorker httpWorker = new HttpWorker(context, taskManager, task);
            workerFutureList.put(task, workCompletionService.submit(httpWorker));
        }
    }

    public void stop(@NonNull Task task) {
        if (!checkTask(task)) {
            throw new IllegalArgumentException("task ,urlStr, filePath, listener, errorListener must not be null!");
        }

        Future<Task> future = workerFutureList.remove(task);

        if (future != null) {
            future.cancel(false);
        }

    }

    public List<Task> getStoppedTaskList() {
        return taskManager.getStoppedTask();
    }

    public void setDebuggable(boolean debuggable) {
        Debug.debug = debuggable;
    }

    private Callable<Void> waitFinishTask = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            while (!quit) {
                try {
                    Future<Task> f = workCompletionService.take();
                    workerFutureList.remove(f.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    };

    private boolean checkTask(@NonNull Task task) {
        if (task == null
                || task.getUrlStr() == null
                || task.getFilePath() == null
                || task.getListener() == null
                || task.getErrorListener() == null) {
            return false;
        }
        return true;
    }
}
