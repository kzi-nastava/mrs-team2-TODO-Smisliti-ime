package com.example.getgo.fragments.passengers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.passenger.UpdatePassengerDTO;
import com.example.getgo.dtos.passenger.UpdatedPassengerDTO;
import com.example.getgo.dtos.user.UpdatedProfilePictureDTO;
import com.example.getgo.repositories.PassengerRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PassengerProfileInfoFragment extends Fragment {

    private ImageView ivProfilePicture;
    private MaterialCardView cvProfilePicture;
    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;
    private TextView tvChangePassword;
    private MaterialButton btnSave;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private PassengerRepository passengerRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    public PassengerProfileInfoFragment() {}

    public static PassengerProfileInfoFragment newInstance() {
        return new PassengerProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        passengerRepository = PassengerRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

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

        initializeViews(view);
        setupListeners();
        loadUserData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void initializeViews(View view) {
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
    }

    private void setupListeners() {
        cvProfilePicture.setOnClickListener(v -> checkPermissionAndOpenPicker());
        tvChangePassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, PassengerChangePasswordFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });
        btnSave.setOnClickListener(v -> saveUserData());
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
        executor.execute(() -> {
            try {
                GetPassengerDTO passenger = passengerRepository.getProfile();

                mainHandler.post(() -> {
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
                });
            } catch (Exception e) {
                mainHandler.post(() -> showToast("Failed to load profile: " + e.getMessage()));
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

        executor.execute(() -> {
            try {
                UpdatedPassengerDTO result = passengerRepository.updateProfile(updateDTO);

                mainHandler.post(() -> {
                    btnSave.setEnabled(true);
                    showToast("Profile updated successfully");
                    loadUserData();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnSave.setEnabled(true);
                    showToast("Failed to update profile: " + e.getMessage());
                });
            }
        });
    }

    private void uploadProfilePicture() {
        if (selectedImageUri == null) {
            showToast("No image selected");
            return;
        }

        executor.execute(() -> {
            try {
                File file = convertUriToFile(selectedImageUri);
                UpdatedProfilePictureDTO result = passengerRepository.uploadProfilePicture(file);

                mainHandler.post(() -> {
                    showToast("Profile picture updated successfully");
                    if (result.getPictureUrl() != null) {
                        String imageUrl = ApiClient.SERVER_URL + result.getPictureUrl();
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.unregistered_profile)
                                .error(R.drawable.unregistered_profile)
                                .circleCrop()
                                .into(ivProfilePicture);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> showToast("Failed to upload profile picture: " + e.getMessage()));
            }
        });
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