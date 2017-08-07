package cn.hjf.downloader.sample;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;

import cn.hjf.downloader.ErrorListener;
import cn.hjf.downloader.Listener;
import cn.hjf.downloader.MiniDownloader;
import cn.hjf.downloader.Task;

public class MainActivity extends AppCompatActivity {

    ProgressBar pb1;
    Button pauseBtn1;
    Button startBtn1;


    Task sfdl = null;

    ProgressBar pb2;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb1 = (ProgressBar) findViewById(R.id.pb1);
        pauseBtn1 = (Button) findViewById(R.id.pause1);
        startBtn1 = (Button) findViewById(R.id.start1);

        pb2 = (ProgressBar) findViewById(R.id.pb2);

        final MiniDownloader miniDownloader = new MiniDownloader();

        String urlStr = "http://imgsrc.baidu.com/imgad/pic/item/267f9e2f07082838b5168c32b299a9014c08f1f9.jpg";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "MiniDownloader" + File.separator + "lalala.jpg";

        String url_1 = "http://haixi.jb51.net:8080/201506/books/sfdl3.rar";
        String path_1 = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "MiniDownloader" + File.separator + "sfdl3.rar";

        String url_2 = "http://haixi.jb51.net:8080/201307/books/http_qwzn_jb51.net.rar";
        String path_2 = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "MiniDownloader" + File.separator + "http_qwzn_jb51.rar";



        pauseBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sfdl != null) {
                    miniDownloader.stop(sfdl);
                }
            }
        });
        startBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sfdl != null) {
                    miniDownloader.start(sfdl);
                }
            }
        });

        try {

            sfdl = new Task(
                    url_1,
                    path_1,
                    new Listener() {
                        @Override
                        public void onStart(Task task) {
                            Log.e("O_O", "onStart");
                        }

                        @Override
                        public void onProgress(Task task, long total, long progress) {
                            Log.e("O_O", "onProgress : " + total + " -> " + progress);
                        }

                        @Override
                        public void onStop(Task task) {
                            Log.e("O_O", "onStop");
                        }

                        @Override
                        public void onFinish(Task task) {
                            Log.e("O_O", "onFinish");
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onResourceModified(Task task) {
                            Log.e("O_O", "onResourceModified");
                        }

                        @Override
                        public void onError(Task task, Exception error) {
                            Log.e("O_O", "onError : " + error);
                        }
                    }
            );
           miniDownloader.start(sfdl);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
