package com.example.getgo.fragments.drivers;

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
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.request.CreatedDriverChangeRequestDTO;
import com.example.getgo.dtos.request.UpdateDriverPersonalDTO;
import com.example.getgo.dtos.request.UpdateDriverVehicleDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private DriverApiService driverApiService;

    public DriverProfileInfoFragment() {}

    public static DriverProfileInfoFragment newInstance() {
        return new DriverProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        driverApiService = ApiClient.getClient().create(DriverApiService.class);

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivProfilePicture.setImageURI(selectedImageUri);
                        uploadProfilePicture();
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
                        showToast("Permission denied. Cannot select image.");
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

        etEmail.setEnabled(false);

        // Get data from backend
        loadVehicleTypeDropdown();
        loadDriverData();

        // Set listeners
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());
        btnDriverTab.setOnClickListener(v -> showDriverTab());
        btnVehicleTab.setOnClickListener(v -> showVehicleTab());
//        tvChangePassword.setOnClickListener(v -> {
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.fragmentContainer, DriverChangePasswordFragment.newInstance())
//                    .addToBackStack(null)
//                    .commit();
//        });
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
        String[] vehicleTypes = new String[]{"STANDARD", "LUXURY", "VAN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );
        actvVehicleType.setAdapter(adapter);
    }

    private void loadDriverData() {
        driverApiService.getProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetDriverDTO> call, @NonNull Response<GetDriverDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetDriverDTO driver = response.body();

                    etEmail.setText(driver.getEmail());
                    etFirstName.setText(driver.getName());
                    etLastName.setText(driver.getSurname());
                    etPhone.setText(driver.getPhone());
                    etAddress.setText(driver.getAddress());

                    etVehicleModel.setText(driver.getVehicleModel());
                    actvVehicleType.setText(driver.getVehicleType(), false);
                    etRegistrationNumber.setText(driver.getVehicleLicensePlate());
                    etSeatNumber.setText(String.valueOf(driver.getVehicleSeats()));
                    cbAllowPets.setChecked(driver.getVehicleAllowsPets());
                    cbAllowBabies.setChecked(driver.getVehicleHasBabySeats());

                    tvRecentHours.setText(getString(R.string.recent_hours_format, driver.getRecentHoursWorked()));

                    // Profile picture
                    if (driver.getProfilePictureUrl() != null && !driver.getProfilePictureUrl().isEmpty()) {
                        String imageUrl = ApiClient.SERVER_URL + driver.getProfilePictureUrl();
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.unregistered_profile)
                                .error(R.drawable.unregistered_profile)
                                .circleCrop()
                                .into(ivProfilePicture);
                    }
                } else {
                    showToast("Failed to load profile");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetDriverDTO> call, @NonNull Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void saveData() {
        if (isDriverTabSelected) {
            saveDriverData();
        } else {
            saveVehicleData();
        }
    }

    private void saveDriverData() {
        String email = String.valueOf(etEmail.getText()).trim();
        String firstName = String.valueOf(etFirstName.getText()).trim();
        String lastName = String.valueOf(etLastName.getText()).trim();
        String phone = String.valueOf(etPhone.getText()).trim();
        String address = String.valueOf(etAddress.getText()).trim();

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                phone.isEmpty() || address.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email");
            return;
        }

        btnSave.setEnabled(false);

        UpdateDriverPersonalDTO updateDTO = new UpdateDriverPersonalDTO(firstName, lastName, phone, address);

        driverApiService.requestPersonalInfoChange(updateDTO).enqueue(new Callback<CreatedDriverChangeRequestDTO>() {
            @Override
            public void onResponse(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Response<CreatedDriverChangeRequestDTO> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Change request submitted successfully");
                } else {
                    showToast("Failed to submit change request");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void saveVehicleData() {
        String vehicleModel = String.valueOf(etVehicleModel.getText()).trim();
        String vehicleType = actvVehicleType.getText().toString().trim();
        String registrationNumber = String.valueOf(etRegistrationNumber.getText()).trim();
        String seatNumberStr = String.valueOf(etSeatNumber.getText()).trim();
        boolean allowPets = cbAllowPets.isChecked();
        boolean allowBabies = cbAllowBabies.isChecked();

        if (vehicleModel.isEmpty() || vehicleType.isEmpty() ||
                registrationNumber.isEmpty() || seatNumberStr.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        int seatNumber;
        try {
            seatNumber = Integer.parseInt(seatNumberStr);
            if (seatNumber <= 0) {
                showToast("Seat number must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            showToast("Invalid seat number");
            return;
        }

        btnSave.setEnabled(false);

        UpdateDriverVehicleDTO updateDTO = new UpdateDriverVehicleDTO(
                vehicleModel, vehicleType, registrationNumber, seatNumber, allowPets, allowBabies
        );

        driverApiService.requestVehicleInfoChange(updateDTO).enqueue(new Callback<CreatedDriverChangeRequestDTO>() {
            @Override
            public void onResponse(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Response<CreatedDriverChangeRequestDTO> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Change request submitted successfully");
                } else {
                    showToast("Failed to submit change request");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void uploadProfilePicture() {
        if (selectedImageUri == null) {
            showToast("No image selected");
            return;
        }

        try {
            File file = convertUriToFile(selectedImageUri);
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            driverApiService.requestProfilePictureChange(body).enqueue(new Callback<CreatedDriverChangeRequestDTO>() {
                @Override
                public void onResponse(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Response<CreatedDriverChangeRequestDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        showToast("Profile picture change request submitted successfully");
                    } else {
                        showToast("Failed to submit picture change request");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CreatedDriverChangeRequestDTO> call, @NonNull Throwable t) {
                    showToast("Error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            showToast("Error uploading image: " + e.getMessage());
        }
    }

    private File convertUriToFile(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Failed to open input stream");
        }

        File file = new File(requireContext().getCacheDir(), "profile_picture.jpg");
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return file;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}