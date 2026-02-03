package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.getgo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class PassengerHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextInputEditText etStartPoint, etDestination;
    private LinearLayout layoutWaypoints, layoutFriendEmailsList, layoutScheduledTime, layoutFriendEmails;
    private AutoCompleteTextView actvVehicleType, actvOrderTiming, actvTravelOption;
    private CheckBox cbHasBaby, cbHasPets;
    private TextInputEditText etScheduledTime;
    private Button btnOrderRide, btnCancel, btnAddWaypoint, btnRemoveWaypoint, btnAddFriendEmail, btnRemoveFriendEmail;

    private List<TextInputEditText> waypointInputs = new ArrayList<>();
    private List<LatLng> waypointCoords = new ArrayList<>();
    private List<TextInputEditText> friendEmailInputs = new ArrayList<>();

    private LatLng startPointCoord = null;
    private LatLng destinationCoord = null;

    private Integer activeInputIndex = null; // -1 start, -2 dest, 0... waypoint

    public PassengerHomeFragment() {}

    public static PassengerHomeFragment newInstance() {
        return new PassengerHomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_passenger_home, container, false);

        initializeViews(root);
        setupMap();
        setupDropdowns();
        setupListeners();

        return root;
    }

    private void initializeViews(View root) {
        etStartPoint = root.findViewById(R.id.etStartPoint);
        etDestination = root.findViewById(R.id.etDestination);
        layoutWaypoints = root.findViewById(R.id.layoutWaypoints);
        layoutFriendEmailsList = root.findViewById(R.id.layoutFriendEmailsList);
        layoutScheduledTime = root.findViewById(R.id.layoutScheduledTime);
        layoutFriendEmails = root.findViewById(R.id.layoutFriendEmails);
        actvVehicleType = root.findViewById(R.id.actvVehicleType);
        actvOrderTiming = root.findViewById(R.id.actvOrderTiming);
        actvTravelOption = root.findViewById(R.id.actvTravelOption);
        cbHasBaby = root.findViewById(R.id.cbHasBaby);
        cbHasPets = root.findViewById(R.id.cbHasPets);
        etScheduledTime = root.findViewById(R.id.etScheduledTime);
        btnOrderRide = root.findViewById(R.id.btnOrderRide);
        btnCancel = root.findViewById(R.id.btnCancel);
        btnAddWaypoint = root.findViewById(R.id.btnAddWaypoint);
        btnRemoveWaypoint = root.findViewById(R.id.btnRemoveWaypoint);
        btnAddFriendEmail = root.findViewById(R.id.btnAddFriendEmail);
        btnRemoveFriendEmail = root.findViewById(R.id.btnRemoveFriendEmail);
    }

    private void setupDropdowns() {
        String[] vehicleTypes = {"Any", "SUV", "SEDAN", "LUXURY"};
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                vehicleTypes
        );
        actvVehicleType.setAdapter(vehicleAdapter);
        actvVehicleType.setText(vehicleAdapter.getItem(0), false);

        String[] orderTimings = {"Order now", "Order later"};
        ArrayAdapter<String> timingAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                orderTimings
        );
        actvOrderTiming.setAdapter(timingAdapter);
        actvOrderTiming.setText(timingAdapter.getItem(0), false);

        String[] travelOptions = {"Alone", "With friends"};
        ArrayAdapter<String> travelAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                travelOptions
        );
        actvTravelOption.setAdapter(travelAdapter);
        actvTravelOption.setText(travelAdapter.getItem(0), false);
    }

    private void setupListeners() {
        etStartPoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) activeInputIndex = -1;
        });
        etDestination.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) activeInputIndex = -2;
        });

        btnAddWaypoint.setOnClickListener(v -> addWaypoint());
        btnRemoveWaypoint.setOnClickListener(v -> removeLastWaypoint());

        actvOrderTiming.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                layoutScheduledTime.setVisibility(View.VISIBLE);
            } else {
                layoutScheduledTime.setVisibility(View.GONE);
            }
        });

        actvTravelOption.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                layoutFriendEmails.setVisibility(View.VISIBLE);
            } else {
                layoutFriendEmails.setVisibility(View.GONE);
                friendEmailInputs.clear();
                layoutFriendEmailsList.removeAllViews();
            }
        });

        etScheduledTime.setOnClickListener(v -> showTimePicker());

        btnAddFriendEmail.setOnClickListener(v -> addFriendEmail());
        btnRemoveFriendEmail.setOnClickListener(v -> removeLastFriendEmail());

        btnOrderRide.setOnClickListener(v -> orderRide());
        btnCancel.setOnClickListener(v -> resetForm());
    }

    private void setupMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        mMap.addMarker(new MarkerOptions()
                .position(noviSad)
                .title("Novi Sad"));

        mMap.setOnMapClickListener(latLng -> {
            if (activeInputIndex == null) return;

            String displayValue = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude);

            if (activeInputIndex == -1) {
                startPointCoord = latLng;
                etStartPoint.setText(displayValue);
                etStartPoint.clearFocus();
            } else if (activeInputIndex == -2) {
                destinationCoord = latLng;
                etDestination.setText(displayValue);
                etDestination.clearFocus();
            } else if (activeInputIndex >= 0 && activeInputIndex < waypointInputs.size()) {
                waypointCoords.set(activeInputIndex, latLng);
                waypointInputs.get(activeInputIndex).setText(displayValue);
                waypointInputs.get(activeInputIndex).clearFocus();
            }

            Toast.makeText(requireContext(),
                    "Location set: " + displayValue,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void addWaypoint() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View waypointView = inflater.inflate(R.layout.item_waypoint, layoutWaypoints, false);

        TextInputEditText etWaypoint = waypointView.findViewById(R.id.etWaypoint);
        etWaypoint.setHint("Waypoint " + (waypointInputs.size() + 1));

        int waypointIndex = waypointInputs.size();
        etWaypoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) activeInputIndex = waypointIndex;
        });

        waypointInputs.add(etWaypoint);
        waypointCoords.add(null);
        layoutWaypoints.addView(waypointView);
        btnRemoveWaypoint.setEnabled(true);
    }

    // Remove last non-destination waypoint
    private void removeLastWaypoint() {
        if (!waypointInputs.isEmpty()) {
            int lastIndex = waypointInputs.size() - 1;

            layoutWaypoints.removeViewAt(lastIndex);
            waypointInputs.remove(lastIndex);
            waypointCoords.remove(lastIndex);

            if (waypointInputs.isEmpty()) {
                btnRemoveWaypoint.setEnabled(false);
            }
        }
    }

    private void addFriendEmail() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View emailView = inflater.inflate(R.layout.item_friend_email, layoutFriendEmailsList, false);

        TextInputEditText etEmail = emailView.findViewById(R.id.etFriendEmail);

        friendEmailInputs.add(etEmail);
        layoutFriendEmailsList.addView(emailView);

        btnRemoveFriendEmail.setEnabled(true);
    }

    private void removeLastFriendEmail() {
        if (!friendEmailInputs.isEmpty()) {
            int lastIndex = friendEmailInputs.size() - 1;

            layoutFriendEmailsList.removeViewAt(lastIndex);
            friendEmailInputs.remove(lastIndex);

            if (friendEmailInputs.isEmpty()) {
                btnRemoveFriendEmail.setEnabled(false);
            }
        }
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select scheduled time")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);
            etScheduledTime.setText(time);
        });

        timePicker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void orderRide() {
        if (etStartPoint.getText().toString().trim().isEmpty()) {
            etStartPoint.setError("Starting point is required");
            return;
        }
        if (etDestination.getText().toString().trim().isEmpty()) {
            etDestination.setError("Destination is required");
            return;
        }
        for (TextInputEditText waypointInput : waypointInputs) {
            if (waypointInput.getText().toString().trim().isEmpty()) {
                waypointInput.setError("Waypoint is required");
                return;
            }
        }
        if (actvOrderTiming.getText().toString().equals("Order later")) {
            String scheduledTime = etScheduledTime.getText().toString().trim();
            if (scheduledTime.isEmpty()) {
                etScheduledTime.setError("Please select scheduled time");
                return;
            }
        }
        if (actvTravelOption.getText().toString().equals("With friends")) {
            for (TextInputEditText emailInput : friendEmailInputs) {
                String email = emailInput.getText().toString().trim();
                if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Valid email is required");
                    return;
                }
            }
        }

        // TODO: Build request and call backend API
        // Build arrays: [start, waypoint1, waypoint2, ..., destination]

        Toast.makeText(requireContext(), "Ordering ride...", Toast.LENGTH_SHORT).show();
        Toast.makeText(requireContext(), "Ride ordered successfully!", Toast.LENGTH_LONG).show();
    }


    private void resetForm() {
        etStartPoint.setText("");
        etDestination.setText("");

        layoutWaypoints.removeAllViews();
        waypointInputs.clear();
        waypointCoords.clear();
        btnRemoveWaypoint.setEnabled(false);

        startPointCoord = null;
        destinationCoord = null;

        layoutFriendEmailsList.removeAllViews();
        friendEmailInputs.clear();
        btnRemoveFriendEmail.setEnabled(false);

        ArrayAdapter<String> vehicleAdapter = (ArrayAdapter<String>) actvVehicleType.getAdapter();
        ArrayAdapter<String> timingAdapter = (ArrayAdapter<String>) actvOrderTiming.getAdapter();
        ArrayAdapter<String> travelAdapter = (ArrayAdapter<String>) actvTravelOption.getAdapter();
        actvVehicleType.setText(vehicleAdapter.getItem(0), false);
        actvOrderTiming.setText(timingAdapter.getItem(0), false);
        actvTravelOption.setText(travelAdapter.getItem(0), false);

        cbHasBaby.setChecked(false);
        cbHasPets.setChecked(false);

        etScheduledTime.setText("");

        layoutScheduledTime.setVisibility(View.GONE);
        layoutFriendEmails.setVisibility(View.GONE);

        activeInputIndex = null;
    }
}