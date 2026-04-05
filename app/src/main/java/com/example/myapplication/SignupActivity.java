package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        EditText email=findViewById(R.id.et_signup_email);
        EditText age=findViewById(R.id.et_signup_age);
        EditText adress=findViewById(R.id.et_signup_adress);
        EditText pass = findViewById(R.id.et_signup_password);
        EditText conpass=findViewById(R.id.et_signup_confpassword);
        Button signup=findViewById(R.id.btn_signup);
        Button cancel=findViewById(R.id.btn_cancel);

        signup.setOnClickListener(v ->{
            Toast.makeText(this, "A sign up request was sent to the server", Toast.LENGTH_SHORT).show();
        });
        cancel.setOnClickListener(v ->{
           Toast.makeText(this, "cancel", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(SignupActivity.this,MainActivity.class);
           startActivity(intent);
        });
    };
}