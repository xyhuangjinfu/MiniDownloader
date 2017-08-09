package cn.hjf.downloader;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by huangjinfu on 2017/8/9.
 */

final class MDFutureTask<T> extends FutureTask<T> implements Comparable<MDFutureTask> {

    private Worker worker;

    public MDFutureTask(Callable<T> callable, Worker worker) {
        super(callable);
        this.worker = worker;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (worker.isExecuted()) {
            worker.setQuit(true);
            return true;
        }
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public int compareTo(MDFutureTask o) {
        return worker.getTask().compareTo(o.worker.getTask());
    }
}
