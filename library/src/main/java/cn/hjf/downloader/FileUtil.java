package cn.hjf.downloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class FileUtil {

    /**
     * ************************************************************************************************************
     * ************************************************************************************************************
     */
    /**
     * @param context
     * @return
     */
    @NonNull
    public static List<Task> readTasksFromDisk(Context context) {
        List<Task> taskList = new ArrayList<>();
        File[] files = new File(getRootDir(context)).listFiles();
        if (files != null && files.length != 0) {
            for (int i = 0; i < files.length; i++) {
                Task task = parseTask(files[i]);
                if (task != null) {
                    taskList.add(task);
                }
            }

        }
        return taskList;
    }

    public static boolean saveTask(Context context, @NonNull Task task) {
        File file = new File(getRootDir(context), urlToFileName(task.getUrlStr()));
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(task);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean deleteTask(Context context, @NonNull Task task) {
        File file = new File(getRootDir(context), urlToFileName(task.getUrlStr()));
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
     * ************************************************************************************************************
     * ************************************************************************************************************
     */

    public static boolean createParentDirs(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            return file.getParentFile().mkdirs();
        }
        return true;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
     * ******************************************************************************************
     * ******************************************************************************************
     */

    private static String urlToFileName(String urlStr) {
        int firstHalfLength = urlStr.length() / 2;
        String fileName = String.valueOf(urlStr.substring(0, firstHalfLength).hashCode());
        fileName += String.valueOf(urlStr.substring(firstHalfLength).hashCode());
        return fileName;
    }

    private static String getRootDir(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + "MiniDownloader";
    }

    @Nullable
    private static Task parseTask(@NonNull File file) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            return (Task) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
