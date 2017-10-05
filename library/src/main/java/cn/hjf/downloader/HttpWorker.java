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
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author huangjinfu
 */

class HttpWorker extends Worker {

    @Nullable
    private HttpResource httpResource;
    private HttpURLConnection httpURLConnection;

    public HttpWorker(
            @NonNull Context context,
            @NonNull TaskManager taskManager,
            @NonNull Task task,
            @NonNull ProgressUpdater progressUpdater) {
        super(context, taskManager, task, progressUpdater);
        this.httpResource = (HttpResource) task.getResource();
    }

    @Override
    protected void initNetworkConnect() throws Exception {
        /** Create http connection. */
        URL url = new URL(task.getUrlStr());
        httpURLConnection = (HttpURLConnection) url.openConnection();

        /** Add header. */
        addHeader();

        /** Response 206 or 200, download. */
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_PARTIAL
                && responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server Error! responseCode : " + responseCode + ", url : " + url);
        }
    }

    @Override
    protected void setProgressIfNecessary() throws Exception {
        if (task.getProgress() == null) {
            /** Get content Length. */
            String lenStr = httpURLConnection.getHeaderField("Content-Length");
            if (lenStr == null || "".equals(lenStr)) {
                throw new IllegalStateException("Unknown Content-Length!");
            }
            Progress progress = new Progress(Long.valueOf(lenStr));
            /** Set progress for task. */
            task.setProgress(progress);
        }
    }

    @Override
    protected InputStream getInputStream() throws Exception {
        return httpURLConnection.getInputStream();
    }

    @Override
    protected void closeNetworkConnect() {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    /**
     * Add some header for partial request if necessary.
     */
    private void addHeader() {
        if (httpResource != null) {
            if (httpResource.geteTag() != null && !"".equals(httpResource.geteTag())) {
                httpURLConnection.setRequestProperty("If-Match", httpResource.geteTag());
            }
            if (httpResource.getLastModified() != null && !"".equals(httpResource.getLastModified())) {
                httpURLConnection.setRequestProperty("If-Unmodified-Since", httpResource.geteTag());
            }
        }
        Progress progress = task.getProgress();
        if (progress != null) {
            httpURLConnection.setRequestProperty("Range", "bytes=" + (progress.getDownloaded()) + "-" + (progress.getTotal() - 1));
        }
    }

}