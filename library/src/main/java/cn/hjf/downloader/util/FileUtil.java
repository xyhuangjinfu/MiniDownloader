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
}
