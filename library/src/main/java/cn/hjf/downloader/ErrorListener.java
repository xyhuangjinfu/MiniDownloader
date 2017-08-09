package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public interface ErrorListener {
    void onError(Task task, Exception error);
}
