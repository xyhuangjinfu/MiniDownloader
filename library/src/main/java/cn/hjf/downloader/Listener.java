package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public interface Listener {

    void onStart();

    void onProgress(long total, long progress);

    void onFinish();
}
