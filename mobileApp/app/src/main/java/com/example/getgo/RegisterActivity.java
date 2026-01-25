package com.example.getgo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

import com.example.getgo.auth.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private TextView tvLoginLink;
    private EditText etEmail, etPassword, etPasswordConfirm, etFirstName, etLastName, etAddress, etPhone;
    private ImageView avatar;
    private Uri selectedAvatarUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_register);

        findViewById(android.R.id.content).setBackgroundResource(R.drawable.background);

        initializeViews();
        setupWindowInsets();
        setupListeners();

        avatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        avatar = findViewById(R.id.avatarRegister);
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
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword != null ? etPassword.getText().toString() : "";
        String pass2 = etPasswordConfirm != null ? etPasswordConfirm.getText().toString() : "";

        if (email.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
            Toast.makeText(this, "Please fill email and both password fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(pass2)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = etFirstName != null ? etFirstName.getText().toString().trim() : "";
        String lastName = etLastName != null ? etLastName.getText().toString().trim() : "";
        String address = etAddress != null ? etAddress.getText().toString().trim() : "";
        String phone = etPhone != null ? etPhone.getText().toString().trim() : "";

        String avatarUriString = selectedAvatarUri != null ? selectedAvatarUri.toString() : null;

        // Perform network registration off the UI thread
        btnRegister.setEnabled(false);
        new Thread(() -> {
            // registerUser now returns null on success, or error message
            String error = AuthRepository.getInstance(this)
                    .registerUser(email, pass, firstName, lastName, address, phone, "PASSENGER", avatarUriString);

            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (error == null) {
                    Toast.makeText(this, "Registration created. Check email to activate account", Toast.LENGTH_LONG).show();
                    finish(); // return to login
                } else {
                    // show server error returned by backend for debugging
                    Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void navigateToLogin() {
        finish(); // Close RegisterActivity to return to LoginActivity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedAvatarUri = data.getData();
            avatar.setImageURI(selectedAvatarUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}