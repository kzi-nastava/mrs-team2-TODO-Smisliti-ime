package com.example.getgo.fragments.admins;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.getgo.R;
import com.example.getgo.adapters.AdminRideHistoryAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.AdminApiService;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideHistoryFragment extends Fragment {

    private TextInputEditText etEmail;
    private Spinner spinnerRole;
    private TextView tvFilterDate;
    private ListView rideHistoryLV;
    private AdminRideHistoryAdapter adapter;
    private MaterialButton btnApply, btnReset, btnNext, btnPrev;

    private int currentPage = 0;
    private int pageSize = 10;
    private int totalElements = 0;
    private String selectedDateForApi = "";
    private String sortField = "startTime";
    private String sortDirection = "DESC";

    public static AdminRideHistoryFragment newInstance() { return new AdminRideHistoryFragment(); }

    public static void navigateTo(FragmentActivity activity) {
        if (activity == null) return;
        try {
            AdminRideHistoryFragment frag = AdminRideHistoryFragment.newInstance();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, frag)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.w("AdminRideHistory", "Failed to navigate: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_ride_history, container, false);

        etEmail = v.findViewById(R.id.etEmail);
        spinnerRole = v.findViewById(R.id.spinnerRole);
        tvFilterDate = v.findViewById(R.id.tvFilterDate);
        rideHistoryLV = v.findViewById(R.id.rideHistoryListView);
        btnApply = v.findViewById(R.id.btnApply);
        btnReset = v.findViewById(R.id.btnReset);
        btnNext = v.findViewById(R.id.btnNextPage);
        btnPrev = v.findViewById(R.id.btnPrevPage);

        // Role spinner
        String[] roles = {"Passenger", "Driver"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Page size spinner
        Spinner spinnerPageSize = v.findViewById(R.id.spinnerPageSize);
        Integer[] sizes = {5, 10, 20};
        ArrayAdapter<Integer> pageAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sizes);
        pageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(pageAdapter);
        spinnerPageSize.setSelection(1);
        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageSize = sizes[position];
                currentPage = 0;
                loadAdminRides();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sort field spinner
        Spinner spinnerSortField = v.findViewById(R.id.spinnerSortField);
        String[] sortFields = {"Start Time", "Price", "Distance", "Duration"};
        String[] sortFieldsApi = {"startTime", "estimatedPrice", "estDistanceKm", "estTime"};
        ArrayAdapter<String> sortFieldAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, sortFields);
        sortFieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortField.setAdapter(sortFieldAdapter);
        spinnerSortField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortField = sortFieldsApi[position];
                currentPage = 0;
                loadAdminRides();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sort direction spinner
        Spinner spinnerSortDirection = v.findViewById(R.id.spinnerSortDirection);
        String[] directions = {"DESC", "ASC"};
        String[] directionsApi = {"DESC", "ASC"};
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, directions);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortDirection.setAdapter(directionAdapter);
        spinnerSortDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortDirection = directionsApi[position];
                currentPage = 0;
                loadAdminRides();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        adapter = new AdminRideHistoryAdapter(requireContext());
        rideHistoryLV.setAdapter(adapter);
        rideHistoryLV.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        tvFilterDate.setOnClickListener(v1 -> showDatePicker());

        btnApply.setOnClickListener(v12 -> {
            currentPage = 0;
            loadAdminRides();
        });

        btnReset.setOnClickListener(v13 -> {
            tvFilterDate.setText(getString(R.string.filter_by_date));
            selectedDateForApi = ""; // Reset API datum
            etEmail.setText("");
            currentPage = 0;
            loadAdminRides();
        });

        btnNext.setOnClickListener(v14 -> {
            if ((currentPage + 1) * pageSize < totalElements) {
                currentPage++;
                loadAdminRides();
            }
        });

        btnPrev.setOnClickListener(v15 -> {
            if (currentPage > 0) {
                currentPage--;
                loadAdminRides();
            }
        });

        // ITEM CLICK LISTENER
        rideHistoryLV.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("AdminRideHistory", "=== ITEM CLICKED at position: " + position + " ===");

            GetRideDTO item = adapter.getItem(position);
            if (item == null) {
                Toast.makeText(requireContext(), "No ride data", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AdminRideHistory", "Ride ID: " + item.getId());

            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an email first", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedRole = spinnerRole.getSelectedItem().toString();
            String roleForApi = "Driver".equals(selectedRole) ? "DRIVER" : "PASSENGER";

            Log.d("AdminRideHistory", "Opening detail: rideId=" + item.getId() + ", email=" + email + ", role=" + roleForApi);

            AdminRideDetailFragment fragment = AdminRideDetailFragment.newInstance(item.getId(), email, roleForApi);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack("admin_ride_detail")
                    .commit();
        });

        loadAdminRides();
        return v;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();
        picker.show(getParentFragmentManager(), "ADMIN_DATE_PICKER");
        picker.addOnPositiveButtonClickListener(selection -> {
            // Format za prikaz korisniku
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(new Date(selection));
            tvFilterDate.setText(displayDate);

            // Format za API - backend očekuje dd-MM-yyyy
            SimpleDateFormat apiFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDateForApi = apiFormat.format(new Date(selection));

            Log.d("AdminRideHistory", "Date selected - Display: " + displayDate + ", API: " + selectedDateForApi);
        });
    }

    private void loadAdminRides() {
        String email = etEmail.getText().toString().trim();

        String startDate = selectedDateForApi;

        Log.d("AdminRideHistory", "Loading rides - email: " + email + ", startDate: '" + startDate + "', page: " + currentPage);

        AdminApiService svc = ApiClient.getClient().create(AdminApiService.class);

        if (TextUtils.isEmpty(email)) {
            adapter.setRides(Collections.emptyList());
            totalElements = 0;
            return;
        }

        String selectedRole = spinnerRole.getSelectedItem().toString();
        Call<PageResponse<GetRideDTO>> call;

        if ("Driver".equals(selectedRole)) {
            call = svc.getDriverRides(email, currentPage, pageSize,
                    startDate.isEmpty() ? null : startDate, sortField, sortDirection);
        } else {
            call = svc.getPassengerRides(email, currentPage, pageSize,
                    startDate.isEmpty() ? null : startDate, sortField, sortDirection);
        }

        call.enqueue(new Callback<PageResponse<GetRideDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<GetRideDTO>> call, Response<PageResponse<GetRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<GetRideDTO> page = response.body();
                    totalElements = page.getTotalElements();
                    adapter.setRides(new ArrayList<>(page.getContent()));
                    Log.d("AdminRideHistory", "Loaded " + page.getContent().size() + " rides, total: " + totalElements);
                } else {
                    Log.w("AdminRideHistory", "Failed: " + response.code());
                    try {
                        Log.w("AdminRideHistory", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {}
                    adapter.setRides(Collections.emptyList());
                    totalElements = 0;
                }
            }

            @Override
            public void onFailure(Call<PageResponse<GetRideDTO>> call, Throwable t) {
                Log.e("AdminRideHistory", "Error: " + t.getMessage());
                adapter.setRides(Collections.emptyList());
                totalElements = 0;
            }
        });
    }
}
