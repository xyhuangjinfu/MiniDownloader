package cn.hjf.downloader.http;

import java.io.Serializable;

import cn.hjf.downloader.Task;

/**
 * Created by huangjinfu on 2017/8/3.
 */

class HttpTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Task task;
    private HttpResource httpResource;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public HttpResource getHttpResource() {
        return httpResource;
    }

    public void setHttpResource(HttpResource httpResource) {
        this.httpResource = httpResource;
    }
}
