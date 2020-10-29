package com.example.gcapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    private static final int NOISEOK = 1;
    private static final int NOISENO = -1;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;   // 表示是否获得录音权限;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private static final String TAG = "StartActivity";

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case NOISENO :
                    final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                    //builder.setTitle("确认" ) ;
                    builder.setMessage("当前环境噪音过大！");
                    builder.setPositiveButton("重新检测", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "onClick: !!!");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getNoise();
                                }
                            }).start();
                        }
                    });
                    builder.show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            Toast.makeText(StartActivity.this, "未获得录音权限，程序退出！", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_start);
        // 录音权限申请
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        new Thread(new Runnable() {
            @Override
            public void run() {
                getNoise();
            }
        }).start();
    }



    public void getNoise() {
        NoiseDetection nd = new NoiseDetection();
        double noiseLevel = nd.getNoiseLevel();
        Log.e(TAG, "getNoise: " + noiseLevel );
        if (noiseLevel < 50) {
            Message message = new Message();
            message.what = NOISEOK;
            handler.sendMessage(message);
            // 环境噪音符号要求好进入下一步
            Intent intent = new Intent(StartActivity.this, infoSelectActivity.class);
            startActivity(intent);
        } else {
            Message message = new Message();
            message.what = NOISENO;
            handler.sendMessage(message);
        }
    }
}
