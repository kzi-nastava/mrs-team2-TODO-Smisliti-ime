package com.example.getgo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getgo.auth.AuthRepository;

public class ActivationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do NOT show the activation UI — activation happens instantly via the link.
        Intent intent = getIntent();
        Uri data = intent != null ? intent.getData() : null;

        String token = data != null ? data.getQueryParameter("token") : null;
        // fallback for some mail clients that put token in extras
        if ((token == null || token.trim().isEmpty()) && intent != null) {
            String extraToken = intent.getStringExtra("token");
            if (extraToken != null && !extraToken.trim().isEmpty()) token = extraToken;
        }

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Activation link is invalid (missing token).", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Call backend immediately on background thread, then finish
        String finalToken = token;
        new Thread(() -> {
            String err = AuthRepository.getInstance(this).activateByToken(finalToken);
            runOnUiThread(() -> {
                if (err == null) {
                    Toast.makeText(this, "Account activated. You can login now.", Toast.LENGTH_LONG).show();
                    // Open login screen
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    Toast.makeText(this, "Activation failed: " + err, Toast.LENGTH_LONG).show();
                }
                finish();
            });
        }).start();
    }
}
