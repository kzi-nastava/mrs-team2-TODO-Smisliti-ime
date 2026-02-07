package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerChangePasswordFragment extends Fragment {
    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnSave;

    private PassengerApiService passengerApiService;

    public PassengerChangePasswordFragment() {}

    public static PassengerChangePasswordFragment newInstance() {
        return new PassengerChangePasswordFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passengerApiService = ApiClient.getClient().create(PassengerApiService.class);
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

        passengerApiService.updatePassword(updatePasswordDTO).enqueue(new Callback<>() {
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
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        etOldPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }

}
