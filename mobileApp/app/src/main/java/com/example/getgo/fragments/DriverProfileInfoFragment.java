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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class DriverProfileInfoFragment extends Fragment {

    // Profile picture
    private ImageView ivProfilePicture;
    private MaterialCardView cvProfilePicture;
    private Uri selectedImageUri;

    // Image picker launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // Tab buttons
    private MaterialButton btnDriverTab, btnVehicleTab;

    // Sections
    private LinearLayout layoutDriverInfo, layoutVehicleInfo;
    private TextView tvRecentHours;

    // Driver info fields
    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;

    // Vehicle info fields
    private TextInputEditText etVehicleModel, etRegistrationNumber, etSeatNumber;
    private AutoCompleteTextView actvVehicleType;
    private MaterialCheckBox cbAllowPets, cbAllowBabies;

    // Common fields
    private TextView tvChangePassword;
    private MaterialButton btnSave;

    // Current tab state
    private boolean isDriverTabSelected = true;

    public DriverProfileInfoFragment() {
        // Required empty public constructor
    }

    public static DriverProfileInfoFragment newInstance() {
        return new DriverProfileInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_driver_profile_info, container, false);

        // Initialize profile picture
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        cvProfilePicture = view.findViewById(R.id.cvProfilePicture);

        // Initialize tab buttons
        btnDriverTab = view.findViewById(R.id.btnDriverTab);
        btnVehicleTab = view.findViewById(R.id.btnVehicleTab);

        // Initialize sections
        layoutDriverInfo = view.findViewById(R.id.layoutDriverInfo);
        layoutVehicleInfo = view.findViewById(R.id.layoutVehicleInfo);
        tvRecentHours = view.findViewById(R.id.tvRecentHours);

        // Initialize driver info fields
        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);

        // Initialize vehicle info fields
        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        actvVehicleType = view.findViewById(R.id.actvVehicleType);
        etRegistrationNumber = view.findViewById(R.id.etRegistrationNumber);
        etSeatNumber = view.findViewById(R.id.etSeatNumber);
        cbAllowPets = view.findViewById(R.id.cbAllowPets);
        cbAllowBabies = view.findViewById(R.id.cbAllowBabies);

        // Initialize common fields
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnSave = view.findViewById(R.id.btnSave);

        // Setup vehicle type dropdown
        setupVehicleTypeDropdown();

        // Load data
        loadDriverData();
        loadVehicleData();
        updateRecentHours();

        // Profile picture click listener
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());

        // Tab click listeners
        btnDriverTab.setOnClickListener(v -> switchToDriverTab());
        btnVehicleTab.setOnClickListener(v -> switchToVehicleTab());

        // Change password click listener
        tvChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Change password not implemented yet", Toast.LENGTH_SHORT).show();
        });

        // Save button click listener
        btnSave.setOnClickListener(v -> saveData());

        return view;
    }

    private void checkPermissionAndOpenPicker() {
        // For Android 13+ (API 33+), we use READ_MEDIA_IMAGES
        // For Android 11-12 (API 30-32), we use READ_EXTERNAL_STORAGE
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            openImagePicker();
        } else {
            // Request permission (will show system dialog)
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void switchToDriverTab() {
        if (!isDriverTabSelected) {
            isDriverTabSelected = true;

            // Update button colors
            btnDriverTab.setBackgroundTintList(getResources().getColorStateList(R.color.selected_blue, null));
            btnVehicleTab.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue, null));

            // Show/hide sections
            layoutDriverInfo.setVisibility(View.VISIBLE);
            layoutVehicleInfo.setVisibility(View.GONE);
            tvRecentHours.setVisibility(View.VISIBLE);
        }
    }

    private void switchToVehicleTab() {
        if (isDriverTabSelected) {
            isDriverTabSelected = false;

            // Update button colors
            btnDriverTab.setBackgroundTintList(getResources().getColorStateList(R.color.dark_blue, null));
            btnVehicleTab.setBackgroundTintList(getResources().getColorStateList(R.color.selected_blue, null));

            // Show/hide sections
            layoutDriverInfo.setVisibility(View.GONE);
            layoutVehicleInfo.setVisibility(View.VISIBLE);
            tvRecentHours.setVisibility(View.GONE);
        }
    }

    private void setupVehicleTypeDropdown() {
        // TODO: Get vehicle types from backend/service
        String[] vehicleTypes = getVehicleTypes();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );

        actvVehicleType.setAdapter(adapter);
    }

    private String[] getVehicleTypes() {
        // TODO: Replace with actual backend call
        return new String[]{"Sedan", "SUV", "Hatchback", "Van", "Luxury"};
    }

    @SuppressWarnings("SetTextI18n")
    private void loadDriverData() {
        // TODO: Load actual driver data from backend/database
        etEmail.setText("driver@getgo.com");
        etFirstName.setText("John");
        etLastName.setText("Smith");
        etPhone.setText("+381 11 123 4567");
        etAddress.setText("Belgrade, Serbia");
    }

    @SuppressWarnings("SetTextI18n")
    private void loadVehicleData() {
        // TODO: Load actual vehicle data from backend/database
        etVehicleModel.setText("Toyota Camry");
        actvVehicleType.setText("Sedan", false);
        etRegistrationNumber.setText("BG-1234-AB");
        etSeatNumber.setText("4");
        cbAllowPets.setChecked(true);
        cbAllowBabies.setChecked(false);
    }

    private void updateRecentHours() {
        // TODO: Get actual hours from backend/service
        int recentHours = getRecentActiveHours();
        tvRecentHours.setText(getString(R.string.recent_hours_format, recentHours));
    }

    private int getRecentActiveHours() {
        // TODO: Replace with actual backend call
        return 42; // Hardcoded for now
    }

    private void saveData() {
        if (isDriverTabSelected) {
            saveDriverData();
        } else {
            saveVehicleData();
        }
    }

    private void saveDriverData() {
        // Get values with null safety
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

        // TODO: Upload profile picture if selectedImageUri is not null
        // TODO: Send driver data to backend API
        Toast.makeText(requireContext(), "Driver profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveVehicleData() {
        // Get values with null safety
        String vehicleModel = Objects.requireNonNull(etVehicleModel.getText()).toString().trim();
        String vehicleType = actvVehicleType.getText().toString().trim();
        String registrationNumber = Objects.requireNonNull(etRegistrationNumber.getText()).toString().trim();
        String seatNumberStr = Objects.requireNonNull(etSeatNumber.getText()).toString().trim();
        boolean allowPets = cbAllowPets.isChecked();
        boolean allowBabies = cbAllowBabies.isChecked();

        // Basic validation
        if (vehicleModel.isEmpty() || vehicleType.isEmpty() ||
                registrationNumber.isEmpty() || seatNumberStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int seatNumber;
        try {
            seatNumber = Integer.parseInt(seatNumberStr);
            if (seatNumber <= 0) {
                Toast.makeText(requireContext(), "Seat number must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid seat number", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Upload profile picture if selectedImageUri is not null
        // TODO: Send vehicle data to backend API
        Toast.makeText(requireContext(), "Vehicle information updated successfully", Toast.LENGTH_SHORT).show();
    }
}