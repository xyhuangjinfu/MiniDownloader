package cn.hjf.downloader.sample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

import cn.hjf.downloader.Listener;
import cn.hjf.downloader.http.HttpDownloader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpDownloader httpDownloader = new HttpDownloader();


        String urlStr = "http://imgsrc.baidu.com/imgad/pic/item/267f9e2f07082838b5168c32b299a9014c08f1f9.jpg";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "MiniDownloader" + File.separator + "lalala.jpg";

        String url_1 = "http://haixi.jb51.net:8080/201506/books/sfdl3.rar";
        String path_1 = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "MiniDownloader" + File.separator + "sfdl3.rar";

        try {

            httpDownloader.download(url_1, path_1, new Listener() {
                @Override
                public void onStart() {
                    Log.e("O_O", "onStart");
                }

                @Override
                public void onProgress(double progress) {
                    Log.e("O_O", "onProgress : " + progress);
                }

                @Override
                public void onFinish() {
                    Log.e("O_O", "onFinish");
                }

                @Override
                public void onError(String error) {
                    Log.e("O_O", "onError : " + error);
                }
            });
        } catch (Exception e) {
             e.printStackTrace();
        }

    }
}
