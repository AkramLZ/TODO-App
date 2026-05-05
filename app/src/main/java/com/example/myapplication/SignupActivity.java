package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {
    private static final String AUTH_PREFS = "todo_auth";
    private static final String SESSION_EMAIL = "session_email";
    private static final String FIELD_AGE = "age";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_CREATED_AT = "created_at";
    private static final String FIELD_PASSWORD_HASH = "password_hash";
    private static final String FIELD_SALT = "salt";
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        SharedPreferences authPrefs = getSharedPreferences(AUTH_PREFS, MODE_PRIVATE);
        EditText email = findViewById(R.id.et_signup_email);
        EditText age = findViewById(R.id.et_signup_age);
        EditText adress = findViewById(R.id.et_signup_adress);
        EditText pass = findViewById(R.id.et_signup_password);
        EditText conpass = findViewById(R.id.et_signup_confpassword);
        Button signup = findViewById(R.id.btn_signup);
        Button cancel = findViewById(R.id.btn_cancel);

        signup.setOnClickListener(v -> {
            String emailText = normalizeEmail(email.getText().toString());
            String ageText = age.getText().toString().trim();
            String addressText = adress.getText().toString().trim();
            String password = pass.getText().toString();
            String confirmPassword = conpass.getText().toString();

            if (!validateSignup(emailText, ageText, addressText, password, confirmPassword)) {
                return;
            }

            if (hasAccount(authPrefs, emailText)) {
                Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            String salt = generateSalt();
            authPrefs.edit()
                    .putString(userKey(emailText, FIELD_PASSWORD_HASH), hashPassword(password, salt))
                    .putString(userKey(emailText, FIELD_SALT), salt)
                    .putString(userKey(emailText, FIELD_AGE), ageText)
                    .putString(userKey(emailText, FIELD_ADDRESS), addressText)
                    .putString(userKey(emailText, FIELD_CREATED_AT), String.valueOf(System.currentTimeMillis()))
                    .putString(SESSION_EMAIL, emailText)
                    .apply();

            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
            openTasks(emailText);
        });
        cancel.setOnClickListener(v -> {
           Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
           finish();
        });
    }

    private boolean validateSignup(String email, String age, String address, String password,
                                   String confirmPassword) {
        if (email.isEmpty() || age.isEmpty() || address.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidAge(age)) {
            Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void openTasks(String email) {
        Intent intent = new Intent(SignupActivity.this, TasksActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static boolean hasAccount(SharedPreferences authPrefs, String email) {
        return !normalizeEmail(email).isEmpty()
                && authPrefs.contains(userKey(email, FIELD_PASSWORD_HASH))
                && authPrefs.contains(userKey(email, FIELD_SALT));
    }

    private static boolean isValidAge(String age) {
        try {
            int value = Integer.parseInt(age);
            return value > 0 && value <= 120;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String userKey(String email, String field) {
        return "user." + normalizeEmail(email) + "." + field;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
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
