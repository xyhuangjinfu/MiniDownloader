package cn.hjf.downloader;

import android.support.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class HttpDirector implements Callable<Void> {

    private static final int TASK_LENGTH = 1024 * 1024 * 10;

    private ExecutorService directorExecutor;
    private ExecutorService workerExecutor;

    private volatile boolean quit;

    private Task task;
    private HttpResource resource;
    private Progress progress;

    private List<HttpWorker> workerList;

    public HttpDirector(
            Task task,
            ExecutorService workerExecutor) {
        this.task = task;
        resource = (HttpResource) task.getResource();
        progress = task.getProgress();
        workerList = new ArrayList<>();
    }

    @Override
    public Void call() throws Exception {
        /* Get resource info. */
        if (resource == null) {
            resource = getResource();
            if (resource == null) {
                task.getErrorListener().onServerError("unknown server resource");
            }
        }
        /* Create dest file parent dirs. */
        if (!FileUtil.createParentDirs(task.getFilePath())) {
            task.getErrorListener().onLocalError("cannot create parent dir");
            return null;
        }
        /* Split tasks. */
        List<Range> rangeList = splitTask(resource);

        /* Create workers. */
        createWorkers(rangeList);

        return null;
    }

    public void quit() {
        quit = true;
    }

    /**
     * **************************************************************************************************
     * **************************************************************************************************
     */

    @Nullable
    private HttpResource getResource() throws Exception {
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }

        String lenStr = connection.getHeaderField("Content-Length");
        if (lenStr == null || "".equals(lenStr)) {
            return null;
        }
        long contentLength = Long.valueOf(lenStr);

        String acceptRanges = connection.getHeaderField("Accept-Ranges");

        String eTag = connection.getHeaderField("ETag");
        String lastModified = connection.getHeaderField("Last-Modified");

        if (eTag == null || "".equals(eTag)
                || lastModified == null || "".equals(lastModified)) {
            return null;
        }

        return new HttpResource(contentLength, acceptRanges, eTag, lastModified);
    }

    private List<Range> splitTask(HttpResource resourceInfo) {
        /* Http 1.1 only support "bytes" range unit */
        if ("bytes".equals(resourceInfo.getAcceptRanges())) {
            return splitByRanges(resourceInfo);
        } else {
            return splitWhole(resourceInfo);
        }
    }

    private List<Range> splitWhole(HttpResource resourceInfo) {
        List<Range> ranges = new ArrayList<>();
        ranges.add(new Range(0l, resourceInfo.getContentLength() - 1));
        return ranges;
    }

    private List<Range> splitByRanges(HttpResource resourceInfo) {
        List<Range> ranges = new ArrayList<>();
        for (long i = 0; i < resourceInfo.getContentLength(); i = i + TASK_LENGTH) {
            long start = i;
            long end = (resourceInfo.getContentLength() - 1 - start < TASK_LENGTH)
                    ?
                    resourceInfo.getContentLength() - 1
                    :
                    start + TASK_LENGTH - 1;
            ranges.add(new Range(start, end));
        }

        return ranges;
    }

    private void createWorkers(List<Range> rangeList) {
        workerList.clear();

        for (int i = 0; i < rangeList.size(); i++) {
            workerList.add(new HttpWorker(task, rangeList.get(i)));
        }
    }
}
