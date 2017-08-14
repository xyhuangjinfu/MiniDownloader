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
 *
 * @author huangjinfu
 */

public final class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Total size of resource, based on the byte unit.
     */
    private final long total;
    /**
     * Already downloaded size.
     */
    private volatile long downloaded;
    /**
     * Current estimate network speed, based on the kb/s unit.
     */
    private transient volatile double networkSpeed;

    /**
     * Create new Progress instance.
     *
     * @param total Total size of this progress, total size can't be changed since Progress instance be constructed.
     */
    public Progress(long total) {
        this.total = total;
    }

    /**
     * Get total size of this task.
     *
     * @return
     */
    public long getTotal() {
        return total;
    }

    /**
     * Get already downloaded size of this task.
     *
     * @return
     */
    public long getDownloaded() {
        return downloaded;
    }

    /**
     * Get estimate temporary network speed.
     *
     * @return
     */
    public double getNetworkSpeed() {
        return networkSpeed;
    }

    void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    void setNetworkSpeed(double networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    /**
     * Indicate that this Progress is finished.
     *
     * @return true if progress finished.
     */
    boolean finished() {
        return total == downloaded;
    }
}
