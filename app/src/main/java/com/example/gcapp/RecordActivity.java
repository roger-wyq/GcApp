package com.example.gcapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String recordFileName = null;
    private static final int NOCONFIRM = 1;
    private static final int CONFIRM = 2;
    private int statu = NOCONFIRM;
    private int step = 1;
    private int questionNum = 1;

    // 录音键
    private Button recordButton = null;
    // 录音器
    private MediaRecorder recorder = null;
    //播放器
    private MediaPlayer player = null;
    //显示图片和文字
    private LinearLayout fmLayout;
    private ImageView img;
    private TextView textView;
    // 计时工具
    private TextView timer;
    private long baseTimer;
    // 信息
    private String name, age, gender;


    private TextView title = null;
    private TextView tip = null;


    boolean mStartRecording = true;

    private boolean permissionToRecordAccepted = false;   // 表示是否获得录音权限;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        name = intent.getStringExtra("user_name");
        age = intent.getStringExtra("user_age");
        gender = intent.getStringExtra("user_gender");
        Log.e(TAG, "onCreate: " + name + age + gender);
        recordFileName = getExternalCacheDir().getAbsolutePath();
        recordFileName += ("/" + name + age + gender);
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
    }

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


    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(String.format("%s%s.3gp", recordFileName, questionNum));
        questionNum++;
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        }catch (IOException e){
            Log.e(TAG, "startRecording: prepare() failed");
        }
        recorder.start();
    }

    private void startPlay(){
        if(player != null)
            player.stop();
        player = new MediaPlayer();
        Log.e(TAG, "startPlay: " + questionNum);
        int id = getApplication().getResources().getIdentifier("question" + questionNum,
                "raw", getApplicationContext().getPackageName());
        player = MediaPlayer.create(RecordActivity.this, id);

        player.start();
    }



    private void enterStep2(){
        step = 2;
        title.setText("STEP2 图片描述");
        tip.setText(R.string.step2_prompt);
        fmLayout.removeAllViews();
        addimg(R.mipmap.img1);
    }

    private void enterStep3(){
        step = 3;
        title.setText("STEP3 短文朗读");
        tip.setText(R.string.step3_prompt);

        fmLayout.removeAllViews();
        addtxt(R.string.text);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void enterStep4(){
        step = 4;
        title.setText("STEP4 词汇朗读");
        tip.setText(R.string.step3_prompt);
        fmLayout.removeAllViews();
        addtxt(R.string.words1);
    }

    private void enterStep5(){
        step = 5;
        title.setText("STEP5 主题统觉测试");
        tip.setText(R.string.step5_prompt);
        fmLayout.removeAllViews();
        addimg(R.mipmap.tat);
    }

    /*
     *初始化界面
     */
    private void initView(){
        recordButton = (Button) findViewById(R.id.record);
        tip = findViewById(R.id.textView);
        title = findViewById(R.id.steptitle);
        fmLayout = findViewById(R.id.LinL);

        addtxt(R.string.question1);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(statu == NOCONFIRM){
                    statu = CONFIRM;
                    startPlay();
                    recordButton.setText("开始回答");
                }
                else {
                    onRecord(mStartRecording);
                    if (mStartRecording) {
                        // 计时
                        RecordActivity.this.baseTimer = SystemClock.elapsedRealtime();
                        timer = (TextView) findViewById(R.id.timer);
                        final Handler startTimehandler = new Handler(){
                            public void handleMessage(android.os.Message msg) {
                                if (null != timer) {
                                    timer.setText((String) msg.obj);
                                }
                            }
                        };
                        new Timer("录音计时").scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                int time = (int) ((SystemClock.elapsedRealtime() - RecordActivity.this.baseTimer) / 1000);
                                //String hh = new DecimalFormat("00").format(time / 3600);
                                String mm = new DecimalFormat("00").format(time % 3600 / 60);
                                String ss = new DecimalFormat("00").format(time % 60);
                                String timeFormat = new String( mm + ":" + ss);
                                Message msg = new Message();
                                msg.obj = timeFormat;
                                startTimehandler.sendMessage(msg);
                            }
                        }, 0, 1000L);
                        recordButton.setText("完成");
                    } else {
                        timer.setText("00:00");
                        timer = null;
                        Log.e(TAG, "onClick: " + questionNum );
                        if(questionNum <= 9)
                            recordButton.setText("开始回答");
                        else
                            recordButton.setText("开始");
                        if(step == 1) {
                            if (questionNum <= 9) {
                                int id = getApplication().getResources().getIdentifier("question" + (questionNum),
                                        "string", getApplicationContext().getPackageName());
                                textView.setText(id);
                                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                                textView.setTextSize(15);
                                startPlay();
                            }
                            else
                               enterStep2();
                        } else if(step == 2){
                            if(questionNum <= 12){
                                int id = getApplication().getResources().getIdentifier("img" + (questionNum - 9),
                                        "mipmap", getApplicationContext().getPackageName());
                                img.setImageDrawable(getDrawable(id));
                            } else
                                enterStep3();
                        } else if(step == 3){
                                enterStep4();
                        } else if (step == 4) {
                            if(questionNum <= 16) {
                                int id = getApplication().getResources().getIdentifier("words" + (questionNum - 13),
                                        "string", getApplicationContext().getPackageName());
                                textView.setText(id);
                                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                                textView.setTextSize(15);
                                Log.e(TAG, "onClick: "+questionNum);
                            } else {
                                enterStep5();
                            }
                        } else if(step == 5) {
                            Intent intent = new Intent(RecordActivity.this, ReportActivity.class);
                            intent.putExtra("user_name", name);
                            intent.putExtra("user_age", age);
                            intent.putExtra("user_gender", gender);
                            startActivity(intent);
                        }
                    }
                    mStartRecording = !mStartRecording;
                }
            }
        });
    }

    private void addimg(int id) {
        img = new ImageView(this);
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));  //设置图片宽高
        img.setImageResource(id);
        fmLayout.addView(img);

    }

    private void addtxt(int id) {
        textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        textView.setText(id);
        textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        textView.setTextSize(15);
        fmLayout.addView(textView);
    }



}
