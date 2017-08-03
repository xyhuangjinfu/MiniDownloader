package cn.hjf.downloader;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public class HttpDownloadTask implements Callable<Boolean> {

    private String urlStr;
    private String filePath;
    private long start;
    private long end;
    private byte[] buffer = new byte[1024];

    public HttpDownloadTask(String urlStr, String filePath, long start, long end) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.start = start;
        this.end = end;
    }

    @Override
    public Boolean call() throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        if (connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
            return false;
        }

        return readAndWrite(connection);
    }

    private boolean readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(filePath, "rw");
            randomAccessFile.seek(start);

            int count;
            while ((count = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, count);
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
