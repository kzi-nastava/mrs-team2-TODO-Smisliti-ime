package com.example.getgo.fragments.admins;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.AdminApiService;
import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.utils.ToastHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChangePasswordFragment extends Fragment {

    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSave;

    private AdminApiService adminApiService;

    public AdminChangePasswordFragment() {}

    public static AdminChangePasswordFragment newInstance() {
        return new AdminChangePasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminApiService = ApiClient.getClient().create(AdminApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_change_password, container, false);

        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSave = view.findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> changePassword());

        return view;
    }

    private void changePassword() {
        String oldPassword = String.valueOf(etOldPassword.getText()).trim();
        String newPassword = String.valueOf(etNewPassword.getText()).trim();
        String confirmPassword = String.valueOf(etConfirmPassword.getText()).trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "New password and confirm password do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        UpdatePasswordDTO updatePasswordDTO = new UpdatePasswordDTO(oldPassword, newPassword, confirmPassword);

        adminApiService.updatePassword(updatePasswordDTO).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UpdatedPasswordDTO> call, @NonNull Response<UpdatedPasswordDTO> response) {
                btnSave.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else {
                    Toast.makeText(requireContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdatedPasswordDTO> call, @NonNull Throwable t) {
                btnSave.setEnabled(true);
                ToastHelper.showError(requireContext(), "Password change failed", t.getMessage());
            }
        });
    }

    private void clearFields() {
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
}