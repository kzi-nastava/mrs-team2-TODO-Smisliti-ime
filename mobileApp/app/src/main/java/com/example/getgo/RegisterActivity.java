package com.example.getgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.example.getgo.api.ApiClient;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private Button btnRegister;
    private TextView tvLoginLink;
    private EditText etEmail, etPassword, etPasswordConfirm, etFirstName, etLastName, etAddress, etPhone;
    private ImageView avatar;
    private Uri selectedAvatarUri = null;

    private static final String PREFS_NAME = "getgo_prefs";
    private static final String PREF_BACKEND_URL = "backend_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_register);
        setupGuestToolbar();

        findViewById(android.R.id.content).setBackgroundResource(R.drawable.background);

        // Apply stored backend URL if present
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String backend = prefs.getString(PREF_BACKEND_URL, null);
        if (backend != null && !backend.isEmpty()) {
            ApiClient.setDefaultBaseUrl(backend);
            Log.d(TAG, "Using stored backend: " + backend);
        } else {
            Log.d(TAG, "Using default backend (ngrok)");
        }

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

        btnRegister.setEnabled(false);
        Log.d(TAG, "Starting registration for: " + email);
        new Thread(() -> {
            String error = AuthRepository.getInstance(this)
                    .registerUser(email, pass, firstName, lastName, address, phone, "PASSENGER", avatarUriString);

            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (error == null) {
                    Toast.makeText(this, "Registration created. Check email to activate account", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Registration successful for: " + email);
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Registration failed: " + error);
                }
            });
        }).start();
    }

    private void navigateToLogin() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedAvatarUri = data.getData();
            avatar.setImageURI(selectedAvatarUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        // Hide Register button (we're on register page)
        View btnRegister = toolbar.findViewById(R.id.btnRegisterToolbar);
        if (btnRegister != null) {
            btnRegister.setVisibility(View.GONE);
        }

        // Wire Login button
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