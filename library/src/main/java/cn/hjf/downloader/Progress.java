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
 * A class to present download progress of a task.
 * Created by huangjinfu on 2017/8/7.
 */

public class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long total;
    private volatile long downloaded;

    public Progress(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public long getDownloaded() {
        return downloaded;
    }

    void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    boolean finished() {
        return total == downloaded;
    }
}
