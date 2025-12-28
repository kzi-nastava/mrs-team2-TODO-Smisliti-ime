package com.example.getgo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupWindowInsets();
        setupListeners();
    }

    private void initializeViews() {
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupWindowInsets() {
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void handleRegister() {
        // TODO: Implement registration logic
    }

    private void navigateToLogin() {
        finish(); // Close RegisterActivity to return to LoginActivity
    }
}