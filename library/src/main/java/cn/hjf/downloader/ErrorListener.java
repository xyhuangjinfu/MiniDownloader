package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public interface ErrorListener {
    void onInvalidUrl(String urlStr);

    void onLocalError(String msg);

    void onServerError(String msg);
}
