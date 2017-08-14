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
import java.util.Collection;
import java.util.List;

/**
 * @author huangjinfu
 */

final class FileUtil {

    private static final String TAG = Debug.appLogPrefix + "FileUtil";

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

    public static void saveTaskList(Context context, Collection<Task> tasks) {
        for (Task task : tasks) {
            saveTask(context, task);
        }
    }

    public static void clearAllTasks(Context context) {
        File[] files = new File(getRootDir(context)).listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    public static boolean saveTask(Context context, @NonNull Task task) {
        File file = new File(getRootDir(context), urlToFileName(task));

        if (!createParentDirs(file.getAbsolutePath())) {
            return false;
        }

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
        File file = new File(getRootDir(context), urlToFileName(task));
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

    private static String urlToFileName(Task task) {
        return String.valueOf(task.getUrlStr().hashCode()) + String.valueOf(task.getFilePath().hashCode());
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
