package cn.hjf.downloader.http;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by huangjinfu on 2017/8/2.
 */

class HttpWorker implements Callable<Pair<Long, Long>> {

    private HttpTask httpTask;
    private Pair<Long, Long> range;

    private HttpDirector.ProgressUpdateListener progressUpdateListener;

    private byte[] buffer = new byte[1024 * 1024];

    public HttpWorker(
            @NonNull HttpTask httpTask,
            @NonNull Pair<Long, Long> range,
            @NonNull HttpDirector.ProgressUpdateListener progressUpdateListener) {
        if (httpTask == null || range == null || progressUpdateListener == null) {
            throw new IllegalArgumentException("Some parameters must not be null, please check again!");
        }
        this.httpTask = httpTask;
        this.range = range;
        this.progressUpdateListener = progressUpdateListener;
    }

    @Override
    public Pair<Long, Long> call() throws Exception {
        URL url = new URL(httpTask.getTask().getUrlStr());
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

            randomAccessFile = new RandomAccessFile(httpTask.getTask().getFilePath(), "rw");
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
