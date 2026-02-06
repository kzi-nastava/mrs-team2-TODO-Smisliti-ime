package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.repositories.DriverRepository;
import com.example.getgo.repositories.RideRepository;
import com.example.getgo.utils.MapManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class PassengerHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapManager mapManager;

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

        // Check for re-order data
        if (getArguments() != null && getArguments().containsKey("REORDER_RIDE")) {
            GetRideDTO reorderRide = (GetRideDTO) getArguments().getSerializable("REORDER_RIDE");
            prefillRideData(reorderRide);
        }
        return root;
    }

    private void prefillRideData(GetRideDTO ride) {
        if (ride == null) return;
        if (ride.getStartPoint() != null) etStartPoint.setText(ride.getStartPoint());
        if (ride.getEndPoint() != null) etDestination.setText(ride.getEndPoint());
        if (ride.getVehicleType() != null && actvVehicleType != null) {
            actvVehicleType.setText(ride.getVehicleType().toString(), false);
        }
        if (ride.getNeedsPetFriendly() != null && cbHasPets != null) cbHasPets.setChecked(ride.getNeedsPetFriendly());
        if (ride.getNeedsBabySeats() != null && cbHasBaby != null) cbHasBaby.setChecked(ride.getNeedsBabySeats());
        // Waypoints fill (addresses only) can be added here when DTO available
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
        mapManager = new MapManager(requireContext(), mMap);

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        loadActiveDrivers();

        mMap.setOnMapClickListener(this::handleMapClick);
    }

    private void loadActiveDrivers() {
        new Thread(() -> {
            try {
                DriverRepository repo = DriverRepository.getInstance();
                List<GetActiveDriverLocationDTO> drivers = repo.getActiveDriverLocations();

                requireActivity().runOnUiThread(() -> {
                    mapManager.updateDriverLocations(drivers);
                    Log.d("PassengerHome", "Loaded " + drivers.size() + " active drivers");
                });
            } catch (Exception e) {
                Log.e("PassengerHome", "Failed to load active drivers", e);
            }
        }).start();
    }

    private void handleMapClick(LatLng latLng) {
        if (activeInputIndex == null) return;

        mapManager.getAddressFromLocation(latLng, new MapManager.AddressCallback() {
            @Override
            public void onAddressFound(String address, LatLng coordinates) {
                setLocationForActiveInput(coordinates, address);
                drawRouteIfReady();
                activeInputIndex = null;
            }

            @Override
            public void onError(String error) {
                String displayValue = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude);
                setLocationForActiveInput(latLng, displayValue);
                activeInputIndex = null;
                Toast.makeText(requireContext(), "Location set (geocoding failed)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocationForActiveInput(LatLng coordinates, String displayText) {
        if (activeInputIndex == -1) {
            startPointCoord = coordinates;
            etStartPoint.setText(displayText);
            etStartPoint.clearFocus();
            mapManager.addWaypointMarker(coordinates, 0, "Start Point");

        } else if (activeInputIndex == -2) {
            destinationCoord = coordinates;
            etDestination.setText(displayText);
            etDestination.clearFocus();
            mapManager.addWaypointMarker(coordinates, 100, "Destination");

        } else if (activeInputIndex >= 0 && activeInputIndex < waypointInputs.size()) {
            waypointCoords.set(activeInputIndex, coordinates);
            waypointInputs.get(activeInputIndex).setText(displayText);
            waypointInputs.get(activeInputIndex).clearFocus();
            mapManager.addWaypointMarker(coordinates, activeInputIndex + 1, "Waypoint " + (activeInputIndex + 1)); // CHANGED: +1 to avoid overlap with start
        }
    }

    private void drawRouteIfReady() {
        List<LatLng> allPoints = new ArrayList<>();

        if (startPointCoord != null) {
            allPoints.add(startPointCoord);
            Log.d("PassengerHome", "Added start point: " + startPointCoord);
        }
        for (LatLng waypoint : waypointCoords) {
            if (waypoint != null) {
                allPoints.add(waypoint);
                Log.d("PassengerHome", "Added waypoint: " + waypoint);
            }
        }
        if (destinationCoord != null) {
            allPoints.add(destinationCoord);
            Log.d("PassengerHome", "Added destination: " + destinationCoord);
        }

        Log.d("PassengerHome", "Total points for route: " + allPoints.size());

        if (allPoints.size() < 2) {
            Log.d("PassengerHome", "Not enough points to draw route");
            return;
        }

        Log.d("PassengerHome", "Calling mapManager.drawRoute()");
        mapManager.drawRoute(allPoints, null);
    }

    private void addWaypoint() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View waypointView = inflater.inflate(R.layout.item_waypoint, layoutWaypoints, false);

        TextInputEditText etWaypoint = waypointView.findViewById(R.id.etWaypoint);
        etWaypoint.setHint("Waypoint " + (waypointInputs.size() + 1));

        int waypointIndex = waypointInputs.size();
        etWaypoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeInputIndex = waypointIndex;
            }
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

            drawRouteIfReady();
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

        // Geocode addresses if coordinates are not set
        if (startPointCoord == null) {
            String startAddress = etStartPoint.getText().toString().trim();
            if (!startAddress.isEmpty()) {
                geocodeAddressAndOrder(startAddress, true);
                return; // Will retry after geocoding
            }
        }
        if (destinationCoord == null) {
            String destAddress = etDestination.getText().toString().trim();
            if (!destAddress.isEmpty()) {
                geocodeAddressAndOrder(destAddress, false);
                return;
            }
        }

        // Geocode waypoints if needed
        for (int i = 0; i < waypointInputs.size(); i++) {
            if (waypointCoords.get(i) == null) {
                String addr = waypointInputs.get(i).getText().toString().trim();
                if (!addr.isEmpty()) {
                    final int idx = i;
                    geocodeWaypointAndOrder(addr, idx);
                    return;
                }
            }
        }

        // All coordinates ready, proceed with order
        submitRideOrder();
    }

    private void geocodeAddressAndOrder(String address, boolean isStart) {
        mapManager.getCoordinatesFromAddress(address, new MapManager.CoordinatesCallback() {
            @Override
            public void onCoordinatesFound(LatLng coordinates) {
                if (isStart) {
                    startPointCoord = coordinates;
                    mapManager.addWaypointMarker(coordinates, 0, "Start Point");
                } else {
                    destinationCoord = coordinates;
                    mapManager.addWaypointMarker(coordinates, 100, "Destination");
                }
                drawRouteIfReady();
                orderRide(); // Retry
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to geocode: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void geocodeWaypointAndOrder(String address, int index) {
        mapManager.getCoordinatesFromAddress(address, new MapManager.CoordinatesCallback() {
            @Override
            public void onCoordinatesFound(LatLng coordinates) {
                waypointCoords.set(index, coordinates);
                mapManager.addWaypointMarker(coordinates, index + 1, "Waypoint " + (index + 1));
                drawRouteIfReady();
                orderRide(); // Retry
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to geocode waypoint: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRideOrder() {
        List<Double> lats = new ArrayList<>();
        List<Double> lngs = new ArrayList<>();
        List<String> addrs = new ArrayList<>();

        lats.add(startPointCoord.latitude);
        lngs.add(startPointCoord.longitude);
        addrs.add(etStartPoint.getText().toString());

        for (int i = 0; i < waypointCoords.size(); i++) {
            LatLng coord = waypointCoords.get(i);
            if (coord != null) {
                lats.add(coord.latitude);
                lngs.add(coord.longitude);
                addrs.add(waypointInputs.get(i).getText().toString());
            }
        }

        lats.add(destinationCoord.latitude);
        lngs.add(destinationCoord.longitude);
        addrs.add(etDestination.getText().toString());

        CreateRideRequestDTO request = new CreateRideRequestDTO(
                lats,
                lngs,
                addrs,
                isOrderLater() ? etScheduledTime.getText().toString() : null,
                isWithFriends() ? getFriendEmails() : null,
                cbHasBaby.isChecked(),
                cbHasPets.isChecked(),
                actvVehicleType.getText().toString()
        );

        btnOrderRide.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                CreatedRideResponseDTO response = repo.orderRide(request);

                requireActivity().runOnUiThread(() -> {
                    btnOrderRide.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Ride ordered successfully! ID: " + response.getRideId(),
                            Toast.LENGTH_LONG).show();
                    resetForm();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    btnOrderRide.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Failed to order ride: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean isOrderLater() {
        return actvOrderTiming.getText().toString().equals("Order later");
    }

    private boolean isWithFriends() {
        return actvTravelOption.getText().toString().equals("With friends");
    }

    private List<String> getFriendEmails() {
        List<String> emails = new ArrayList<>();
        for (TextInputEditText input : friendEmailInputs) {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                emails.add(email);
            }
        }
        return emails;
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
