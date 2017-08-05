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

    /* Task manager to manage task status. */
    private TaskManager taskManager;

    /* Task queue to hold tasks.*/
    private BlockingQueue<Task> taskQueue;

    /* Dispatch task to download. */
    private TaskDispatcher taskDispatcher;

    /* Executor to run dispatch task */
    private ExecutorService taskDispatchExecutor;

    public void init(@NonNull Context context) {
        appContext = context.getApplicationContext();

        taskManager.init(appContext);

        taskQueue = new PriorityBlockingQueue<>();
        taskDispatcher = new TaskDispatcher(taskQueue);
        taskDispatchExecutor = Executors.newFixedThreadPool(1);
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
