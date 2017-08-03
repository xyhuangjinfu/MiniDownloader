package cn.hjf.downloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public class HttpDownloader {

    private static final int DOWNLOAD_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int GET_LENGTH_THREAD_POOL_SIZE = 2;

    private ExecutorService downloadExecutor;
    private ExecutorService getLengthExecutor;

    public HttpDownloader() {
        downloadExecutor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_POOL_SIZE);
        getLengthExecutor = Executors.newFixedThreadPool(GET_LENGTH_THREAD_POOL_SIZE);
    }

    public void download(String urlStr, String filePath, DownloadListener listener) throws Exception {
        HttpDispatchTask dispatchTask = new HttpDispatchTask(urlStr, filePath, DOWNLOAD_THREAD_POOL_SIZE, downloadExecutor, listener);
        getLengthExecutor.submit(dispatchTask);
    }


}
