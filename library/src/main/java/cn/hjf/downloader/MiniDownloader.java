package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class MiniDownloader {
    /* Application context. */
    private Context appContext;

    /* Task queue to hold tasks.*/
    private final BlockingQueue<Task> taskQueue;

    /* Dispatch task to download. */
    private TaskDispatcher taskDispatcher;

    /* Executor to run dispatch task */
    private final ExecutorService taskDispatchExecutor;

    /* Executor to run direct task. */
    private final ExecutorService directorExecutor;

    /* Executor to run download task. */
    private final ExecutorService workerExecutor;

    private static class InstanceHolder {
        static MiniDownloader instance = new MiniDownloader();
    }

    public static MiniDownloader getInstance() {
        return InstanceHolder.instance;
    }

    private MiniDownloader() {
        taskQueue = new PriorityBlockingQueue<>();
        taskDispatchExecutor = Executors.newFixedThreadPool(1);
        directorExecutor = Executors.newFixedThreadPool(2);
        workerExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void init(@NonNull Context context) {
        appContext = context.getApplicationContext();

        TaskManager.getInstance().init(appContext);

        taskDispatcher = new TaskDispatcher(taskQueue, directorExecutor, workerExecutor);
        taskDispatchExecutor.submit(taskDispatcher);
    }

    public void quit() {

    }

    public void download(@NonNull Task task) {
        taskQueue.add(task);
    }

    public void pause(@NonNull Task task) {

    }

    public void delete(@NonNull Task task) {

    }

    public List<Task> getPausedTask() {
        return null;
    }

}
