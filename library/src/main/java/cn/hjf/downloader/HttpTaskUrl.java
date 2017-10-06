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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A url for http protocol.
 *
 * @author huangjinfu
 */

public class HttpTaskUrl extends TaskUrl {

    public final String encodedPath;

    public HttpTaskUrl(
            @NonNull String host,
            int port,
            @NonNull String path) {
        this("http", host, port, path);
    }

    protected HttpTaskUrl(String protocol, String host, int port, String path) {
        super(protocol, host, port);

        CheckUtil.checkStringNotNullOrEmpty(path, "filePath must not be null!");

        try {
            this.encodedPath = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Something occurred wrong when encode url.", e.getCause());
        }
    }

    @Override
    public String toUrl() {
        StringBuilder sb = new StringBuilder(protocol.length() + host.length() + encodedPath.length() + 20);

        sb.append(protocol);
        sb.append("://");
        sb.append(host);
        sb.append(":");
        sb.append(port);
        sb.append("/");
        sb.append(encodedPath);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + encodedPath.hashCode();
    }
}
