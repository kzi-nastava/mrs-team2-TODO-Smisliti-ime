package com.example.getgo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.auth.AuthRepository;
import com.example.getgo.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private TextView tvBackToLogin;
    private TextView tvResetEmail;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupGuestToolbar();

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvResetEmail = findViewById(R.id.tvResetEmail);

        Intent intent = getIntent();
        Uri data = intent != null ? intent.getData() : null;
        token = null;
        String email = null;

        if (data != null) {
            token = data.getQueryParameter("token");
            email = data.getQueryParameter("email");

            if (token == null || token.isEmpty()) {
                List<String> segs = data.getPathSegments();
                if (segs != null && !segs.isEmpty()) {
                    String last = segs.get(segs.size() - 1);
                    if (last != null && !last.isEmpty()) token = last;
                }
            }
        }

        if ((token == null || token.isEmpty()) && intent != null) {
            token = intent.getStringExtra("token");
        }
        if ((email == null || email.isEmpty()) && intent != null) {
            email = intent.getStringExtra("email");
        }

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

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidationUtils.isValidPassword(newPassword)) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Missing reset token. Use the link from your email.", Toast.LENGTH_LONG).show();
                return;
            }

            btnResetPassword.setEnabled(false);

            // Use AuthRepository instead of manual HTTP
            new Thread(() -> {
                String errorMsg = AuthRepository.getInstance(this).resetPassword(token, newPassword);

                runOnUiThread(() -> {
                    btnResetPassword.setEnabled(true);
                    if (errorMsg == null) {
                        Toast.makeText(this, "Password successfully reset! Please login.", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Reset failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        tvBackToLogin.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, LoginActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    private String extractTokenFromUrl(String url) {
        if (url == null) return null;
        try {
            Uri u = Uri.parse(url);
            String t = u.getQueryParameter("token");
            if (t != null && !t.isEmpty()) return t;
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

        TextView logo = toolbar.findViewById(R.id.tvAppLogo);
        if (logo != null) {
            logo.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        View btnRegister = toolbar.findViewById(R.id.btnRegisterToolbar);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }

        View btnLogin = toolbar.findViewById(R.id.btnLoginToolbar);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

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