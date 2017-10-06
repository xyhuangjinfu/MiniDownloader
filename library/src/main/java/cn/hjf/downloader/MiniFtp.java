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
import java.net.ServerSocket;
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

    private static final String TAG = Debug.appLogPrefix + "MiniFtp";

    private String host;

    private Socket commandSocket;
    private int commandPort;
    private InputStream commandIS;
    private OutputStream commandOS;

    private Socket dataSocket;
    private InputStream dataIS;
    private ServerSocket serverSocket;

    private String user;
    private String password;
    private String file;
    private String type = "I";

    /**
     * Initial MiniFtp by url string.
     *
     * @param urlStr
     * @throws Exception
     */
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

    /**
     * Initial MiniFtp by FtpTaskUrl.
     *
     * @param ftpTaskUrl
     */
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

    /**
     * **********************************************************************************************************
     * **********************************************************************************************************
     */

    /**
     * Connect to ftp server.
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        /** Build command transfer channel. */
        initCommandTransfer();

        /** Connect server. */
        hello();
        /** Send user. */
        user();
        /** Send password. */
        pass();
        /** Try to enter PASV mode. */
        if (!pasv()) {
            if (Debug.debug) {
                Log.d(TAG, "PASV command not implement, try to change to active mode");
            }
            /** PASV mode not implemented, enter active mode. */
            port();
        }

    }

    /**
     * Query file size.
     *
     * @return
     * @throws Exception
     */
    public long fileSize() throws Exception {
        return size();
    }

    /**
     * Reset download progress.
     *
     * @param offset
     * @throws Exception
     */
    public void setProgress(long offset) throws Exception {
        if (offset <= 0) {
            return;
        }
        /** Set offset */
        rest(offset);
    }

    /**
     * Get data transfer InputStream.
     *
     * @return
     * @throws Exception
     */
    public InputStream getInputStream() throws Exception {
        /** Set transfer type */
        type();

        /** Download file */
        retr();

        dataIS = dataSocket.getInputStream();
        return dataIS;
    }

    /**
     * Close MiniFtp, and release relative resources.
     *
     * @throws Exception
     */
    public void close() throws Exception {
        if (dataIS != null) {
            dataIS.close();
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }

        commandIS.close();
        commandOS.close();
        commandSocket.close();
    }

    /**
     * **********************************************************************************************************
     * **********************************************************************************************************
     */

    private void hello() throws Exception {
        String resConnect = readCommand();
        if (!resConnect.startsWith("220")) {
            throw new Exception("Server not ready : " + resConnect);
        }
    }

    private void user() throws Exception {
        writeCommand(("USER " + user + "\r\n").getBytes());
        String resUser = readCommand();
        if (!resUser.startsWith("331")) {
            throw new Exception("Username not okay");
        }
    }

    private void pass() throws Exception {
        writeCommand(("PASS " + password + "\r\n").getBytes());
        String resPass = readCommand();
        if (!resPass.startsWith("230")) {
            throw new Exception("User login fail");
        }
    }

    /**
     * Enter PASV mode.
     *
     * @return Whether tfp server implemented PASV command. true-implemented, false-not implemented.
     * @throws Exception
     */
    private boolean pasv() throws Exception {
        writeCommand("PASV\r\n".getBytes());
        String resMode = readCommand();

        if (resMode.startsWith("502")) {
            return false;
        }

        if (!resMode.startsWith("227")) {
            throw new Exception("Entering passive mode fail");
        }

        /** Made data socket connected. */
        dataSocket = new Socket(host, getPort(resMode));

        return true;
    }

    private void port() throws Exception {
        StringBuilder hostPort = new StringBuilder(23);

        String ip = NetworkUtil.getIPV4();
        if (ip == null) {
            throw new Exception("Change ftp to active mode, but get ip failed");
        }
        String[] ips = ip.split("\\.");
        for (int i = 0; i < ips.length; i++) {
            hostPort.append(ips[i]);
            hostPort.append(",");
        }

        serverSocket = NetworkUtil.availablePort();
        if (serverSocket == null) {
            throw new Exception("Change ftp to active mode, but no available port exist");
        }
        int localPort = serverSocket.getLocalPort();

        hostPort.append((localPort & 0xFF00) >>> 8);
        hostPort.append(",");
        hostPort.append((localPort & 0xFF));

        writeCommand(("PORT " + hostPort + "\r\n").getBytes());
        String resMode = readCommand();
        if (!resMode.startsWith("200")) {
            throw new Exception("Change to active mode failed");
        }
    }

    private long size() throws Exception {
        /** Get file size */
        writeCommand(("SIZE " + file + "\r\n").getBytes());
        String resSize = readCommand();
        if (!resSize.startsWith("213")) {
            throw new Exception("Get file status error.");
        }
        String lenStr = resSize.substring(resSize.lastIndexOf(" "));
        return Long.valueOf(lenStr.trim());
    }

    private void type() throws Exception {
        if (type != null) {
            writeCommand(("TYPE " + type + "\r\n").getBytes());
            String resType = readCommand();
            if (!resType.startsWith("200")) {
                throw new Exception("Type set to " + type + " failed");
            }
        }
    }

    private void rest(long offset) throws Exception {
        /** Set offset */
        writeCommand(("REST " + offset + "\r\n").getBytes());
        String resRest = readCommand();
        if (!resRest.startsWith("350")) {
            throw new Exception("Partial download error");
        }
    }

    private void retr() throws Exception {
        writeCommand(("RETR " + file + "\r\n").getBytes());

        /** Wait to ftp server connect, Made data socket connected. */
        if (serverSocket != null) {
            dataSocket = serverSocket.accept();
        }

        String resRetr = readCommand();
        if (!resRetr.startsWith("150")) {
            throw new Exception("Download error");
        }
    }

    /**
     * **********************************************************************************************************
     * **********************************************************************************************************
     */

    private void initCommandTransfer() throws Exception {
        commandSocket = new Socket(host, commandPort);
        commandIS = commandSocket.getInputStream();
        commandOS = commandSocket.getOutputStream();
    }

    private void writeCommand(byte[] data) throws Exception {
        if (Debug.debug) {
            Log.d(TAG, "writeCommand : " + new String(data));
        }

        commandOS.write(data);
        commandOS.flush();
    }

    private String readCommand() throws Exception {
        byte[] buffer = new byte[500];
        int readCount = commandIS.read(buffer);
        String response = new String(buffer, 0, readCount);

        if (Debug.debug) {
            Log.d(TAG, "readCommand : " + response);
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
