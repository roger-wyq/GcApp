package com.example.gcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class ReportActivity extends AppCompatActivity {

    private static final String TAG = "ReportActivity";
    private String FileName;
    private String name, age, gender;

    private int p=0;//当前进度
    private ProgressBar pb_main_download;//进度条
    private TextView tv_main_desc;//显示文本的控件
    private MyHandler myHandler=new MyHandler();//新写的Handler类
    private TextView upload;

    private Button exit;

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int code=msg.what;//接受处理码
            switch (code){
                case 1:
                    p++;
                    pb_main_download.setProgress(p);//给进度条的当前进度赋值
                    tv_main_desc.setText(p+"%");//显示当前进度为多少
                    if(p == 100)
                        upload.setText("上传完成！");
                    break;
            }
        }
    }

    public class myThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(true){
                try {
                    Thread.sleep(100);//使线程休眠0.1秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(p==100){//当前进度等于总进度时退出循环
                    //p=0;
                    break;
                }
                Message msg=new Message();
                msg.what=1;
                myHandler.sendMessage(msg);//发送处理码
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.upload);
        //根据ID找到进度条
        pb_main_download=findViewById(R.id.progressBar);
        //根据ID找到显示文本的控件
        tv_main_desc=findViewById(R.id.textView8);

        upload = findViewById(R.id.upload);


        if(0==p){//如果当前进度为0
            new myThread().start();//开启线程
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Intent intent = getIntent();
        name = intent.getStringExtra("user_name");
        age = intent.getStringExtra("user_age");
        gender = intent.getStringExtra("user_gender");
        Log.e(TAG, "onCreate: " + name + age + gender);

        FileName = getExternalCacheDir().getAbsolutePath();
        FileName += ("/" + name + age + gender);
        try {
            packFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadFile("http://47.242.41.20:5000/api/uploadtosave",
                FileName + ".zip");
    }

    private void packFile() throws Exception {
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 1; i <= 17; i++) {
            File file = new File(FileName + i + ".3gp");
            files.add(file);
        }
        FileOutputStream out = new FileOutputStream(FileName + ".zip");
        ZipUtils.toZip(files, out);
    }

    private void uploadFile(String uploadUrl, String uploadFilePath) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "----WebKitFormBoundaryPayQyop3U33kzugj";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            Log.d(TAG, "uploadFile: !!");

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            Log.d(TAG, "uploadFile: !!!");
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"audiofile\"; filename=\"" + name + age + gender + ".zip\"" + end);
//          dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""
//                  + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1) + "\"" + end);
            dos.writeBytes(end);
            // 文件通过输入流读到Java代码中-++++++++++++++++++++++++++++++`````````````````````````
            FileInputStream fis = new FileInputStream(uploadFilePath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);

            }
            fis.close();
            System.out.println("file send to server............");
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            // 读取服务器返回结果
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            dos.close();
            Log.d(TAG, "uploadFile: " + result);
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}



