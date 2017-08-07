package cn.hjf.downloader.sample;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.hjf.downloader.ErrorListener;
import cn.hjf.downloader.Listener;
import cn.hjf.downloader.MiniDownloader;
import cn.hjf.downloader.Progress;
import cn.hjf.downloader.Task;

public class MainActivity extends AppCompatActivity {

    private ListView taskListView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private MiniDownloader miniDownloader;

    private Listener listener = new Listener() {
        @Override
        public void onStart(Task task) {
            refreshData();
        }

        @Override
        public void onProgress(Task task, Progress progress) {
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
    };
    private ErrorListener errorListener = new ErrorListener() {
        @Override
        public void onResourceModified(Task task) {
            refreshData();
        }

        @Override
        public void onError(Task task, Exception error) {
            refreshData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        miniDownloader = new MiniDownloader(this);

        taskListView = (ListView) findViewById(R.id.taskLv);
        taskList.addAll(createTask());

        taskAdapter = new TaskAdapter(this, taskList);

        taskListView.setAdapter(taskAdapter);

        taskAdapter.setOnEventListener(new TaskAdapter.OnEventListener() {
            @Override
            public void onStart(Task task) {
                miniDownloader.start(task);
            }

            @Override
            public void onStop(Task task) {
                Log.e("O_O", "onStop");
                miniDownloader.stop(task);
            }
        });
    }

    private List<Task> createTask() {
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

        return taskList;
    }

    private void refreshData() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                taskAdapter.notifyDataSetChanged();
            }
        });
    }
}
