package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public interface Listener {
    void onStart(Task task);

    void onUpdateProgress(Task task, long total, long downloaded);

    void onPause(Task task);

    void onFinish(Task task);
}
