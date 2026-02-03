package com.example.getgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.api.ApiClient;
import com.example.getgo.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPasswordRequestActivity extends AppCompatActivity {

    private TextInputEditText etEmailRequest;
    private MaterialButton btnSendRequest;
    private MaterialButton btnBackToLogin;

    private static final String PREFS_NAME = "getgo_prefs";
    private static final String PREF_BACKEND_URL = "backend_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_request);
        setupGuestToolbar();

        etEmailRequest = findViewById(R.id.etEmailRequest);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendRequest.setOnClickListener(v -> {
            String email = etEmailRequest.getText() != null ? etEmailRequest.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendRequest.setEnabled(false);

            new Thread(() -> {
                String backend = getBackendBaseUrl();
                String endpoint = backend + "api/auth/forgot-password";
                String error = null;
                try {
                    JSONObject body = new JSONObject();
                    body.put("email", email);
                    postJson(endpoint, body.toString());
                } catch (Exception e) {
                    error = e.getMessage();
                }

                String finalError = error;
                runOnUiThread(() -> {
                    btnSendRequest.setEnabled(true);
                    if (finalError == null) {
                        // Do not reveal whether email exists; show generic message
                        Toast.makeText(this, "If the email exists, a reset link has been sent.", Toast.LENGTH_LONG).show();
                        // return to login
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to send reset link: " + finalError, Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private String getBackendBaseUrl() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String backend = prefs.getString(PREF_BACKEND_URL, null);
        if (backend == null || backend.isEmpty()) {
            backend = String.valueOf(ApiClient.getClient().baseUrl());
        }
        if (!backend.endsWith("/")) backend = backend + "/";
        return backend;
    }

    private String postJson(String endpoint, String json) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            byte[] out = json.getBytes("UTF-8");
            conn.setFixedLengthStreamingMode(out.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) throw new Exception("No response from server (code " + code + ")");
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String resp = s.hasNext() ? s.next() : "";
            if (code >= 200 && code < 300) return resp;
            throw new Exception("Server returned " + code + ": " + resp);
        } finally {
            conn.disconnect();
        }
    }

    private void setupGuestToolbar() {
        View includeRoot = findViewById(R.id.guest_toolbar);
        androidx.appcompat.widget.Toolbar toolbar = null;
        if (includeRoot instanceof androidx.appcompat.widget.Toolbar) {
            toolbar = (androidx.appcompat.widget.Toolbar) includeRoot;
        } else if (includeRoot != null) {
            toolbar = includeRoot.findViewById(R.id.toolbar);
        }
        if (toolbar == null) return;

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

        // Wire toolbar buttons
        View btnRegister = toolbar.findViewById(R.id.btnRegisterToolbar);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }

        View btnLogin = toolbar.findViewById(R.id.btnLoginToolbar);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
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
