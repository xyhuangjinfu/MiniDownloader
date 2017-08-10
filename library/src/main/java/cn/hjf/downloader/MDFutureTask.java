/*
 * Copyright 2017 huangjinfu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hjf.downloader;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author huangjinfu
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
