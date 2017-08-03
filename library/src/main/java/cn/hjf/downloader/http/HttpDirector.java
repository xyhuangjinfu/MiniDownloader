package cn.hjf.downloader.http;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import cn.hjf.downloader.Task;
import cn.hjf.downloader.exception.FileSystemException;
import cn.hjf.downloader.util.FileUtil;

/**
 * Created by huangjinfu on 2017/8/2.
 */

class HttpDirector implements Callable<Void> {

    private static final String TAG = "MD-HttpDirector";

    private static final int WORKER_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int TASK_LENGTH = 1024 * 1024 * 8;

    private Context appContext;

    private Task task;
    private HttpResource httpResource;

    private List<HttpWorker> workerList = new ArrayList<>();
    private List<ProgressUpdateListener> progressUpdateListenerList = new ArrayList<>();

    private AtomicLong downloadCount = new AtomicLong(0);

    private static ExecutorService workerExecutor = Executors.newFixedThreadPool(WORKER_THREAD_POOL_SIZE);
    private CompletionService completionService;

    public HttpDirector(Context appContext, Task task) {
        this.appContext = appContext;
        this.task = task;
        completionService = new ExecutorCompletionService(workerExecutor);
    }

    @Override
    public Void call() throws Exception {
        long start = SystemClock.uptimeMillis();

        /* Get resource info. */
        HttpResource resource = getResourceInfo();
        if (resource == null) {
            throw new Exception("resourceInfo == null");
        }
        httpResource = resource;

        /* Create dest file parent dirs. */
        if (!FileUtil.createParentDirs(task.getFilePath())) {
            task.getErrorListener().onError(new FileSystemException("cannot create parent dir"));
            return null;
        }

        /* Split tasks. */
        task.setRanges(splitTask(httpResource));

        /* Create workers. */
        createWorkers();

        /* Notify start download. */
        task.getListener().onStart();

        /* Start up download workers. */
        for (int i = 0; i < workerList.size(); i++) {
            completionService.submit(workerList.get(i));
        }

        /* Block wait to download finish. */
        for (int i = 0; i < workerList.size(); i++) {
            Future<Pair<Long, Long>> f = completionService.take();
            Pair<Long, Long> range = f.get();
            task.getRanges().remove(range);
        }

        /* Notify finish download. */
        if (task.getRanges().isEmpty()) {
            task.getListener().onFinish();
        }

        Log.e(TAG, "Download finish, use " + (SystemClock.uptimeMillis() - start) + " ms, urlStr : " + task.getUrlStr());

        return null;
    }

    @Nullable
    private HttpResource getResourceInfo() throws Exception {
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }

        String lenStr = connection.getHeaderField("Content-Length");
        long contentLength = (lenStr == null || "".equals(lenStr)) ? -1 : Long.valueOf(lenStr);
        String acceptRanges = connection.getHeaderField("Accept-Ranges");
        String eTag = connection.getHeaderField("ETag");
        String lastModified = connection.getHeaderField("Last-Modified");

        return new HttpResource(contentLength, acceptRanges, eTag, lastModified);
    }

    private List<Pair<Long, Long>> splitTask(HttpResource resourceInfo) {
        /* Http 1.1 only support "bytes" range unit */
        if ("bytes".equals(resourceInfo.getAcceptRanges())) {
            return splitByRanges(resourceInfo);
        } else {
            return splitWhole(resourceInfo);
        }
    }

    private List<Pair<Long, Long>> splitWhole(HttpResource resourceInfo) {
        List<Pair<Long, Long>> ranges = new ArrayList<>();
        ranges.add(Pair.create(0l, resourceInfo.getContentLength() - 1));
        return ranges;
    }

    private List<Pair<Long, Long>> splitByRanges(HttpResource resourceInfo) {
        List<Pair<Long, Long>> ranges = new ArrayList<>();
        for (long i = 0; i < resourceInfo.getContentLength(); i = i + TASK_LENGTH) {
            long start = i;
            long end = (resourceInfo.getContentLength() - 1 - start < TASK_LENGTH)
                    ?
                    resourceInfo.getContentLength() - 1
                    :
                    start + TASK_LENGTH - 1;
            ranges.add(Pair.create(start, end));
        }

        return ranges;
    }

    private void createWorkers() {
        workerList.clear();
        progressUpdateListenerList.clear();

        for (int i = 0; i < task.getRanges().size(); i++) {
            ProgressUpdateListener listener = getProgressListener(i);
            progressUpdateListenerList.add(listener);
            workerList.add(new HttpWorker(task, task.getRanges().get(i), listener));
        }

        Log.e(TAG, "createWorkers(), urlStr : " + task.getUrlStr() + " , content length : " + httpResource.getContentLength() + " , worker count : " + workerList.size());
    }

    private ProgressUpdateListener getProgressListener(final int position) {
        return new ProgressUpdateListener() {
            @Override
            public void updateProgress(long count) {
                task.getListener().onProgress(httpResource.getContentLength(), downloadCount.addAndGet(count));
            }
        };
    }

    /**
     * ********************************************************************************************************
     * ********************************************************************************************************
     */

    interface ProgressUpdateListener {
        void updateProgress(long downloadCount);
    }
}
