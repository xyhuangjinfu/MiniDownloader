package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/5.
 */

interface ProgressListener {
    void onUpdate(long download);
}
