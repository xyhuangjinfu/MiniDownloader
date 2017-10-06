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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Represent a download task, contains url, file path, priority, status, event listener, etc.
 *
 * @author huangjinfu
 */

public final class Task implements Serializable, Comparable<Task> {

    private static final long serialVersionUID = 1L;

    public enum Status {
        /**
         * New created task.
         */
        NEW,
        /**
         * Waiting in task queue.
         */
        WAITING,
        /**
         * Be executed.
         */
        RUNNING,
        /**
         * Stopped by user.
         */
        STOPPED,
        /**
         * Normal download finish, have no error.
         */
        FINISHED,
        /**
         * Some error occurred in the download progress.
         */
        ERROR
    }

    public enum Priority {
        LOW(1),
        NORMAL(5),
        HIGH(10);

        int value;

        Priority(int value) {
            this.value = value;
        }
    }

    private final TaskUrl taskUrl;
    private final String urlStr;
    private final String filePath;
    private transient Listener listener;
    private transient ErrorListener errorListener;

    /**
     * Internal state of task.
     */
    private volatile Status status = Status.NEW;
    private Resource resource;
    private Progress progress;
    private Priority priority = Priority.NORMAL;

    public Task(
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener) {
        this(null, urlStr, filePath, listener, errorListener, Priority.NORMAL);
    }

    public Task(
            @NonNull String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener,
            @NonNull Priority priority) {
        this(null, urlStr, filePath, listener, errorListener, priority);
    }

    public Task(
            @NonNull TaskUrl taskUrl,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener) {
        this(taskUrl, null, filePath, listener, errorListener, Priority.NORMAL);
    }

    public Task(
            @NonNull TaskUrl taskUrl,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener,
            @NonNull Priority priority) {
        this(taskUrl, null, filePath, listener, errorListener, priority);
    }

    private Task(
            @Nullable TaskUrl taskUrl,
            @Nullable String urlStr,
            @NonNull String filePath,
            @NonNull Listener listener,
            @NonNull ErrorListener errorListener,
            @NonNull Priority priority) {

        checkTask(taskUrl, urlStr, filePath, listener, errorListener, priority);

        this.taskUrl = taskUrl;
        this.urlStr = taskUrl != null ? taskUrl.toUrl() : urlStr;

        this.filePath = filePath;
        this.listener = listener;
        this.errorListener = errorListener;
        this.priority = priority;
    }

    private void checkTask(TaskUrl taskUrl,
                           String urlStr,
                           String filePath,
                           Listener listener,
                           ErrorListener errorListener,
                           Priority priority) {
        if (taskUrl == null
                && (urlStr == null || "".equals(urlStr))) {
            throw new IllegalArgumentException("taskUrl or urlStr, at least one of them is not null.");
        }
        CheckUtil.checkStringNotNullOrEmpty(filePath, "filePath must not be null");
        CheckUtil.checkNotNull(listener, "listener must not be null");
        CheckUtil.checkNotNull(errorListener, "errorListener must not be null");
        CheckUtil.checkNotNull(priority, "priority must not be null");
    }

    public TaskUrl getTaskUrl() {
        return taskUrl;
    }

    public String getUrlStr() {
        return urlStr;
    }

    Resource getResource() {
        return resource;
    }

    void setResource(Resource resource) {
        this.resource = resource;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public Priority getPriority() {
        return priority;
    }

    void setProgress(Progress progress) {
        this.progress = progress;
    }

    public Progress getProgress() {
        return progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "{url:" + urlStr + ", path:" + filePath + ", priority:" + priority + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Task)) {
            return false;
        }

        Task t = (Task) obj;

        return t.urlStr.equals(this.urlStr) && t.filePath.equals(this.filePath);

    }

    @Override
    public int hashCode() {
        int result = 17;
        if (urlStr != null) {
            result = 31 * result + urlStr.hashCode();
        }
        if (taskUrl != null) {
            result = 31 * result + taskUrl.hashCode();
        }
        result = 31 * result + filePath.hashCode();
        return result;
    }

    @Override
    public int compareTo(Task o) {
        return o.priority.value - this.priority.value;
    }
}
