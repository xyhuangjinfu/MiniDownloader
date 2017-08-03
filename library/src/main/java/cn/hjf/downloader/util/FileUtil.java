package cn.hjf.downloader.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cn.hjf.downloader.Task;

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

    public static boolean saveTask(Context context, Task task) {
        return save(getTaskPath(context, task.getUrlStr()), task);
    }

    private static String urlToFileName(String urlStr) {
        int firstHalfLength = urlStr.length() / 2;
        String fileName = String.valueOf(urlStr.substring(0, firstHalfLength).hashCode());
        fileName += String.valueOf(urlStr.substring(firstHalfLength).hashCode());
        return fileName;
    }

    private static String getRootCachePath(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    private static String getTaskPath(Context context, String fileName) {
        return getRootCachePath(context) + File.separator + "task" + File.separator + fileName;
    }

    private static String getHttpResourcePath(Context context, String fileName) {
        return getRootCachePath(context) + File.separator + "http" + File.separator + fileName;
    }

    private static boolean save(String path, Serializable object) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static Object read(String path) {
        ObjectInputStream ois = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
