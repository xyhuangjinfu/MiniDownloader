package cn.hjf.downloader;

import android.content.Context;

/**
 * Created by huangjinfu on 2017/8/4.
 */

public final class MiniDownloader {

    private static class InstanceHolder {
        static MiniDownloader instance = new MiniDownloader();
    }

    public static MiniDownloader getInstance() {
        return InstanceHolder.instance;
    }

    private Context context;
    private TaskManager taskManager;

    public void init(Context context) {
        /* Init task manager. */
        //TODO
    }
}
