package com.example.getgo.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.R;
import com.example.getgo.repositories.AuthRepository;

public class ActivationActivity extends AppCompatActivity {

    private static final String TAG = "ActivationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_activation);
        setupGuestToolbar();

        // Extract token from deep link
        Intent intent = getIntent();
        Uri data = intent != null ? intent.getData() : null;
        String token = data != null ? data.getQueryParameter("token") : null;
        if ((token == null || token.trim().isEmpty()) && intent != null) {
            String extraToken = intent.getStringExtra("token");
            if (extraToken != null && !extraToken.trim().isEmpty()) token = extraToken;
        }
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Activation link is invalid (missing token).", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Optional: if email is present, show it
        String email = data != null ? data.getQueryParameter("email") : null;
        TextView tvEmail = findViewById(R.id.tvActivationEmail);
        if (tvEmail != null) {
            tvEmail.setText(email != null && !email.isEmpty() ? email : "Unknown");
        }

        ProgressBar progress = findViewById(R.id.pbActivating);
        TextView tvStatus = findViewById(R.id.tvActivatingStatus);
        if (progress != null) progress.setVisibility(View.VISIBLE);
        if (tvStatus != null) tvStatus.setText("Activating...");

        final long start = SystemClock.elapsedRealtime();
        final String finalToken = token;

        new Thread(() -> {
            String err = AuthRepository.getInstance(this).activateByToken(finalToken);
            long elapsed = SystemClock.elapsedRealtime() - start;
            long remaining = Math.max(0, 3000 - elapsed); // ensure at least 3s display
            SystemClock.sleep(remaining);

            runOnUiThread(() -> {
                if (err == null) {
                    Toast.makeText(this, "Account activated. You can login now.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Activation failed: " + err, Toast.LENGTH_LONG).show();
                }
                finish();
            });
        }).start();
    }

    private void setupGuestToolbar() {
        View includeRoot = findViewById(R.id.guest_toolbar);
        androidx.appcompat.widget.Toolbar toolbar = null;
        if (includeRoot instanceof androidx.appcompat.widget.Toolbar) {
            toolbar = (androidx.appcompat.widget.Toolbar) includeRoot;
        } else if (includeRoot != null) {
            toolbar = includeRoot.findViewById(R.id.toolbar);
        }
        if (toolbar == null) {
            Log.w(TAG, "Toolbar not found on activation page");
            return;
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }

        // Logo navigates home (guest)
        TextView logo = toolbar.findViewById(R.id.tvAppLogo);
        if (logo != null) {
            logo.setClickable(true);
            logo.setOnClickListener(v -> {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // Hide auth buttons on this page
        View btnRegister = toolbar.findViewById(R.id.btnRegisterToolbar);
        if (btnRegister != null) btnRegister.setVisibility(View.GONE);
        View btnLogin = toolbar.findViewById(R.id.btnLoginToolbar);
        if (btnLogin != null) btnLogin.setVisibility(View.GONE);

        // Apply status bar padding
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
