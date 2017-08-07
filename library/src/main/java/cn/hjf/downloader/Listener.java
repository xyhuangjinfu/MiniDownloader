package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public interface Listener {

    void onStart(Task task);

    void onProgress(Task task, long total, long progress);

    void onStop(Task task);

    void onFinish(Task task);
}
