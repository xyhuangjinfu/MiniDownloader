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

/**
 * A util to check parameters which be passed into methods whether valid.
 *
 * @author huangjinfu
 */

final class CheckUtil {

    public static void checkNotNull(Object value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void checkStringNotNullOrEmpty(String str, String errorMessage) {
        if (str == null || "".equals(str)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
