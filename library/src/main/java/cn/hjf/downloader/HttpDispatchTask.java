package cn.hjf.downloader;

import android.os.SystemClock;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public class HttpDispatchTask implements Callable<Boolean> {

    private static final int MAX_TASK_LENGTH = 1024 * 1024;

    private String urlStr;
    private String filePath;
    private int downloadThreadPoolSize;
    private CompletionService completionService;
    private DownloadListener downloadListener;

    public HttpDispatchTask(String urlStr, String filePath, int downloadThreadPoolSize, ExecutorService downloadExecutor, DownloadListener listener) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.downloadThreadPoolSize = downloadThreadPoolSize;
        completionService = new ExecutorCompletionService(downloadExecutor);
        this.downloadListener = listener;
    }

    @Override
    public Boolean call() throws Exception {
        long start = SystemClock.uptimeMillis();

        long contentLength = getContentLength(urlStr);
        if (contentLength == -1) {
            throw new Exception("contentLength == -1");
        }

        if (!FileUtil.createParentDirs(filePath)) {
            downloadListener.onError("cannot create parent dir");
             return false;
        }

        List<HttpDownloadTask> taskList = splitTask(contentLength);

        downloadListener.onStart();

        for (int i = 0; i < taskList.size(); i++) {
            completionService.submit(taskList.get(i));
        }

        for (int i = 0; i < taskList.size(); i++) {
            Future<Boolean> future = completionService.take();
            downloadListener.onProgress((i + 1) * 1.0 / taskList.size());
        }

        downloadListener.onFinish();

        Log.e("O_O", "time : " + (SystemClock.uptimeMillis() - start) + " ms");
        return true;
    }

    private long getContentLength(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("HEAD");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return -1;
        }

        String lenStr = connection.getHeaderField("Content-Length");
        return (lenStr == null || "".equals(lenStr)) ? -1 : Long.valueOf(lenStr);
    }

    private List<HttpDownloadTask> splitTask(long contentLength) {
        long taskLength = 0;
        if (contentLength > downloadThreadPoolSize * MAX_TASK_LENGTH) {
            taskLength = MAX_TASK_LENGTH;
        } else {
            taskLength = contentLength / downloadThreadPoolSize;
        }

        List<HttpDownloadTask> downloadTaskList = new ArrayList<>();
        for (long i = 0; i < contentLength; i = i + taskLength) {
            long start = i;
            long end = (contentLength - 1 - start < taskLength) ? contentLength - 1 : start + taskLength - 1;
            downloadTaskList.add(new HttpDownloadTask(
                    urlStr,
                    filePath,
                    start,
                    end));
        }
        return downloadTaskList;
    }

}
