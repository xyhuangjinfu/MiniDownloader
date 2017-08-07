package cn.hjf.downloader;

import android.content.Context;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class HttpWorker extends Worker implements CustomFutureCallable<Range> {

    @Nullable
    private HttpResource httpResource;
    @Nullable
    private Progress progress;

//    private Range taskRange;
//    private Range downloadedRange;

    private volatile boolean executed;
    private volatile boolean quit;
    private byte[] buffer = new byte[1024 * 1024];

    public HttpWorker(@NonNull Context context, @NonNull Task task) {
        super(context, task);

        this.httpResource = (HttpResource) task.getResource();

        this.progress = task.getProgress();
//        if (progress != null) {
//            Progress downloaded = task.getProgress();
//            taskRange = new Range(downloaded.getDownloadRange().getEnd() + 1, downloaded.getTotal() - 1);
//        }
    }

    @Override
    public Range call() throws Exception {
        /** Mark this task be executed. */
        executed = true;

        /** Downgrade download thread priority. */
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        /** Connect server. */
        URL url = new URL(task.getUrlStr());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        /** Add header. */
        addHeader(connection);

        /** Notify start. */
        task.getListener().onStart(task);

        /** Remote resource modified.*/
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
            handlePreconditionFailed();
            return null;
        }

        /** Response 206 or 200, download. */
        if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL
                || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            /** Handle null progress. */
            if (progress == null) {
                try {
                    handleNullProgress(connection);
                } catch (Exception e) {
                    task.getErrorListener().onError(task, e);
                    return null;
                }
            }

            /** Download */
            Range downloadedRange = readAndWrite(connection);

            if (downloadedRange != null) {
                /** Notify finish. */
                System.out.println(downloadedRange.getEnd() + "  <>  " + (progress.getTotal() - 1));
                if (downloadedRange.getEnd() == progress.getTotal() - 1) {
                    handleFinish();
                } else {
                    handleStop(downloadedRange);
                }
                return downloadedRange;
            }
        }

        /** Notify error. */
        task.getErrorListener().onError(task, new IOException("Server Error!"));

        return null;
    }

    @Override
    public RunnableFuture<Range> newTaskFor() {
        return new FutureTask<Range>(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (executed) {
                    quit = true;
                    return true;
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }

    private void handlePreconditionFailed() {
        /** Clear progress and resource info. */
        task.setResource(null);
        task.setProgress(null);
        /** Delete task if exist on disk. */
        FileUtil.deleteTask(context, task);
        /** Delete last download data if exist.*/
        FileUtil.deleteFile(task.getFilePath());
        /** Notify remote resource modified.*/
        task.getErrorListener().onResourceModified(task);
    }

    private void handleStop(Range downloaded) {
        /** Notify stop. */
        task.getListener().onStop(task);
        /** Save stopped task. */
        saveStoppedTask(downloaded);
    }

    private void handleFinish() {
        /** Delete task from disk if exist.*/
        FileUtil.deleteTask(context, task);
        /** Notify finish. */
        task.getListener().onFinish(task);
    }

    private void handleNullProgress(HttpURLConnection connection) throws Exception {
        progress = new Progress();
        /** Get content Length. */
        String lenStr = connection.getHeaderField("Content-Length");
        if (lenStr == null || "".equals(lenStr)) {
            throw new IllegalStateException("Unknown Content-Length!");
        }
        progress.setTotal(Long.valueOf(lenStr));
    }

    private void addHeader(HttpURLConnection connection) {
        if (httpResource != null) {
            if (httpResource.geteTag() != null && !"".equals(httpResource.geteTag())) {
                connection.setRequestProperty("If-Match", httpResource.geteTag());
            }
            if (httpResource.getLastModified() != null && !"".equals(httpResource.getLastModified())) {
                connection.setRequestProperty("If-Unmodified-Since", httpResource.geteTag());
            }
        }
        if (progress != null && progress.getDownloadRange() != null) {
            connection.setRequestProperty("Range", "bytes=" + (progress.getDownloadRange().getEnd() + 1) + "-" + (progress.getTotal() - 1));
        }
    }

    @Nullable
    private Range readAndWrite(HttpURLConnection connection) {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;
        long downloadCount = 0;
        try {
            bis = new BufferedInputStream(connection.getInputStream());

            randomAccessFile = new RandomAccessFile(task.getFilePath(), "rw");
            if (progress.getDownloadRange() != null) {
                downloadCount = progress.getDownloadRange().getEnd() + 1;
                randomAccessFile.seek(progress.getDownloadRange().getEnd() + 1);
            }

            int readCount;
            while ((readCount = bis.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readCount);
                downloadCount += readCount;
                task.getListener().onProgress(task, progress.getTotal(), downloadCount);

                /** Time to quit. */
                if (quit) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        if (downloadCount == 0) {
            return null;
        }
        return new Range(0, downloadCount - 1);
    }

    private void saveStoppedTask(Range downloaded) {
        /** Update progress and resource */
        Progress newProgress = new Progress(progress.getTotal(), downloaded);
        task.setProgress(newProgress);
        task.setResource(httpResource);
        /** Save to disk. */
        FileUtil.saveTask(context, task);
    }

}