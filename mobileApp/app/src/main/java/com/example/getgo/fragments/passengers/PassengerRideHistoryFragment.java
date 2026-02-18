package com.example.getgo.fragments.passengers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.getgo.R;
import com.example.getgo.adapters.RideHistoryAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.getgo.utils.ToastHelper;

public class PassengerRideHistoryFragment extends Fragment implements SensorEventListener {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private TextView tvFilterDate;
    private MaterialButton btnReset, btnApply, btnNext, btnPrev;
    private Spinner spinnerSortField, spinnerSortDirection;

    private ArrayList<GetRideDTO> fullHistoryList;

    private int currentPage = 0;
    private int pageSize = 5;
    private int totalElements = 0;

    // Shake sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private boolean sortAscending = true;
    private String selectedDateForApi = "";
    private String sortField = "startTime";
    private String sortDirection = "DESC";

    public PassengerRideHistoryFragment() {}

    public static PassengerRideHistoryFragment newInstance() {
        return new PassengerRideHistoryFragment();
    }

    // navigation helper — call from Activity or toolbar handlers
    public static void navigateTo(FragmentActivity activity) {
        if (activity == null) return;
        try {
            PassengerRideHistoryFragment frag = PassengerRideHistoryFragment.newInstance();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, frag)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.w("PassengerRideHistory", "Failed to navigate: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_ride_history, container, false);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);
        btnNext = view.findViewById(R.id.btnNextPage);
        btnPrev = view.findViewById(R.id.btnPrevPage);
        tvFilterDate = view.findViewById(R.id.tvFilterDate);
        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);

        // Sorting spinners (passenger)
        spinnerSortField = view.findViewById(R.id.spinnerSortField);
        spinnerSortDirection = view.findViewById(R.id.spinnerSortDirection);
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
                loadPassengerRides();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] directions = {"DESC", "ASC"};
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, directions);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortDirection.setAdapter(directionAdapter);
        spinnerSortDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortDirection = directions[position];
                currentPage = 0;
                loadPassengerRides();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Page size spinner
        Spinner spinnerPageSize = view.findViewById(R.id.spinnerPageSize);
        Integer[] pageSizes = {5, 10, 20};
        ArrayAdapter<Integer> adapterSpinner = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_item_white, pageSizes);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapterSpinner);
        spinnerPageSize.setSelection(0);
        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageSize = pageSizes[position];
                currentPage = 0;
                loadPassengerRides();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fullHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(requireContext());
        rideHistoryLV.setAdapter(adapter);

        tvFilterDate.setOnClickListener(v -> showDatePicker());

        btnReset.setOnClickListener(v -> {
            tvFilterDate.setText(getString(R.string.filter_by_date));
            selectedDateForApi = "";
            currentPage = 0;
            loadPassengerRides();
        });

        btnApply.setOnClickListener(v -> {
            currentPage = 0;
            loadPassengerRides();
        });

        btnNext.setOnClickListener(v -> {
            if ((currentPage + 1) * pageSize < totalElements) {
                currentPage++;
                loadPassengerRides();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadPassengerRides();
            }
        });

        loadPassengerRides();

        adapter.setOnRideClickListener(ride -> {
            PassengerRideDetailFragment fragment = PassengerRideDetailFragment.newInstance(ride.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // use normalized g-force to be more robust across devices
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            final float SHAKE_THRESHOLD = 2.7f; // tuned threshold
            final long SHAKE_DEBOUNCE_MS = 1000L;

            if (gForce > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > SHAKE_DEBOUNCE_MS) {
                    lastShakeTime = currentTime;

                    // Toggle sort direction and force field to startTime
                    sortAscending = !sortAscending;
                    sortDirection = sortAscending ? "ASC" : "DESC";
                    sortField = "startTime"; // ensure we sort by startTime on shake
                    currentPage = 0;

                    // Update UI controls and trigger reload on main thread
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            // spinner indices: startTime = 0, directions {"DESC","ASC"}
                            try {
                                if (spinnerSortField != null) spinnerSortField.setSelection(0, true);
                                if (spinnerSortDirection != null) spinnerSortDirection.setSelection(sortAscending ? 1 : 0, true);
                            } catch (Exception e) {
                                Log.w("PassengerRideHistory", "Failed to update spinner selection: " + e.getMessage());
                            }

                            // Use existing helper which respects backend sorting
                            sortRidesByDate();

                            Toast.makeText(requireContext(),
                                    "Sorted by Start Time: " + (sortAscending ? "Ascending ▲" : "Descending ▼"),
                                    Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // fallback: just load data if fragment not attached
                        sortRidesByDate();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void sortRidesByDate() {
        // This method can be removed or simplified since backend now handles sorting
        sortDirection = sortAscending ? "ASC" : "DESC";
        loadPassengerRides();
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {

            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(new Date(selection));
            tvFilterDate.setText(displayDate);

            SimpleDateFormat apiFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDateForApi = apiFormat.format(new Date(selection));

            Log.d("PassengerRideHistory", "Date selected - Display: " + displayDate + ", API: " + selectedDateForApi);

            currentPage = 0;
            loadPassengerRides();
        });
    }

    private void loadPassengerRides() {
        String startDate = selectedDateForApi;

        Log.d("PassengerRideHistory", "Loading rides - startDate: '" + startDate +
              "', page: " + currentPage + ", size: " + pageSize +
              ", sort: " + sortField + ", dir: " + sortDirection);

        PassengerApiService passengerService = ApiClient.getClient().create(PassengerApiService.class);

        Call<PageResponse<GetRideDTO>> call = passengerService.getPassengerRides(
                currentPage,
                pageSize,
                startDate.isEmpty() ? null : startDate,
                sortField,
                sortDirection
        );

        call.enqueue(new Callback<PageResponse<GetRideDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<GetRideDTO>> call, Response<PageResponse<GetRideDTO>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<GetRideDTO> page = response.body();
                    totalElements = page.getTotalElements();
                    adapter.setRides(new ArrayList<>(page.getContent()));

                    Log.d("PassengerRideHistory", "Loaded " + page.getContent().size() + " rides, total: " + totalElements);
                } else {
                    Log.w("PassengerRideHistory", "Failed to load rides: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.w("PassengerRideHistory", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("PassengerRideHistory", "Error reading error body", e);
                    }
                    adapter.setRides(Collections.emptyList());
                    totalElements = 0;
                }
            }

            @Override
            public void onFailure(Call<PageResponse<GetRideDTO>> call, Throwable t) {
                if (!isAdded()) return;
                Log.e("PassengerRideHistory", "API call failed", t);
                adapter.setRides(Collections.emptyList());
                totalElements = 0;
                ToastHelper.showError(requireContext(), "Failed to load ride history", t.getMessage());
            }
        });
    }
}
