package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.InputStream;

/**
 * Created by huangjinfu on 2017/8/7.
 */

class FtpWorker extends Worker {

    private MiniFtp miniFtp;

    public FtpWorker(@NonNull Context context, @NonNull TaskManager taskManager, @NonNull Task task) {
        super(context, taskManager, task);
    }

    @Override
    protected void initNetworkConnect() throws Exception {
        miniFtp = new MiniFtp(task.getUrlStr());
        miniFtp.connect();
    }

    @Override
    protected void setProgressIfNecessary() throws Exception {
        if (task.getProgress() == null) {
            Progress progress = new Progress(miniFtp.size());
            /** Set progress for task. */
            task.setProgress(progress);
        }
    }

    @Override
    protected InputStream getInputStream() throws Exception {
        if (task.getProgress().getDownloaded() != 0) {
            miniFtp.rest(task.getProgress().getDownloaded());
        }
        return miniFtp.getInputStream();
    }

    @Override
    protected void closeNetworkConnect() {
        if (miniFtp != null) {
            try {
                miniFtp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}