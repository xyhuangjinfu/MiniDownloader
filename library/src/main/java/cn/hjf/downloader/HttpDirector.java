package cn.hjf.downloader;

import android.support.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class HttpDirector implements NewTaskCallable<Void> {

    private static final int TASK_LENGTH = 1024 * 1024 * 10;

    private ExecutorService workerExecutor;

    private final Task task;

    private List<HttpWorker> workerList;
    private List<Future> futureList;
    private List<WorkListener> workListenerList;
    private List<ProgressListener> progressListenerList;
    private List<Range> downloadedRangeList;
    private AtomicLong downloadCount = new AtomicLong(0);

    public HttpDirector(
            Task task,
            ExecutorService workerExecutor) {
        this.task = task;
        this.workerExecutor = workerExecutor;

        workerList = new ArrayList<>();
        futureList = new ArrayList<>();
        workListenerList = new ArrayList<>();
        progressListenerList = new ArrayList<>();
        downloadedRangeList = new ArrayList<>();
    }

    @Override
    public Void call() throws Exception {

        direct();

        return null;
    }

    public void quit() {
        for (int i = 0; i < futureList.size(); i++) {
            Future f = futureList.get(i);
            f.cancel(false);
        }
    }

    public Task getTask() {
        return task;
    }

    /**
     * **************************************************************************************************
     * **************************************************************************************************
     */

    private void direct() throws Exception {
          /* Get resource info. */
        if (task.getResource() == null) {
            task.setResource(getResource());
            if (task.getResource() == null) {
                task.getErrorListener().onServerError("unknown server resource");
                return;
            }
        }
        /* Create dest file parent dirs. */
        if (!FileUtil.createParentDirs(task.getFilePath())) {
            task.getErrorListener().onLocalError("cannot create parent dir");
            return;
        }
        /* Split tasks. */
        List<Range> rangeList = splitTask();

        /* Create workers. */
        createWorkers(rangeList);

        /* Notify start download. */
        task.getListener().onStart(task);

        /* start download. */
        futureList.addAll(workerExecutor.invokeAll(workerList));
    }

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

    private List<Range> splitTask() {
        HttpResource r = (HttpResource) task.getResource();
        /* Http 1.1 only support "bytes" range unit */
        if ("bytes".equals(r.getAcceptRanges())) {
            return splitByRanges(r);
        } else {
            return splitWhole(r);
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
        workListenerList.clear();
        progressListenerList.clear();

        for (int i = 0; i < rangeList.size(); i++) {
            WorkListener workListener = createWorkerListener();
            workListenerList.add(workListener);

            ProgressListener progressListener = createProgressListener();

            workerList.add(new HttpWorker(task,
                    rangeList.get(i),
                    workListener,
                    progressListener));
        }
    }

    private WorkListener createWorkerListener() {
        return new WorkListener() {
            @Override
            public void onResourceModified(Task task) {
                //TODO quit all workers, delete downloaded data, re-create workers.
                task.setResource(null);

                try {
                    direct();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDownload(Task task, Range expect, Range actual) {
                downloadedRangeList.add(actual);

                if (downloadedRangeList.size() == workerList.size()) {
                    //TODO calculate downloaded
                }
            }
        };
    }

    private ProgressListener createProgressListener() {
        return new ProgressListener() {
            @Override
            public void onUpdate(long total, long download) {
                long totalSize = ((HttpResource) task.getResource()).getContentLength();
                task.getListener().onUpdateProgress(task, totalSize, downloadCount.addAndGet(download));
            }
        };
    }

    @Override
    public FutureTask<Void> newTask() {
        return new FutureTask<Void>(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                for (int i = 0; i < futureList.size(); i++) {
                    Future f = futureList.get(i);
                    f.cancel(false);
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }
}
