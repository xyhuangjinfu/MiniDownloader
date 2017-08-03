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
import cn.hjf.downloader.Task;
import cn.hjf.downloader.http.HttpDownloader;

public class MainActivity extends AppCompatActivity {

    ProgressBar pb1;
    Button pauseBtn1;
    Task sfdl = null;

    ProgressBar pb2;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb1 = (ProgressBar) findViewById(R.id.pb1);
        pauseBtn1 = (Button) findViewById(R.id.pause1);

        pb2 = (ProgressBar) findViewById(R.id.pb2);

        final HttpDownloader httpDownloader = new HttpDownloader();

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
                    httpDownloader.pause(sfdl);
                }
            }
        });

        try {

            sfdl = httpDownloader.download(this, url_1, path_1, new Listener() {
                @Override
                public void onStart() {
                    Log.e("O_O", "onStart");
                }

                @Override
                public void onProgress(final long total, final long progress) {
//                    Log.e("O_O", "onProgress, total : " + total + "  -  progress : " + progress);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pb1.setMax(100);
                            pb1.setProgress((int) (progress * 100 / total));
                        }
                    });
                }

                @Override
                public void onFinish() {
                    Log.e("O_O", "onFinish");
                }

            }, new ErrorListener() {
                @Override
                public void onInvalidUrl(String urlStr) {
                    Log.e("O_O", "onInvalidUrl : " + urlStr);
                }

                @Override
                public void onError(Exception error) {
                    Log.e("O_O", "onError : " + error.getMessage());
                }
            });


//            httpDownloader.download(url_2, path_2, new Listener() {
//                @Override
//                public void onStart() {
//                    Log.e("O_O", "onStart");
//                }
//
//                @Override
//                public void onProgress(final long total, final long progress) {
////                    Log.e("O_O", "onProgress, total : " + total + "  -  progress : " + progress);
//
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            pb2.setMax(100);
//                            pb2.setProgress((int) (progress * 100 / total));
//                        }
//                    });
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.e("O_O", "onFinish");
//                }
//
//            }, new ErrorListener() {
//                @Override
//                public void onInvalidUrl(String urlStr) {
//                    Log.e("O_O", "onInvalidUrl : " + urlStr);
//                }
//
//                @Override
//                public void onError(Exception error) {
//                    Log.e("O_O", "onError : " + error.getMessage());
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
