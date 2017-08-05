package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class MiniDownloader {
    /* Application context. */
    private Context appContext;

    /* Task queue to hold tasks.*/
//    private final BlockingQueue<Task> taskQueue;

    /* Dispatch task to download. */
//    private TaskDispatcher taskDispatcher;

    /* Executor to run dispatch task */
//    private final ExecutorService taskDispatchExecutor;

    /* Executor to run direct task. */
    private final ExecutorService directorExecutor;

    /* Executor to run download task. */
    private final ExecutorService workerExecutor;

    private final HttpDownloader httpDownloader;

    private static class InstanceHolder {
        static MiniDownloader instance = new MiniDownloader();
    }

    public static MiniDownloader getInstance() {
        return InstanceHolder.instance;
    }

    private MiniDownloader() {
//        taskQueue = new PriorityBlockingQueue<>();
//        taskDispatchExecutor = Executors.newFixedThreadPool(1);
        directorExecutor = Executors.newFixedThreadPool(2);
        workerExecutor = new CustomFutureTaskThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        httpDownloader = new HttpDownloader(directorExecutor, workerExecutor);
    }

    public void init(@NonNull Context context) {
        appContext = context.getApplicationContext();

        TaskManager.getInstance().init(appContext);

//        taskDispatcher = new TaskDispatcher(taskQueue, directorExecutor, workerExecutor);
//        taskDispatchExecutor.submit(taskDispatcher);
    }

    public void quit() {
        workerExecutor.shutdownNow();
        directorExecutor.shutdownNow();
    }

    public void download(@NonNull Task task) {
        if (isHttpTask(task)) {
            httpDownloader.download(task);
        }
    }

    public void pause(@NonNull Task task) {
        if (isHttpTask(task)) {
            httpDownloader.pause(task);
        }
    }

    public void delete(@NonNull Task task) {
        if (isHttpTask(task)) {
            httpDownloader.delete(task);
        }
    }

    public List<Task> getPausedTask() {
        return TaskManager.getInstance().getPausedTask();
    }

    private boolean isHttpTask(Task task) {
        return task.getUrlStr().toUpperCase().startsWith("HTTP");
    }

}
