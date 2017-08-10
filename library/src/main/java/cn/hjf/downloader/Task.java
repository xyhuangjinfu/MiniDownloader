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

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class Task implements Serializable, Comparable<Task> {

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

    public Task(String urlStr, String filePath, Listener listener, ErrorListener errorListener) {
        this(urlStr, filePath, listener, errorListener, Priority.NORMAL);
    }

    public Task(String urlStr, String filePath, Listener listener, ErrorListener errorListener, Priority priority) {
        this.urlStr = urlStr;
        this.filePath = filePath;
        this.listener = listener;
        this.errorListener = errorListener;
        this.priority = priority;
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
        result = 31 * result + urlStr.hashCode();
        result = 31 * result + filePath.hashCode();
        return result;
    }

    @Override
    public int compareTo(Task o) {
        return o.priority.value - this.priority.value;
    }
}
