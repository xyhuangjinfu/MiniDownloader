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

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Because of URLConnection not support partial download for ftp protocol,
 *
 * @author huangjinfu
 */

final class MiniFtp {

    private static final String TAG = "MiniFtp";

    private String host;

    private Socket commandSocket;
    private int commandPort;
    private InputStream commandIS;
    private OutputStream commandOS;

    private Socket dataSocket;
    private int dataPort;
    private InputStream dataIS;

    private String user;
    private String password;
    private String file;
    private String type = "I";

    public MiniFtp(String urlStr) throws Exception {
        URL url = new URL(urlStr);

        /** Parse user info. */
        String userInfo = url.getUserInfo();
        if (userInfo != null && !"".equals(userInfo)) {
            String[] userInfoArray = userInfo.split(":");
            user = userInfoArray[0];
            password = userInfoArray[1];
        }

        /** Parse host and port. */
        commandPort = url.getPort();
        host = url.getHost();

        /** Parse file and type. */
        file = url.getPath();
        if (file.contains(";")) {
            type = file.substring(file.lastIndexOf(";") + 1).replace("type=", "");
            file = file.substring(0, file.lastIndexOf(";"));
        }
    }

    public MiniFtp(FtpTaskUrl ftpTaskUrl) {
        /** Parse user info. */
        user = ftpTaskUrl.user;
        password = ftpTaskUrl.password;
        /** Parse host and port. */
        commandPort = ftpTaskUrl.port;
        host = ftpTaskUrl.host;
        /** Parse file and type. */
        file = ftpTaskUrl.path;
        type = ftpTaskUrl.type;
    }

    public void connect() throws Exception {
        commandSocket = new Socket(host, commandPort);
        commandIS = commandSocket.getInputStream();
        commandOS = commandSocket.getOutputStream();

        /** Connect server */
        String resConnect = readCommand();
        if (!resConnect.startsWith("220")) {
            throw new Exception("Server not ready : " + resConnect);
        }

        /** Input user */
        writeCommand(("USER " + user + "\r\n").getBytes());
        String resUser = readCommand();
        if (!resUser.startsWith("331")) {
            throw new Exception("Username not okay");
        }

        /** Input password */
        writeCommand(("PASS " + password + "\r\n").getBytes());
        String resPass = readCommand();
        if (!resPass.startsWith("230")) {
            throw new Exception("User login fail");
        }

        /** PASV mode transfer */
        writeCommand("PASV\r\n".getBytes());
        String resMode = readCommand();
        if (!resMode.startsWith("227")) {
            throw new Exception("Entering passive mode fail");
        }

        /** Get data port */
        dataPort = getPort(resMode);
        dataSocket = new Socket(host, dataPort);
    }

    public long size() throws Exception {
        /** Get file size */
        writeCommand(("SIZE " + file + "\r\n").getBytes());
        String resSize = readCommand();
        if (!resSize.startsWith("213")) {
            throw new Exception("Get file status error.");
        }
        String lenStr = resSize.substring(resSize.lastIndexOf(" "));
        return Long.valueOf(lenStr.trim());
    }

    public void rest(long offset) throws Exception {
        if (offset <= 0) {
            return;
        }
        /** Set offset */
        writeCommand(("REST " + offset + "\r\n").getBytes());
        String resRest = readCommand();
        if (!resRest.startsWith("350")) {
            throw new Exception("Partial download error");
        }
    }

    public InputStream getInputStream() throws Exception {
        /** Set transfer type */
        if (type != null) {
            writeCommand(("TYPE " + type + "\r\n").getBytes());
            String resType = readCommand();
            if (!resType.startsWith("200")) {
                throw new Exception("Type set to " + type + " failed");
            }
        }
        /** Download file */
        writeCommand(("RETR " + file + "\r\n").getBytes());
        String resRetr = readCommand();
        if (!resRetr.startsWith("150")) {
            throw new Exception("Download error");
        }
        /** Connect data transfer socket */

        dataIS = dataSocket.getInputStream();
        return dataIS;
    }

    public void close() throws Exception {
        dataIS.close();
        dataSocket.close();

        commandIS.close();
        commandOS.close();
        commandSocket.close();
    }

    /**
     * **********************************************************************************************************
     * **********************************************************************************************************
     */

    private void writeCommand(byte[] data) throws Exception {
        if (Debug.debug) {
            Log.e(TAG, "writeCommand : " + new String(data));
        }

        commandOS.write(data);
        commandOS.flush();
    }

    private String readCommand() throws Exception {
        byte[] buffer = new byte[500];
        commandIS.read(buffer);
        String response = new String(buffer);

        if (Debug.debug) {
            Log.e(TAG, "readCommand : " + response);
        }

        return response;
    }

    private static int getPort(String response) {
        Pattern pattern = Pattern.compile("\\(.*\\)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String d = matcher.group();
            StringBuilder sb = new StringBuilder(d);
            sb.deleteCharAt(0);
            sb.deleteCharAt(sb.length() - 1);
            String s = sb.toString();
            String[] l = s.split(",");
            return Integer.valueOf(l[4]) * 256 + Integer.valueOf(l[5]);
        }
        return 0;
    }
}
