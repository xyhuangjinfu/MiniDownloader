package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public interface ErrorListener {

    void onInvalidUrl(String urlStr);

    void onError(Exception error);
}
