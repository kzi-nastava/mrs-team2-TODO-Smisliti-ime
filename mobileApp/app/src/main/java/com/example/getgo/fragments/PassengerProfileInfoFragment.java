package com.example.getgo.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class PassengerProfileInfoFragment extends Fragment {

    private ImageView ivProfilePicture;
    private MaterialCardView cvProfilePicture;
    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;
    private TextView tvChangePassword;
    private MaterialButton btnSave;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public PassengerProfileInfoFragment() {}

    public static PassengerProfileInfoFragment newInstance() {
        return new PassengerProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivProfilePicture.setImageURI(selectedImageUri);
                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(requireContext(), "Permission denied. Cannot select image.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_passenger_profile_info, container, false);

        // Initialize views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        cvProfilePicture = view.findViewById(R.id.cvProfilePicture);
        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnSave = view.findViewById(R.id.btnSave);

        // Load existing user data
        loadUserData();

        // Setup listeners
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());
        tvChangePassword.setOnClickListener(v -> Toast.makeText(requireContext(),
                "Change password not implemented yet", Toast.LENGTH_SHORT).show());
        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void checkPermissionAndOpenPicker() {
        String permission;

        if (Build.VERSION.SDK_INT >= 33) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
        );
        imagePickerLauncher.launch(intent);
    }

    @SuppressWarnings("SetTextI18n")
    private void loadUserData() {
        // TODO: Load actual data from backend
        etEmail.setText("passenger@getgo.com");
        etFirstName.setText("John");
        etLastName.setText("Doe");
        etPhone.setText("+381 11 123 4567");
        etAddress.setText("Belgrade, Serbia");
    }

    private void saveUserData() {
        // Get values from input fields with null safety
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String firstName = Objects.requireNonNull(etFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(etLastName.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String address = Objects.requireNonNull(etAddress.getText()).toString().trim();

        // Basic validation
        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Connect to backend
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}