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
 * A url to represent a task, we don't need to use it most commonly except some special characters occurs in url.
 *
 * @author huangjinfu
 */

abstract class TaskUrl implements Serializable {

    public final String protocol;
    public final String host;
    public final int port;

    public TaskUrl(String protocol, String host, int port) {
        checkValidity(protocol, host);

        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    private void checkValidity(String protocol, String host) {
        CheckUtil.checkStringNotNullOrEmpty(protocol, "protocol must not be null!");
        CheckUtil.checkStringNotNullOrEmpty(host, "host must not be null!");
    }

    /**
     * Translate to a String url.
     *
     * @return
     */
    public abstract String toUrl();

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + protocol.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
