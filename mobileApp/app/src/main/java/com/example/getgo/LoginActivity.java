package com.example.getgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.getgo.model.UserRole;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegisterLink.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String username = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try login
        UserRole role = authenticateUser(username, password);

        if (role != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ROLE", role.name());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleForgotPassword() {
        Toast.makeText(this, "Password reset not implemented", Toast.LENGTH_SHORT).show();
    }

    // TODO: Replace with real authentication
    private UserRole authenticateUser(String username, String password) {
        if (username.equals("a") && password.equals("a")) {
            return UserRole.ADMIN;
        } else if (username.equals("d") && password.equals("d")) {
            return UserRole.DRIVER;
        } else if (username.equals("p") && password.equals("p")) {
            return UserRole.PASSENGER;
        }
        return null;
    }
}