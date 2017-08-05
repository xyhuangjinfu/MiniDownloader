package cn.hjf.downloader;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public interface NewTaskCallable<T> extends Callable<T> {
    FutureTask<T> newTask();
}
