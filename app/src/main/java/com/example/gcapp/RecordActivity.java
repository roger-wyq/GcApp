package com.example.gcapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String recordFileName = null;

    // 录音键和播放键
    private Button recordButton = null;
    private Button playButton = null;

    // 录音器和播放器
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    // 录音计时器
    Chronometer chronometer = null;

    /*
     *进度条相关
     */
    protected SeekBar seekBar = null;//进度条
    private Timer timer = null;//定时器
    protected TextView tv_start = null;//开始时间
    protected TextView tv_end = null;//结束时间
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突。



    boolean mStartPlaying = true;
    boolean mStartRecording = true;

    private boolean permissionToRecordAccepted = false;   // 表示是否获得录音权限;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(!permissionToRecordAccepted){
            Toast.makeText(RecordActivity.this, "未获得录音权限，程序退出！", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void onRecord(boolean start){
        if(start){
            startRecording();
        }else {
            stopRecording();
        }
    }

    private void onPlay(boolean start){
        if(start){
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void stopPlaying() {
        timer.cancel();
        player.release();
        player = null;
        Log.d(TAG, "stopPlaying: ???");
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(recordFileName);
            player.prepare();
            player.start();
        }catch (IOException e){
            Log.e(TAG, "startPlaying");
        }
        int duration = player.getDuration();
        seekBar.setMax(duration);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isSeekbarChaning){
                    seekBar.setProgress(player.getCurrentPosition());
                }
            }
        },0, 50);
    }

    private void stopRecording() {
        chronometer.stop();
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        }catch (IOException e){
            Log.e(TAG, "startRecording: prepare() failed");
        }
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        recorder.start();
    }

    /*
     *计算播放时间
     */
    private String calculateTime(int time){
        int minute;
        int second;
        String timeStr = "";
        if(time >= 60){
            minute = time / 60;
            second = time % 60;
            if(minute >= 0 && minute < 10) {
                timeStr += ("0" + minute);
            }else {
                timeStr += minute;
            }
            if(second >= 0 && second < 10) {
                timeStr += (":0" + second);
            }else {
                timeStr += (":" + second);
            }
        }else {
            timeStr += "00:";
            second = time;
            if(second >= 0 && second < 10) {
                timeStr += ("0" + second);
            }else {
                timeStr += second;
            }
        }
        return timeStr;
    }

    /*
     *初始化界面
     */
    private void initView(){
        playButton = (Button) findViewById(R.id.play);
        recordButton = (Button) findViewById(R.id.record);
        chronometer = (Chronometer) findViewById(R.id.chr);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        tv_end = (TextView) findViewById(R.id.tv_end);
        tv_start = (TextView) findViewById(R.id.tv_start);

        /*
         *为录音键和播放键添加点击事件监听
         */
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if(mStartPlaying){
                    playButton.setText("停止播放");
                } else {
                    playButton.setText("开始播放");
                }
                mStartPlaying = !mStartPlaying;
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if(mStartRecording){
                    recordButton.setText("停止录音");
                } else {
                    recordButton.setText("开始录音");
                }
                mStartRecording = !mStartRecording;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(player != null) {
                    int duration2 = player.getDuration();
                    int position = player.getCurrentPosition();
                    tv_start.setText(calculateTime(position / 1000));
                    tv_end.setText(calculateTime(duration2 / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning = false;
                player.seekTo(seekBar.getProgress());
                tv_start.setText(calculateTime(player.getCurrentPosition() / 1000));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        String name = intent.getStringExtra("user_name");

        recordFileName = getExternalCacheDir().getAbsolutePath();
        recordFileName += ("/" + name + ".3gp");

        Log.e(TAG, "onCreate: " + recordFileName);
        ///storage/emulated/0/Android/data/com.example.gcapp/cache/Roget.3gp

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initView();

    }

    // 程序结束时释放资源
    @Override
    protected void onStop() {
        super.onStop();
        if(recorder != null){
            recorder.release();
            recorder = null;
        }

        if(player != null){
            player.release();
            player = null;
        }
    }
}
