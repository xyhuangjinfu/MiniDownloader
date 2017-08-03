package cn.hjf.downloader.util;

import java.io.File;

/**
 * Created by huangjinfu on 2017/8/2.
 */

public class FileUtil {

    public static boolean createParentDirs(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            return file.getParentFile().mkdirs();
        }
        return true;
    }

    private String urlToFileName(String urlStr) {
        int firstHalfLength = urlStr.length() / 2;
        String fileName = String.valueOf(urlStr.substring(0, firstHalfLength).hashCode());
        fileName += String.valueOf(urlStr.substring(firstHalfLength).hashCode());
        return fileName;
    }

//    private
}
