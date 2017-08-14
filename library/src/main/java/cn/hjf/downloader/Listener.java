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

import android.support.annotation.MainThread;

/**
 * This interface can notify all events for all tasks. Those methods will be called on main thread.
 *
 * @author huangjinfu
 */

@MainThread
public interface Listener {

    /**
     * Notify that task be submitted to workers, but be waiting in the task queue.
     *
     * @param task
     */
    void onWait(Task task);

    /**
     * Notify that task really be executed by workers.
     *
     * @param task
     */
    void onStart(Task task);

    /**
     * Notify that task progress is updated.
     * Progress information is stored in {@link Progress}, see {@link Progress#getTotal()}, {@link Progress#getDownloaded()}, {@link Progress#getNetworkSpeed()}.
     *
     * @param task
     */
    void onProgressUpdate(Task task);

    /**
     * Notify that task be stopped.
     *
     * @param task
     */
    void onStop(Task task);

    /**
     * Notify that task be finished.
     *
     * @param task
     */
    void onFinish(Task task);

    /**
     * notify that task be deleted.
     *
     * @param task
     */
    void onDelete(Task task);
}
