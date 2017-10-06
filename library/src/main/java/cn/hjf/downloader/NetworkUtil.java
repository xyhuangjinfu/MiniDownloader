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

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author huangjinfu
 */

public final class NetworkUtil {

    private static final String TAG = Debug.appLogPrefix + "NetworkUtil";

    /**
     * Get ipv4 address.
     *
     * @return
     */
    @Nullable
    public static String getIPV4() {
        try {
            Enumeration<NetworkInterface> net = NetworkInterface.getNetworkInterfaces();
            while (net.hasMoreElements()) {
                NetworkInterface networkInterface = net.nextElement();
                Enumeration<InetAddress> add = networkInterface.getInetAddresses();
                while (add.hasMoreElements()) {
                    InetAddress a = add.nextElement();
                    if (!a.isLoopbackAddress()
                            && !a.getHostAddress().contains(":")) {
                        if (Debug.debug) {
                            Log.d(TAG, "getIPV4 : " + a.getHostAddress());
                        }
                        return a.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find a available port, and return a ServerSocket which listen on that port.
     * You should release this ServerSocket when you never use it.
     *
     * @return
     */
    @Nullable
    public static ServerSocket availablePort() {
        for (int i = 65535; i > 1024; i--) {
            try {
                ServerSocket s = new ServerSocket(i);
                if (Debug.debug) {
                    Log.d(TAG, "availablePort : " + i);
                }
                return s;
            } catch (IOException e) {
                continue;
            }
        }
        return null;
    }
}
