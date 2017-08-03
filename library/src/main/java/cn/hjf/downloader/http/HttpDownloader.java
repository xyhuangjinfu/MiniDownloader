package cn.hjf.downloader.http;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.hjf.downloader.Downloader;
import cn.hjf.downloader.ErrorListener;
import cn.hjf.downloader.Listener;
import cn.hjf.downloader.Task;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public class HttpDownloader implements Downloader {

    private static final int DIRECTOR_THREAD_POOL_SIZE = 2;

    private Context appContext;
    private ExecutorService directorExecutor;

    public HttpDownloader() {
        directorExecutor = Executors.newFixedThreadPool(DIRECTOR_THREAD_POOL_SIZE);
    }

    @Override
    public void download(
            @NonNull Context context,
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener) {
        if (context == null
                || urlStr == null
                || filePath == null
                || listener == null
                || errorListener == null) {
            throw new IllegalArgumentException("Some parameters must not be null, please check again!");
        }
        appContext = context.getApplicationContext();
        HttpDirector director = new HttpDirector(appContext, createNewTask(urlStr, filePath, listener, errorListener));
        directorExecutor.submit(director);
    }

    private Task createNewTask(String urlStr, String filePath, Listener listener, ErrorListener errorListener) {
        Task task = new Task();
        task.setStatus(Task.Status.NEW);
        task.setUrlStr(urlStr);
        task.setFilePath(filePath);
        task.setListener(listener);
        task.setErrorListener(errorListener);
        return task;
    }
}
