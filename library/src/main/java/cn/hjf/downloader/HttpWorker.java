package cn.hjf.downloader;

import android.os.Process;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class HttpWorker implements Callable<Range> {

    private Task task;
    private Range range;

    private WorkListener workListener;
    private ProgressListener progressListener;

    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];

    public HttpWorker(
            Task task,
            Range range,
            WorkListener workListener,
            ProgressListener progressListener) {
        this.task = task;
        this.range = range;
    }

    @Override
    public Range call() throws Exception {
        /* Downgrade download thread priority. */
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        /* Connect server. */
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        /* Add header. */
        addHeader(connection);
        /* Check response. */
        /* Remote resource modified.*/
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
            workListener.onResourceModified(task);
            return null;
        }
        /* Response 206, download. */
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
            readAndWrite(connection);
        }
        return null;
    }

    private void addHeader(HttpURLConnection connection) {
        HttpResource r = (HttpResource) task.getResource();
        if (r.geteTag() != null && !"".equals(r.geteTag())) {
            connection.setRequestProperty("If-Match", r.geteTag());
        }
        if (r.getLastModified() != null && !"".equals(r.getLastModified())) {
            connection.setRequestProperty("If-Unmodified-Since", r.geteTag());
        }
        connection.setRequestProperty("Range", "bytes=" + range.getStart() + "-" + range.getEnd());
    }

    private void readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            randomAccessFile.seek(range.getStart());

            final long total = range.getEnd() - range.getStart() + 1;
            int downloadCount = 0;
            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;
                progressListener.onUpdate(total, downloadCount);

                /* Time to quit. */
                if (quit) {
                    workListener.onDownload(task, range, new Range(range.getStart(), range.getStart() + downloadCount - 1));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            /* Download nothing. */
            workListener.onDownload(task, range, Range.INVALID_RANGE);
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
    }

    public void quit() {
        quit = true;
    }
}
