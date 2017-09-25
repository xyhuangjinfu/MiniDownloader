package cn.hjf.downloader.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.hjf.downloader.ErrorListener;
import cn.hjf.downloader.Listener;
import cn.hjf.downloader.MiniDownloader;
import cn.hjf.downloader.Task;

public class MainActivity extends AppCompatActivity {

    private ListView taskListView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    private Listener listener = new Listener() {
        @Override
        public void onWait(Task task) {
            refreshData();
        }

        @Override
        public void onStart(Task task) {
            refreshData();
        }

        @Override
        public void onProgressUpdate(Task task) {
            refreshData();
        }

        @Override
        public void onStop(Task task) {
            refreshData();
        }

        @Override
        public void onFinish(Task task) {
            refreshData();
        }

        @Override
        public void onDelete(Task task) {
            refreshData();
        }
    };
    private ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onError(Task task, Exception error) {
            showError(error.getMessage());
            refreshData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MiniDownloader.getInstance().init(this);
        MiniDownloader.getInstance().setDebuggable(true);

        taskListView = (ListView) findViewById(R.id.taskLv);
        taskList.addAll(getTaskList());

        taskAdapter = new TaskAdapter(this, taskList);

        taskListView.setAdapter(taskAdapter);

        taskAdapter.setOnEventListener(new TaskAdapter.OnEventListener() {
            @Override
            public void onStart(Task task) {
                MiniDownloader.getInstance().start(task);
            }

            @Override
            public void onStop(Task task) {
                MiniDownloader.getInstance().stop(task);
            }

            @Override
            public void onDelete(Task task) {
                MiniDownloader.getInstance().delete(task);
            }
        });
    }

    private List<Task> getTaskList() {
        Set<Task> taskSet = new HashSet<>();
        taskSet.addAll(getUnfinishedTasks());
        taskSet.addAll(createNewTask());
        return new ArrayList<>(taskSet);
    }

    private List<Task> createNewTask() {
        List<Task> taskList = new ArrayList<>();

        taskList.add(
                new Task(
                        "http://haixi.jb51.net:8080/201506/books/sfdl3.rar",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "sfdl3.rar",
                        listener,
                        errorListener)
        );
        taskList.add(
                new Task(
                        "http://haixi.jb51.net:8080/201307/books/http_qwzn_jb51.net.rar",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "http_qwzn_jb51.rar",
                        listener,
                        errorListener)
        );
        taskList.add(
                new Task(
                        "http://wangshuo.jb51.net:81/201306/books/jsjkxcs_sjms_kfymxdxrjdjc_jb51.net.rar",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "sjms.pdf",
                        listener,
                        errorListener,
                        Task.Priority.HIGH)
        );
        taskList.add(
                new Task(
                        "ftp://hjf:666@192.168.1.64:21/jcip.pdf",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "jcip.pdf",
                        listener,
                        errorListener)
        );
        taskList.add(
                new Task(
                        "ftp://hjf:666@192.168.1.64:21/dclqdbcys.pdf",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "dclqdbcys.pdf",
                        listener,
                        errorListener)
        );
        taskList.add(
                new Task(
                        "ftp://hjf:666@192.168.1.64:21/hello.txt;type=i",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "hello.txt",
                        listener,
                        errorListener)
        );
        taskList.add(
                new Task(
                        "ftp://192.168.1.64:21/data.txt",
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + File.separator + "MiniDownloader" + File.separator + "data.txt",
                        listener,
                        errorListener)
        );

        return taskList;
    }

    private List<Task> getUnfinishedTasks() {
        List<Task> taskList = new ArrayList<>();
        taskList.addAll(MiniDownloader.getInstance().getStoppedTaskList());
        for (int i = 0; i < taskList.size(); i++) {
            taskList.get(i).setListener(listener);
            taskList.get(i).setErrorListener(errorListener);
        }
        return taskList;
    }

    private void refreshData() {
        taskAdapter.notifyDataSetChanged();
    }

    private void showError(final String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        MiniDownloader.getInstance().quit();
        super.onDestroy();
    }
}
