package com.example.getgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.api.ApiClient;
import com.example.getgo.auth.AuthRepository;
import com.example.getgo.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "getgo_prefs";
    private static final String PREF_BACKEND_URL = "backend_url";
    private static final String PREF_JWT = "jwt_token";

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply stored backend URL if present
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String backend = prefs.getString(PREF_BACKEND_URL, null);
        if (backend != null && !backend.isEmpty()) {
            ApiClient.setDefaultBaseUrl(backend);
            Log.d(TAG, "Using stored backend: " + backend);
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);
        setupGuestToolbar();

        findViewById(android.R.id.content).setBackgroundResource(R.drawable.background);

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
        final String username = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Client-side validation
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidEmail(username)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // disable button while network call runs
        btnLogin.setEnabled(false);

        new Thread(() -> {
            AuthRepository repo = AuthRepository.getInstance(this);
            AuthRepository.LoginResult res = repo.loginBackend(username, password);

            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                if (res.error == null) {
                    // save jwt token for later requests
                    if (res.token != null) {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putString(PREF_JWT, res.token).apply();
                    }
                    // pass role to MainActivity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("USER_ROLE", res.role != null ? res.role : "PASSENGER");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Login failed: " + res.error, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleForgotPassword() {
        // Open the "enter email" screen which sends the reset email
        Intent intent = new Intent(this, ForgotPasswordRequestActivity.class);
        startActivity(intent);
    }

    private void setupGuestToolbar() {
        // The include has id=guest_toolbar, but the Toolbar inside has id=toolbar.
        // findViewById will find the first match; ensure we get the actual Toolbar widget.
        View includeRoot = findViewById(R.id.guest_toolbar);
        androidx.appcompat.widget.Toolbar toolbar = null;
        if (includeRoot instanceof androidx.appcompat.widget.Toolbar) {
            toolbar = (androidx.appcompat.widget.Toolbar) includeRoot;
        } else if (includeRoot != null) {
            // fallback: find toolbar child
            toolbar = includeRoot.findViewById(R.id.toolbar);
        }
        if (toolbar == null) {
            Log.w(TAG, "Toolbar not found in setupGuestToolbar");
            return;
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }

        // Wire logo
        TextView logo = toolbar.findViewById(R.id.tvAppLogo);
        if (logo != null) {
            logo.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // Wire toolbar Register button
        View btnRegister = toolbar.findViewById(R.id.btnRegisterToolbar);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }

        // Hide Login button (we're on login page)
        View btnLogin = toolbar.findViewById(R.id.btnLoginToolbar);
        if (btnLogin != null) {
            btnLogin.setVisibility(View.GONE);
        }

        // Adjust toolbar padding for system bars
        View root = findViewById(android.R.id.content);
        final androidx.appcompat.widget.Toolbar finalToolbar = toolbar;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            finalToolbar.setPadding(
                finalToolbar.getPaddingLeft(),
                bars.top,
                finalToolbar.getPaddingRight(),
                finalToolbar.getPaddingBottom()
            );
            return insets;
        });
    }
}