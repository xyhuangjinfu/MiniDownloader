package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public interface DownloadListener {

    void onStart();

    void onProgress(double progress);

    void onFinish();

    void onError(String error);
}
