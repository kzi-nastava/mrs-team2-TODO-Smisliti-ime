package com.example.getgo.fragments.admins;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.AdminApiService;
import com.example.getgo.dtos.admin.GetAdminDTO;
import com.example.getgo.dtos.admin.UpdateAdminDTO;
import com.example.getgo.dtos.admin.UpdatedAdminDTO;
import com.example.getgo.utils.ToastHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileInfoFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private TextView tvChangePassword;
    private MaterialButton btnSave;

    private AdminApiService adminApiService;

    public AdminProfileInfoFragment() {}

    public static AdminProfileInfoFragment newInstance() {
        return new AdminProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminApiService = ApiClient.getClient().create(AdminApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile_info, container, false);

        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnSave = view.findViewById(R.id.btnSave);

        etEmail.setEnabled(false);

        loadUserData();

        tvChangePassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, AdminChangePasswordFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        adminApiService.getProfile().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetAdminDTO> call, @NonNull Response<GetAdminDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetAdminDTO admin = response.body();

                    etEmail.setText(admin.getEmail());
                    etFirstName.setText(admin.getName());
                    etLastName.setText(admin.getSurname());
                    etPhone.setText(admin.getPhone());
                    etAddress.setText(admin.getAddress());
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetAdminDTO> call, @NonNull Throwable t) {
                ToastHelper.showError(requireContext(), "Failed to load profile", t.getMessage());
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
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        UpdateAdminDTO updateDTO = new UpdateAdminDTO(firstName, lastName, phone, address);

        adminApiService.updateProfile(updateDTO).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UpdatedAdminDTO> call, @NonNull Response<UpdatedAdminDTO> response) {
                btnSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    loadUserData();
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdatedAdminDTO> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                ToastHelper.showError(requireContext(), "Failed to update profile", t.getMessage());
            }
        });
    }
}