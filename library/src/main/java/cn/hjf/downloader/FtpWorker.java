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

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.InputStream;

/**
 * @author huangjinfu
 */

class FtpWorker extends Worker {

    private MiniFtp miniFtp;

    public FtpWorker(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task,
            @NonNull ProgressUpdater progressUpdater) {
        super(context, taskManager, task, progressUpdater);
    }

    @Override
    protected void initNetworkConnect() throws Exception {
        if (task.getTaskUrl() != null) {
            miniFtp = new MiniFtp((FtpTaskUrl) task.getTaskUrl());
        } else {
            miniFtp = new MiniFtp(task.getUrlStr());
        }
        miniFtp.connect();
    }

    @Override
    protected void setProgressIfNecessary() throws Exception {
        if (task.getProgress() == null) {
            Progress progress = new Progress(miniFtp.fileSize());
            /** Set progress for task. */
            task.setProgress(progress);
        }
    }

    @Override
    protected InputStream getInputStream() throws Exception {
        if (task.getProgress().getDownloaded() != 0) {
            miniFtp.setProgress(task.getProgress().getDownloaded());
        }
        return miniFtp.getInputStream();
    }

    @Override
    protected void closeNetworkConnect() {
        if (miniFtp != null) {
            try {
                miniFtp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}