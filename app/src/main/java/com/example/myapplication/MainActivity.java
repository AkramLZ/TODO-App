package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_LOGOUT = "extra_logout";

    private static final String AUTH_PREFS = "todo_auth";
    private static final String SESSION_EMAIL = "session_email";
    private static final String FIELD_PASSWORD_HASH = "password_hash";
    private static final String FIELD_SALT = "salt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences authPrefs = getSharedPreferences(AUTH_PREFS, MODE_PRIVATE);
        if (getIntent().getBooleanExtra(EXTRA_LOGOUT, false)) {
            authPrefs.edit().remove(SESSION_EMAIL).apply();
        } else {
            String sessionEmail = normalizeEmail(authPrefs.getString(SESSION_EMAIL, ""));
            if (hasAccount(authPrefs, sessionEmail)) {
                openTasks(sessionEmail);
                return;
            }
        }

        EditText email = findViewById(R.id.et_email);
        EditText pass = findViewById(R.id.et_password);
        Button btn = findViewById(R.id.btn_connect);
        TextView signup = findViewById(R.id.tv_signup);

        signup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        btn.setOnClickListener(v -> {
            String emailText = normalizeEmail(email.getText().toString());
            String password = pass.getText().toString();

            if (emailText.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(MainActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (authenticate(authPrefs, emailText, password)) {
                authPrefs.edit().putString(SESSION_EMAIL, emailText).apply();
                Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
                openTasks(emailText);
            } else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openTasks(String email) {
        Intent intent = new Intent(MainActivity.this, TasksActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static boolean authenticate(SharedPreferences authPrefs, String email, String password) {
        if (!hasAccount(authPrefs, email)) {
            return false;
        }

        String salt = authPrefs.getString(userKey(email, FIELD_SALT), "");
        String storedHash = authPrefs.getString(userKey(email, FIELD_PASSWORD_HASH), "");
        return storedHash.equals(hashPassword(password, salt));
    }

    private static boolean hasAccount(SharedPreferences authPrefs, String email) {
        return !normalizeEmail(email).isEmpty()
                && authPrefs.contains(userKey(email, FIELD_PASSWORD_HASH))
                && authPrefs.contains(userKey(email, FIELD_SALT));
    }

    private static String userKey(String email, String field) {
        return "user." + normalizeEmail(email) + "." + field;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
