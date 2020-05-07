package com.example.gcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class infoSelectActivity extends AppCompatActivity {

    private static final String TAG = "infoSelectActivity";
    private EditText EditText_name = null;
    private EditText EditText_age = null;
    private Spinner Spinner_gender = null;
    private Button Button_confirm = null;

    private String name;
    private String age;
    private String gender;

    private void initView(){
        EditText_name = (EditText) findViewById(R.id.editText_name);
        EditText_age = (EditText) findViewById(R.id.editText_age);
        Spinner_gender = (Spinner) findViewById(R.id.spinner_gender);
        Button_confirm = (Button) findViewById(R.id.button_confirm);

        Button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = EditText_name.getText().toString();
                age = EditText_age.getText().toString();
                gender = Spinner_gender.getSelectedItem().toString();
                if(name == null || age == null){
                    if(name == null)
                        Toast.makeText(infoSelectActivity.this, "请输入姓名！", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(infoSelectActivity.this, "请输入年龄！", Toast.LENGTH_LONG).show();
                } else {
                    enterMain();
                }
            }
        });

    }

    private void enterMain(){
        Intent intent = new Intent(infoSelectActivity.this, RecordActivity.class);
        intent.putExtra("user_name", name);
        intent.putExtra("user_age", age);
        intent.putExtra("user_gender", gender);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_select);
        initView();
    }
}
