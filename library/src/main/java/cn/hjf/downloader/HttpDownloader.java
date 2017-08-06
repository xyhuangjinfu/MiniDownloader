package cn.hjf.downloader;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class HttpDownloader {

    private ExecutorService directorExecutor;
    private ExecutorService workerExecutor;

    private List<HttpDirector> directorList;
    private List<Future> futureList;

    public HttpDownloader(ExecutorService directorExecutor, ExecutorService workerExecutor) {
        this.directorExecutor = directorExecutor;
        this.workerExecutor = workerExecutor;

        directorList = new ArrayList<>();
        futureList = new ArrayList<>();
    }

    public void start(@NonNull Task task) {
        HttpDirector httpDirector = new HttpDirector(task, workerExecutor);
        futureList.add(directorExecutor.submit(httpDirector));
    }

    public void stop(@NonNull Task task) {
        HttpDirector director = null;
        for (int i = 0; i < directorList.size(); i++) {
            director = directorList.get(i);
            if (director.getTask().equals(task)) {
                director.quit();

                Future future = futureList.get(i);
                future.cancel(false);
                futureList.remove(i);

                break;
            }
        }
        directorList.remove(director);
    }
}
