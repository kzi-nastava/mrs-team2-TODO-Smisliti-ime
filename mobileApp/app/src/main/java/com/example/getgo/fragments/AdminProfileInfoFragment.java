package com.example.getgo.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminProfileInfoFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private TextView tvChangePassword;
    private MaterialButton btnSave;

    public AdminProfileInfoFragment() {}

    public static AdminProfileInfoFragment newInstance() {
        return new AdminProfileInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_profile_info, container, false);

        // Initialize views
        etEmail = view.findViewById(R.id.etEmail);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        btnSave = view.findViewById(R.id.btnSave);

        // Load existing user data
        loadUserData();

        // Set up click listeners
        tvChangePassword.setOnClickListener(v -> {
            // TODO: implement password reset
            Toast.makeText(requireContext(), "Change password not implemented yet", Toast.LENGTH_SHORT).show();
        });
        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        // TODO: Load actual user data from backend/database
        // For now, using placeholder data
        etEmail.setText("admin@getgo.com");
        etFirstName.setText("Admin");
        etLastName.setText("User");
        etPhone.setText("+381 11 123 4567");
        etAddress.setText("Belgrade, Serbia");
    }

    private void saveUserData() {
        // Get values from input fields
        String email = etEmail.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

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

        // TODO: Send data to backend API
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}