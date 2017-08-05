package cn.hjf.downloader;

import android.os.Process;

import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.FutureTask;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class HttpWorker implements NewTaskCallable<Void> {

    private Task task;
    private Range range;
    private Range downloaded = Range.INVALID_RANGE;

    private WorkListener workListener;
    private ProgressListener progressListener;

    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];
    private int downloadCount;

    public HttpWorker(
            Task task,
            Range range,
            WorkListener workListener,
            ProgressListener progressListener) {
        this.task = task;
        this.range = range;
        this.workListener = workListener;
        this.progressListener = progressListener;
    }

    @Override
    public Void call() throws Exception {
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

    @Override
    public FutureTask<Void> newTask() {
        return new FutureTask(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                quit = true;
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    /**
     * ***************************************************************************************************************
     * ***************************************************************************************************************
     */

    Range downloaded() {
        return downloaded;
    }

    /**
     * ***************************************************************************************************************
     * ***************************************************************************************************************
     */

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

            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;
                progressListener.onUpdate(total, downloadCount);

                /* Time to quit. */
                if (quit) {
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /* Update download range. */
            Range download = downloadCount == 0 ? Range.INVALID_RANGE : new Range(range.getStart(), range.getStart() + downloadCount - 1);
            downloaded = download;
            /* Close io. */
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

}
