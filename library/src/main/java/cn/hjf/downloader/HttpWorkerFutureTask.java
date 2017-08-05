package cn.hjf.downloader;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class HttpWorkerFutureTask extends FutureTask {
    public HttpWorkerFutureTask(Callable callable) {
        super(callable);
    }

    public HttpWorkerFutureTask(Runnable runnable, Object result) {
        super(runnable, result);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return super.cancel(mayInterruptIfRunning);
    }
}
