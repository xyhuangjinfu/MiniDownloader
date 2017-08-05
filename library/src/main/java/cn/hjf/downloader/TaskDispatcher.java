package cn.hjf.downloader;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class TaskDispatcher implements Callable<Void> {

    private BlockingQueue<Task> taskQueue;
    private volatile boolean quit;

    public TaskDispatcher(BlockingQueue<Task> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public Void call() throws Exception {

        while (!quit) {
            Task task = taskQueue.take();
            dispatchByProtocol(task);

            /* Block util Director return. */
        }

        return null;
    }

    public void quit() {
        quit = true;
    }

    private void dispatchByProtocol(@NonNull Task task) {
        try {
            URL url = new URL(task.getUrlStr());
            if (url.getProtocol().toUpperCase().startsWith("HTTP")) {
                //TODO http download
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            task.getErrorListener().onInvalidUrl(task.getUrlStr());
        }
    }

}
