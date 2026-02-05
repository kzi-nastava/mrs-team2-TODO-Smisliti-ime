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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideHistoryFragment extends Fragment implements SensorEventListener {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private TextView tvFilterDate;
    private ArrayList<GetRideDTO> fullHistoryList;
    private MaterialButton btnReset, btnApply, btnNext, btnPrev;

    private int currentPage = 0;
    private int pageSize = 5;
    private int totalElements = 0;

    // Shake sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private boolean sortAscending = true;
    private String selectedDateForApi = "";

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
        View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);
        btnNext = view.findViewById(R.id.btnNextPage);
        btnPrev = view.findViewById(R.id.btnPrevPage);
        tvFilterDate = view.findViewById(R.id.tvFilterDate);
        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);

        Spinner spinnerPageSize = view.findViewById(R.id.spinnerPageSize);
        Integer[] pageSizes = {5, 10, 20};
        ArrayAdapter<Integer> adapterSpinner = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, pageSizes);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapterSpinner);
        spinnerPageSize.setSelection(0);

        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageSize = pageSizes[position];
                currentPage = 0;
                loadRideHistoryFromServer();
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
            selectedDateForApi = ""; // Reset API datum
            currentPage = 0;
            loadRideHistoryFromServer();
        });

        btnApply.setOnClickListener(v -> {
            currentPage = 0;
            loadRideHistoryFromServer();
        });

        btnNext.setOnClickListener(v -> {
            if ((currentPage + 1) * pageSize < totalElements) {
                currentPage++;
                loadRideHistoryFromServer();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadRideHistoryFromServer();
            }
        });

        loadRideHistoryFromServer();

        adapter.setOnRideClickListener(ride -> {
            PassengerRideDetailFragment fragment = PassengerRideDetailFragment.newInstance(ride);
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

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > 12) { // Shake threshold
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 1000) { // debounce 1s
                    lastShakeTime = currentTime;
                    sortAscending = !sortAscending;
                    sortRidesByDate();
                    Toast.makeText(requireContext(),
                            "Sorted by date: " + (sortAscending ? "Oldest first" : "Newest first"),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void sortRidesByDate() {
        Collections.sort(fullHistoryList, new Comparator<GetRideDTO>() {
            @Override
            public int compare(GetRideDTO r1, GetRideDTO r2) {
                if (r1.getStartingTime() == null || r2.getStartingTime() == null) return 0;
                int cmp = r1.getStartingTime().compareTo(r2.getStartingTime());
                return sortAscending ? cmp : -cmp;
            }
        });
        adapter.setRides(fullHistoryList);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build();
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Format za prikaz korisniku
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(new Date(selection));
            tvFilterDate.setText(displayDate);

            // Format za API - backend očekuje dd-MM-yyyy
            SimpleDateFormat apiFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDateForApi = apiFormat.format(new Date(selection));

            Log.d("PassengerRideHistory", "Date selected - Display: " + displayDate + ", API: " + selectedDateForApi);
        });
    }

    private void loadRideHistoryFromServer() {
        PassengerApiService passengerApiService = ApiClient.getClient().create(PassengerApiService.class);

        // Koristi sačuvani datum za API
        String startDate = selectedDateForApi;

        Log.d("PassengerRideHistory", "Loading rides - page: " + currentPage + ", size: " + pageSize + ", startDate: " + startDate);

        passengerApiService.getPassengerRides(currentPage, pageSize, startDate).enqueue(new Callback<PageResponse<GetRideDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<GetRideDTO>> call, Response<PageResponse<GetRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<GetRideDTO> pageResponse = response.body();
                    List<GetRideDTO> ridesFromServer = pageResponse.getContent();
                    totalElements = pageResponse.getTotalElements();
                    fullHistoryList = new ArrayList<>(ridesFromServer);
                    adapter.setRides(ridesFromServer);
                    Log.d("PassengerRideHistory", "Loaded " + ridesFromServer.size() + " rides, total: " + totalElements);
                } else {
                    Log.d("PassengerRideHistory", "Response failed: " + response.code());
                    try {
                        Log.d("PassengerRideHistory", "Error: " + response.errorBody().string());
                    } catch (Exception e) {}
                }
            }

            @Override
            public void onFailure(Call<PageResponse<GetRideDTO>> call, Throwable t) {
                Log.d("PassengerRideHistory", "Failed to load rides: " + t.getMessage());
            }
        });
    }
}
