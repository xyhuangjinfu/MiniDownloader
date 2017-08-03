package cn.hjf.downloader.http;

import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import cn.hjf.downloader.Task;

/**
 * Created by huangjinfu on 2017/8/2.
 */

class HttpWorker implements Callable<Pair<Long, Long>> {

    private static final String TAG = "MD-HttpWorker";

    private Task task;
    private Pair<Long, Long> range;

    private HttpDirector.ProgressUpdateListener progressUpdateListener;

    private byte[] buffer = new byte[1024 * 1024];

    public HttpWorker(
            @NonNull Task task,
            @NonNull Pair<Long, Long> range,
            @NonNull HttpDirector.ProgressUpdateListener progressUpdateListener) {
        if (task == null || range == null || progressUpdateListener == null) {
            throw new IllegalArgumentException("Some parameters must not be null, please check again!");
        }
        this.task = task;
        this.range = range;
        this.progressUpdateListener = progressUpdateListener;
    }

    @Override
    public Pair<Long, Long> call() throws Exception {
        Log.e(TAG, "HttpWorker run, urlStr : " + task.getUrlStr() + " , range : " + range.toString());

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Range", "bytes=" + range.first + "-" + range.second);
        if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
            return null;
        }
        readAndWrite(connection);
        return range;
    }

    private boolean readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            randomAccessFile.seek(range.first);

            int count;
            while ((count = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, count);
                progressUpdateListener.updateProgress(count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
