package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public interface Downloader {

    Task download(
            @NonNull Context context,
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener);

    void pause(@NonNull Task task);
}
