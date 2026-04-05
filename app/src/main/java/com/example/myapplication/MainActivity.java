package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText email=findViewById(R.id.et_email);
        EditText pass=findViewById(R.id.et_password);
        Button btn=findViewById(R.id.btn_connect);
        TextView signup=findViewById(R.id.tv_signup);

        signup.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this,SignupActivity.class);
            startActivity(intent);
        });
        btn.setOnClickListener(v ->{
            String emailText=email.getText().toString();
            String password=pass.getText().toString();
            if(emailText.equals("user@gmail.com") && password.equals("123456")){
                Toast.makeText(this,"connected",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,WelcomeActivity.class);
                intent.putExtra("email",emailText);
                startActivity(intent);
            }else if(emailText.isEmpty() || password.isEmpty()){
                Toast.makeText(MainActivity.this,"Please fill all fields",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"wrong email or password",Toast.LENGTH_SHORT).show();
            }

        });

    }
}