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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangjinfu on 2017/8/9.
 */

final class MiniFtp {

    private String host;

    private Socket commandSocket;
    private int commandPort;
    private InputStream commandIS;
    private OutputStream commandOS;

    private Socket dataSocket;
    private int dataPort;
    private InputStream dataIS;

    private URL url;
    private String user;
    private String password;
    private String file;

    public MiniFtp(String urlStr) throws Exception {
        url = new URL(urlStr);
        String userInfo = url.getUserInfo();
        if (userInfo == null || "".equals(userInfo)) {
            throw new IllegalArgumentException("Unknown user info.");
        }
        String[] userInfoArray = userInfo.split(":");
        user = userInfoArray[0];
        password = userInfoArray[1];
        commandPort = url.getPort();
        host = url.getHost();
        file = url.getPath();
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
        writeCommand(("USER " + user + "\n").getBytes());
        String resUser = readCommand();
        if (!resUser.startsWith("331")) {
            throw new Exception("Username not okay");
        }

        /** Input password */
        writeCommand(("PASS " + password + "\n").getBytes());
        String resPass = readCommand();
        if (!resPass.startsWith("230")) {
            throw new Exception("User login fail");
        }

        /** PASV mode transfer */
        writeCommand("PASV\n".getBytes());
        String resMode = readCommand();
        if (!resMode.startsWith("227")) {
            throw new Exception("Entering passive mode fail");
        }
        /** Get data port */
        dataPort = getPort(resMode);
    }

    public long size() throws Exception {
        /** Get file size */
        writeCommand(("SIZE " + file + "\n").getBytes());
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
        writeCommand(("REST " + offset + "\n").getBytes());
        String resRest = readCommand();
        if (!resRest.startsWith("350")) {
            throw new Exception("Partial download error");
        }
    }

    public InputStream getInputStream() throws Exception {
        /** Download file */
        writeCommand(("RETR " + file + "\n").getBytes());
        String resRetr = readCommand();
        if (!resRetr.startsWith("150")) {
            throw new Exception("Download error");
        }
        /** Connect data transfer socket */
        dataSocket = new Socket(host, dataPort);
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
        commandOS.write(data);
        commandOS.flush();
    }

    private String readCommand() throws Exception {
        byte[] buffer = new byte[500];
        commandIS.read(buffer);
        return new String(buffer);
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
