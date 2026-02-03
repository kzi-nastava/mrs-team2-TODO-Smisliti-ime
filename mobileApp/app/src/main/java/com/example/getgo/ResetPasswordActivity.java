package com.example.getgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private TextView tvBackToLogin;
    private TextView tvResetEmail; // new label to show user's email
    private String token;

    private static final String PREFS_NAME = "getgo_prefs";
    private static final String PREF_BACKEND_URL = "backend_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupGuestToolbar();

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // new email label (added to layout)
        tvResetEmail = findViewById(R.id.tvResetEmail);

        // read token/email from deep link, extras or full resetUrl
        Intent intent = getIntent();
        Uri data = intent != null ? intent.getData() : null;
        token = null;
        String email = null;

        if (data != null) {
            // preferred: ?token=...
            token = data.getQueryParameter("token");
            email = data.getQueryParameter("email");

            // fallback: maybe token in path segment
            if (token == null || token.isEmpty()) {
                List<String> segs = data.getPathSegments();
                if (segs != null && !segs.isEmpty()) {
                    String last = segs.get(segs.size() - 1);
                    if (last != null && !last.isEmpty()) token = last;
                }
            }
        }

        // fallback: extras (some mail clients / browsers pass token/email as extras)
        if ((token == null || token.isEmpty()) && intent != null) {
            token = intent.getStringExtra("token");
        }
        if ((email == null || email.isEmpty()) && intent != null) {
            email = intent.getStringExtra("email");
        }

        // fallback: sometimes the mail contains a full reset URL passed as "resetUrl" extra
        if ((token == null || token.isEmpty()) && intent != null) {
            String resetUrl = intent.getStringExtra("resetUrl");
            if (resetUrl != null && !resetUrl.isEmpty()) {
                token = extractTokenFromUrl(resetUrl);
            }
        }

        if (email != null && !email.isEmpty()) {
            tvResetEmail.setText(email);
            tvResetEmail.setVisibility(View.VISIBLE);
        } else {
            tvResetEmail.setText("");
            tvResetEmail.setVisibility(View.GONE);
        }

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
                String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (token == null || token.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Missing reset token. Use the link from your email.", Toast.LENGTH_LONG).show();
                    return;
                }

                btnResetPassword.setEnabled(false);

                // network call off UI thread
                new Thread(() -> {
                    String backend = ApiClient.getClient().baseUrl().toString();
                    String endpoint = backend + "api/auth/reset-password";

                    String errorMsg = null;
                    try {
                        JSONObject body = new JSONObject();
                        body.put("token", token);
                        body.put("password", newPassword);
                        String resp = postJson(endpoint, body.toString());
                        // success if no exception
                    } catch (Exception e) {
                        errorMsg = e.getMessage();
                    }

                    final String finalError = errorMsg;
                    runOnUiThread(() -> {
                        btnResetPassword.setEnabled(true);
                        if (finalError == null) {
                            Toast.makeText(ResetPasswordActivity.this, "Password successfully reset! Please login.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Reset failed: " + finalError, Toast.LENGTH_LONG).show();
                        }
                    });
                }).start();
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // helper to read backend URL from prefs (use emulator fallback)
    private String getBackendBaseUrl() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String backend = prefs.getString(PREF_BACKEND_URL, null);
        if (backend == null || backend.isEmpty()) {
            // emulator default (use ngrok or LAN IP on real device via settings)
            backend = "http://10.0.2.2:8080/";
        }
        if (!backend.endsWith("/")) backend = backend + "/";
        return backend;
    }

    // simple POST JSON helper throws on non-2xx
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

    // Helper: best-effort extract token=... from any URL string
    private String extractTokenFromUrl(String url) {
        if (url == null) return null;
        try {
            Uri u = Uri.parse(url);
            String t = u.getQueryParameter("token");
            if (t != null && !t.isEmpty()) return t;
            // fallback: try simple substring search
            int idx = url.indexOf("token=");
            if (idx >= 0) {
                String sub = url.substring(idx + "token=".length());
                int amp = sub.indexOf('&');
                if (amp >= 0) sub = sub.substring(0, amp);
                return sub;
            }
        } catch (Exception ignored) {}
        return null;
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