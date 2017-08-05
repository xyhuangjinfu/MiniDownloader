package cn.hjf.downloader;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class HttpDownloader {

    private List<HttpDirector> directorList;
    private ExecutorService directorExecutor;
    private ExecutorService workerExecutor;

    public HttpDownloader() {
        this.directorList = new ArrayList<>();
    }

    public void download(@NonNull Task task) {
        HttpDirector httpDirector = new HttpDirector(task, workerExecutor);
        directorExecutor.submit(httpDirector);
    }
}
