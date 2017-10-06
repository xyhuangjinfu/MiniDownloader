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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A url for ftp protocol.
 *
 * @author huangjinfu
 */

public class FtpTaskUrl extends TaskUrl {

    public final String user;
    public final String password;
    public final String type;
    public final String path;

    public FtpTaskUrl(@NonNull String host,
                      int port,
                      @Nullable String user,
                      @Nullable String password,
                      @NonNull String path) {
        this(host, port, user, password, path, "I");
    }

    public FtpTaskUrl(@NonNull String host,
                      int port,
                      @Nullable String user,
                      @Nullable String password,
                      @NonNull String path,
                      @Nullable String type) {
        super("ftp", host, port);

        this.user = user;
        this.password = password;
        this.type = type;
        this.path = path;
    }

    @Override
    public String toUrl() {
        try {
            String encodedUser = URLEncoder.encode(user, "UTF-8");
            String encodedPassword = URLEncoder.encode(password, "UTF-8");

            StringBuilder sb = new StringBuilder(
                    protocol.length() +
                            host.length() +
                            encodedUser.length() +
                            encodedPassword.length() +
                            type.length() + 20);

            sb.append(protocol);
            sb.append("://");

            sb.append(encodedUser);
            sb.append(":");
            sb.append(encodedPassword);
            sb.append("@");

            sb.append(host);
            sb.append(":");
            sb.append(port);
            sb.append("/");
            sb.append(path);

            sb.append(";type=");
            sb.append(type);

            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Something occurred wrong when encode url.", e.getCause());
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (user != null) {
            result = 31 * result + user.hashCode();
        }
        if (password != null) {
            result = 31 * result + password.hashCode();
        }
        result = 31 * result + path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
