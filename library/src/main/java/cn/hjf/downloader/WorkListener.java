package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/5.
 */

interface WorkListener {
    void onResourceModified(Task task);

    void onDownload(Task task, Range expect, Range actual);
}
