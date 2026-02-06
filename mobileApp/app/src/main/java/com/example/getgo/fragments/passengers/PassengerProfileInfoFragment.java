package com.example.getgo.fragments.passengers;

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
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.passenger.UpdatePassengerDTO;
import com.example.getgo.dtos.passenger.UpdatedPassengerDTO;
import com.example.getgo.dtos.user.UpdatedProfilePictureDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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

public class PassengerProfileInfoFragment extends Fragment {

    private ImageView ivProfilePicture;
    private MaterialCardView cvProfilePicture;
    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;
    private TextView tvChangePassword;
    private MaterialButton btnSave;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private PassengerApiService passengerApiService;

    public PassengerProfileInfoFragment() {}

    public static PassengerProfileInfoFragment newInstance() {
        return new PassengerProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        passengerApiService = ApiClient.getClient().create(PassengerApiService.class);

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

        etEmail.setEnabled(false);

        // Load existing user data
        loadUserData();

        // Setup listeners
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());
        tvChangePassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, PassengerChangePasswordFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });
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

    private void loadUserData() {
        passengerApiService.getProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetPassengerDTO> call, @NonNull Response<GetPassengerDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetPassengerDTO passenger = response.body();

                    etEmail.setText(passenger.getEmail());
                    etFirstName.setText(passenger.getName());
                    etLastName.setText(passenger.getSurname());
                    etPhone.setText(passenger.getPhone());
                    etAddress.setText(passenger.getAddress());

                    if (passenger.getProfilePictureUrl() != null && !passenger.getProfilePictureUrl().isEmpty()) {
                        String imageUrl = ApiClient.SERVER_URL + passenger.getProfilePictureUrl();
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
            public void onFailure(@NonNull Call<GetPassengerDTO> call, @NonNull Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void saveUserData() {
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

        UpdatePassengerDTO updateDTO = new UpdatePassengerDTO(firstName, lastName, phone, address);

        passengerApiService.updateProfile(updateDTO).enqueue(new Callback<UpdatedPassengerDTO>() {
            @Override
            public void onResponse(@NonNull Call<UpdatedPassengerDTO> call, @NonNull Response<UpdatedPassengerDTO> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    showToast("Profile updated successfully");
                    loadUserData();
                } else {
                    showToast("Failed to update profile");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdatedPassengerDTO> call, @NonNull Throwable t) {
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

            passengerApiService.uploadProfilePicture(body).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<UpdatedProfilePictureDTO> call, @NonNull Response<UpdatedProfilePictureDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        showToast("Profile picture updated successfully");
                        UpdatedProfilePictureDTO result = response.body();
                        if (result.getPictureUrl() != null) {
                            String imageUrl = ApiClient.SERVER_URL + result.getPictureUrl();
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.unregistered_profile)
                                    .error(R.drawable.unregistered_profile)
                                    .circleCrop()
                                    .into(ivProfilePicture);
                        }
                    } else {
                        showToast("Failed to update profile picture");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UpdatedProfilePictureDTO> call, @NonNull Throwable t) {
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