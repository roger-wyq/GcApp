package com.example.gcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ConfirmRecordActivity extends AppCompatActivity {
    private static final String TAG = "ConfirmRecordActivity";
    Button confirm_record = null;
    Button cancel_record = null;
    private String name, age, gender;

    private void initView(){
        confirm_record = findViewById(R.id.confirm_record);
        cancel_record = findViewById(R.id.cancel_record);
        cancel_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
            }
        });

        confirm_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: ");
                enterRecord();
            }
        });
    }

    private void enterRecord(){
        Log.e(TAG, "enterRecord: ");
        Intent intent = new Intent(ConfirmRecordActivity.this, RecordActivity.class);
        intent.putExtra("user_name", name);
        intent.putExtra("user_age", age);
        intent.putExtra("user_gender", gender);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_confirm_record);
        Intent intent = getIntent();
        name = intent.getStringExtra("user_name");
        age = intent.getStringExtra("user_age");
        gender = intent.getStringExtra("user_gender");
        initView();
    }
}
