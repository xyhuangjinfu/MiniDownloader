package cn.hjf.downloader;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class HttpDownloader {

    private BlockingQueue<Task> taskQueue;

    public HttpDownloader() {
        taskQueue = new LinkedBlockingQueue<>();
    }
}
