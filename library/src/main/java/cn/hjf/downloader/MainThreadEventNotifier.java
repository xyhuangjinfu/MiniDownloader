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
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * @author huangjinfu
 */

final class MainThreadEventNotifier {

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void notifyWait(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onWait(task);
            }
        });
    }

    public void notifyStart(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onStart(task);
            }
        });
    }

    public void notifyProgress(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onProgressUpdate(task);
            }
        });
    }

    public void notifyStop(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onStop(task);
            }
        });
    }

    public void notifyFinish(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onFinish(task);
            }
        });
    }

    public void notifyDelete(@NonNull final Task task) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getListener().onDelete(task);
            }
        });
    }

    public void notifyError(@NonNull final Task task, @NonNull final Exception error) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                task.getErrorListener().onError(task, error);
            }
        });
    }
}
