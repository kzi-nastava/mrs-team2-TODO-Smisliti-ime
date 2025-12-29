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

    // Elements
    private ImageView ivProfilePicture;
    private MaterialCardView cvProfilePicture;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private MaterialButton btnDriverTab, btnVehicleTab;
    private LinearLayout layoutDriverInfo, layoutVehicleInfo;
    private TextView tvRecentHours;
    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;
    private TextInputEditText etVehicleModel, etRegistrationNumber, etSeatNumber;
    private AutoCompleteTextView actvVehicleType;
    private MaterialCheckBox cbAllowPets, cbAllowBabies;
    private TextView tvChangePassword;
    private MaterialButton btnSave;

    // States
    private boolean isDriverTabSelected = true;

    public DriverProfileInfoFragment() {}

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

        // Get data from backend
        loadVehicleTypeDropdown();
        loadDriverData();
        loadVehicleData();
        loadRecentHoursWorked();

        // Set listeners
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());
        btnDriverTab.setOnClickListener(v -> showDriverTab());
        btnVehicleTab.setOnClickListener(v -> showVehicleTab());
        tvChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Change password not implemented yet", Toast.LENGTH_SHORT).show();
        });
        btnSave.setOnClickListener(v -> saveData());

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

    private void showDriverTab() {
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

    private void showVehicleTab() {
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

    private void loadVehicleTypeDropdown() {
        // TODO: Get vehicle types from backend
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
        // TODO: Load actual data from backend
        etEmail.setText("driver@getgo.com");
        etFirstName.setText("John");
        etLastName.setText("Smith");
        etPhone.setText("+381 11 123 4567");
        etAddress.setText("Belgrade, Serbia");
    }

    @SuppressWarnings("SetTextI18n")
    private void loadVehicleData() {
        // TODO: Load actual data from backend
        etVehicleModel.setText("Toyota Camry");
        actvVehicleType.setText("Sedan", false);
        etRegistrationNumber.setText("BG-1234-AB");
        etSeatNumber.setText("4");
        cbAllowPets.setChecked(true);
        cbAllowBabies.setChecked(false);
    }

    private void loadRecentHoursWorked() {
        // TODO: Load actual data from backend
        int recentHours = getRecentActiveHours();
        tvRecentHours.setText(getString(R.string.recent_hours_format, recentHours));
    }

    private int getRecentActiveHours() {
        // TODO: Load actual data from backend
        return 42;
    }

    private void saveData() {
        if (isDriverTabSelected) {
            saveDriverData();
        } else {
            saveVehicleData();
        }
    }

    private void saveDriverData() {
        // Get values
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
        Toast.makeText(requireContext(), "Driver profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void saveVehicleData() {
        // Get values
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

        // TODO: Connect to backend
        Toast.makeText(requireContext(), "Vehicle information updated successfully", Toast.LENGTH_SHORT).show();
    }
}