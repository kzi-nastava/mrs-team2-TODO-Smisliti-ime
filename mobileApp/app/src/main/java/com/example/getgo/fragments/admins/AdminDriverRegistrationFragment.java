package com.example.getgo.fragments.admins;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.dtos.driver.CreateDriverDTO;
import com.example.getgo.dtos.driver.CreatedDriverDTO;
import com.example.getgo.repositories.AdminRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDriverRegistrationFragment extends Fragment {
    private LinearLayout layoutDriverForm, layoutVehicleForm;

    private TextInputEditText etEmail, etFirstName, etLastName, etPhone, etAddress;
    private TextInputEditText etVehicleModel, etVehicleType, etLicensePlate, etSeats;
    private MaterialCheckBox cbAllowsBabies, cbAllowsPets;

    private MaterialButton btnNext, btnBack, btnRegister;

    private AdminRepository adminRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    public AdminDriverRegistrationFragment() {}

    public static AdminDriverRegistrationFragment newInstance() {
        return new AdminDriverRegistrationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminRepository = AdminRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_driver_registration, container, false);

        initializeViews(view);
        setupListeners();
        showDriverForm();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void initializeViews(View view) {
        layoutDriverForm = view.findViewById(R.id.layoutDriverForm);
        layoutVehicleForm = view.findViewById(R.id.layoutVehicleForm);

        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);

        etVehicleModel = view.findViewById(R.id.etVehicleModel);
        etVehicleType = view.findViewById(R.id.etVehicleType);
        etLicensePlate = view.findViewById(R.id.etLicensePlate);
        etSeats = view.findViewById(R.id.etSeats);
        cbAllowsBabies = view.findViewById(R.id.cbAllowsBabies);
        cbAllowsPets = view.findViewById(R.id.cbAllowsPets);

        btnNext = view.findViewById(R.id.btnNext);
        btnBack = view.findViewById(R.id.btnBack);
        btnRegister = view.findViewById(R.id.btnRegister);
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> goToVehicle());
        btnBack.setOnClickListener(v -> showDriverForm());
        btnRegister.setOnClickListener(v -> onRegister());
    }

    private void showDriverForm() {
        layoutDriverForm.setVisibility(View.VISIBLE);
        layoutVehicleForm.setVisibility(View.GONE);
    }

    private void showVehicleForm() {
        layoutDriverForm.setVisibility(View.GONE);
        layoutVehicleForm.setVisibility(View.VISIBLE);
    }

    private void goToVehicle() {
        String email = getTextSafely(etEmail);
        String firstName = getTextSafely(etFirstName);
        String lastName = getTextSafely(etLastName);

        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showToast("Please fill in all required driver fields");
            return;
        }

        showVehicleForm();
    }

    private void onRegister() {
        int seats;
        try {
            seats = Integer.parseInt(getTextSafely(etSeats));
        } catch (NumberFormatException e) {
            showToast("Invalid number of seats");
            return;
        }

        CreateDriverDTO dto = buildCreateDriverDTO(seats);

        btnRegister.setEnabled(false);

        executor.execute(() -> {
            try {
                CreatedDriverDTO response = adminRepository.registerDriver(dto);
                mainHandler.post(() -> {
                    btnRegister.setEnabled(true);
                    showToast("Driver registered! Activation email sent to " + response.getEmail());
                    resetForms();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnRegister.setEnabled(true);
                    showToast("Failed to register driver: " + e.getMessage());
                });
            }
        });
    }

    private CreateDriverDTO buildCreateDriverDTO(int seats) {
        return new CreateDriverDTO(
                getTextSafely(etEmail),
                getTextSafely(etFirstName),
                getTextSafely(etLastName),
                getTextSafely(etPhone),
                getTextSafely(etAddress),
                getTextSafely(etVehicleModel),
                getTextSafely(etVehicleType),
                getTextSafely(etLicensePlate),
                seats,
                cbAllowsBabies.isChecked(),
                cbAllowsPets.isChecked()
        );
    }

    private void resetForms() {
        etEmail.setText("");
        etFirstName.setText("");
        etLastName.setText("");
        etPhone.setText("");
        etAddress.setText("");
        etVehicleModel.setText("");
        etVehicleType.setText("");
        etLicensePlate.setText("");
        etSeats.setText("");
        cbAllowsBabies.setChecked(false);
        cbAllowsPets.setChecked(false);
        showDriverForm();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getTextSafely(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}