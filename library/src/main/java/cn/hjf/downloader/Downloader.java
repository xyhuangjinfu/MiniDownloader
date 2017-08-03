package cn.hjf.downloader;

import android.support.annotation.NonNull;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public interface Downloader {

    void download(
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener);
}
