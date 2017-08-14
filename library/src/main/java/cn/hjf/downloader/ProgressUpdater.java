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

import android.os.Handler;
import android.os.HandlerThread;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide fixed rate to update progress.
 *
 * @author huangjinfu
 */

@ThreadSafe
class ProgressUpdater extends HandlerThread {

    /**
     * Update per second.
     */
    private static final int UPDATE_INTERVAL = 1000;

    /**
     * Handler which attach to update message queue.
     */
    private Handler updateHandler;

    /**
     * Evey {@link Target} have single {@link UpdateRunnable} instance.
     */
    private Map<Target, Runnable> targetRunnableMap;

    /**
     * If this field is true, it means that this updater is quited, and no more target can be submitted.
     */
    private boolean quit;

    /**
     * A target which will update it's progress.
     */
    public interface Target {
        /**
         * Time to update progress.
         */
        void updateProgress();
    }

    public ProgressUpdater() {
        super("MD-ProgressUpdater");

        targetRunnableMap = new HashMap<>();
    }

    /**
     * Start progress update service of a specific {@link Target}.
     *
     * @param target
     */
    public synchronized void startUpdateService(Target target) {
        if (!quit) {
            createHandlerIfNecessary();

            UpdateRunnable updateRunnable = new UpdateRunnable(target);

            targetRunnableMap.put(target, updateRunnable);

            updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
        }
    }

    /**
     * Stop progress update service of a specific target.
     *
     * @param target
     * @throws IllegalStateException No started service of this target be found.
     */
    public synchronized void stopUpdateService(Target target) {
        Runnable runnableOfTarget = targetRunnableMap.get(target);
        if (runnableOfTarget == null) {
            throw new IllegalStateException("No started service of this target be found, target:" + target);
        }
        updateHandler.removeCallbacks(runnableOfTarget);
    }

    /**
     * Close this ProgressUpdater, also close all update service of targets.
     */
    public synchronized void close() {
        quit = true;
        for (Runnable r : targetRunnableMap.values()) {
            updateHandler.removeCallbacks(r);
        }
        quit();
    }

    /**
     * Create update handler.
     */
    private void createHandlerIfNecessary() {
        if (updateHandler == null) {
            updateHandler = new Handler(getLooper());
        }
    }

    /**
     * Update Runnable of specific target.
     */
    private class UpdateRunnable implements Runnable {
        private Target target;

        public UpdateRunnable(Target target) {
            this.target = target;
        }

        @Override
        public void run() {
            target.updateProgress();
            updateHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    }

}
