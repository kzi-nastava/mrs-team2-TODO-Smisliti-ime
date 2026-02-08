package com.example.getgo.fragments.drivers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetRideFinishedDTO;
import com.example.getgo.dtos.ride.RideCompletionDTO;
import com.example.getgo.dtos.ride.UpdatedRideDTO;
import com.example.getgo.dtos.ride.StopRideDTO;
import com.example.getgo.repositories.RideRepository;
import com.example.getgo.utils.JwtUtils;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.WebSocketManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHomeFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "DriverHomeFragment";
    private static final String PREFS_NAME = "getgo_prefs";

    private GoogleMap mMap;
    private MapManager mapManager;
    private WebSocketManager webSocketManager;

    private LinearLayout layoutNoRide, layoutRideInfo, layoutRideCompleted, layoutScheduledTime, layoutCancelForm;
    private TextView tvRideId, tvRideTitle, tvStatus, tvStartPoint, tvDestination, tvPassengerInfo, tvPassengerCount;
    private TextView tvEstimatedTime, tvEstimatedPrice, tvScheduledTime;
    private TextView tvFinalPrice, tvDuration;
    private Button btnPrimaryAction, btnSecondaryAction, btnOk, btnConfirmCancel, btnDismissCancel;
    private EditText etCancelReason;

    private GetDriverActiveRideDTO currentRide;
    private String driverEmail;
    private boolean pendingDrawRoute = false;


    public DriverHomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_driver_home, container, false);

        initializeViews(root);
        setupMap();
        loadDriverEmail();
        setupWebSocket();
        loadActiveRide();

        return root;
    }

    private void initializeViews(View root) {
        layoutNoRide = root.findViewById(R.id.layoutNoRide);
        layoutRideInfo = root.findViewById(R.id.layoutRideInfo);
        layoutRideCompleted = root.findViewById(R.id.layoutRideCompleted);
        layoutScheduledTime = root.findViewById(R.id.layoutScheduledTime);
        layoutCancelForm = root.findViewById(R.id.layoutCancelForm);

        tvRideId = root.findViewById(R.id.tvRideId);
        tvRideTitle = root.findViewById(R.id.tvRideTitle);
        tvStatus = root.findViewById(R.id.tvStatus);
        tvStartPoint = root.findViewById(R.id.tvStartPoint);
        tvDestination = root.findViewById(R.id.tvDestination);
        tvPassengerInfo = root.findViewById(R.id.tvPassengerInfo);
        tvPassengerCount = root.findViewById(R.id.tvPassengerCount);
        tvEstimatedTime = root.findViewById(R.id.tvEstimatedTime);
        tvEstimatedPrice = root.findViewById(R.id.tvEstimatedPrice);
        tvScheduledTime = root.findViewById(R.id.tvScheduledTime);
        tvFinalPrice = root.findViewById(R.id.tvFinalPrice);
        tvDuration = root.findViewById(R.id.tvDuration);

        btnPrimaryAction = root.findViewById(R.id.btnPrimaryAction);
        btnSecondaryAction = root.findViewById(R.id.btnSecondaryAction);
        btnOk = root.findViewById(R.id.btnOk);
        btnConfirmCancel = root.findViewById(R.id.btnConfirmCancel);
        btnDismissCancel = root.findViewById(R.id.btnDismissCancel);

        etCancelReason = root.findViewById(R.id.etCancelReason);

        btnPrimaryAction.setOnClickListener(v -> handlePrimaryAction());
        btnSecondaryAction.setOnClickListener(v -> handleSecondaryAction());
        btnOk.setOnClickListener(v -> handleOkClick());
        btnConfirmCancel.setOnClickListener(v -> confirmCancelRide());
        btnDismissCancel.setOnClickListener(v -> dismissCancelForm());
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

        if (pendingDrawRoute && currentRide != null) {
            drawRideRoute();
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

    }

    private void loadDriverEmail() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token == null) {
            Log.e(TAG, "JWT token not found");
            return;
        }

        driverEmail = JwtUtils.getEmailFromToken(token);

        if (driverEmail == null) {
            Log.e(TAG, "Failed to extract email from JWT");
        }
    }

    private void setupWebSocket() {
        if (driverEmail == null) return;

        webSocketManager = new WebSocketManager();
        webSocketManager.connect();

        webSocketManager.subscribeToRideAssigned(driverEmail, ride -> {
            requireActivity().runOnUiThread(() -> {
                currentRide = ride;
                updateUI();
                drawRideRoute();
            });
        });

        webSocketManager.subscribeToRideStatusUpdates(driverEmail, update -> {
            requireActivity().runOnUiThread(() -> {
                if (currentRide != null && currentRide.getRideId().equals(update.getRideId())) {
                    currentRide.setStatus(update.getStatus());
                    updateUI();
                }
            });
        });

        webSocketManager.subscribeToRideFinished(driverEmail, finished -> {
            requireActivity().runOnUiThread(() -> showRideCompleted(finished));
        });

        webSocketManager.subscribeToDriverLocation(driverEmail, location -> {
            requireActivity().runOnUiThread(() -> {
                if (mapManager != null && location.getLatitude() != null && location.getLongitude() != null) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mapManager.updateDriverPosition(position);
                }
            });
        });
    }

    private void loadActiveRide() {
        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                GetDriverActiveRideDTO ride = repo.getDriverActiveRide();

                requireActivity().runOnUiThread(() -> {
                    if (ride != null) {
                        currentRide = ride;

                        if (ride.getStatus().equals("DRIVER_ARRIVED_AT_DESTINATION")) {
                            Log.d(TAG, "Ride is at destination but not yet ended. Showing it as active.");
                        }

                        updateUI();
                        drawRideRoute();
                    } else {
                        showNoRide();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load active ride", e);
                requireActivity().runOnUiThread(() -> {
                    currentRide = null;
                    showNoRide();
                });
            }
        }).start();
    }

    private void updateUI() {
        if (currentRide == null) {
            showNoRide();
            return;
        }

        showRideInfo();

        // Update basic info
        tvRideId.setText(String.valueOf(currentRide.getRideId()));
        tvStatus.setText(currentRide.getStatus());
        tvStartPoint.setText(currentRide.getStartingPoint());
        tvDestination.setText(currentRide.getEndingPoint());
        tvPassengerInfo.setText(currentRide.getPassengerName());
        tvPassengerCount.setText(String.valueOf(currentRide.getPassengerCount()));
        tvEstimatedTime.setText(getString(R.string.time_format, currentRide.getEstimatedTimeMin()));
        tvEstimatedPrice.setText(String.format(Locale.ENGLISH, getString(R.string.price_format), currentRide.getEstimatedPrice()));

        // Show scheduled time if exists
        if (currentRide.getScheduledTime() != null) {
            layoutScheduledTime.setVisibility(View.VISIBLE);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd MMM", Locale.ENGLISH);
            tvScheduledTime.setText(currentRide.getScheduledTime().format(formatter));
        } else {
            layoutScheduledTime.setVisibility(View.GONE);
        }

        // Update UI based on status
        String status = currentRide.getStatus();
        switch (status) {
            case "DRIVER_READY":
                tvRideTitle.setText(R.string.ride_received);
                btnPrimaryAction.setText(R.string.accept_ride);
                btnPrimaryAction.setVisibility(View.VISIBLE);
                btnSecondaryAction.setText(R.string.cancel_ride_button);
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;

            case "DRIVER_INCOMING":
                tvRideTitle.setText(R.string.arriving_at_start_point);
                btnPrimaryAction.setVisibility(View.GONE);
                btnSecondaryAction.setText(R.string.cancel_ride_button);
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;

            case "DRIVER_ARRIVED":
                tvRideTitle.setText(R.string.start_ride);
                btnPrimaryAction.setText(R.string.start_ride_button);
                btnPrimaryAction.setVisibility(View.VISIBLE);
                btnSecondaryAction.setText(R.string.cancel_ride_button);
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;

            case "ACTIVE":
                tvRideTitle.setText(R.string.ride_in_progress);
                btnPrimaryAction.setText(R.string.stop_ride);
                btnPrimaryAction.setVisibility(View.VISIBLE);
                btnSecondaryAction.setText(R.string.panic_button);
                btnSecondaryAction.setVisibility(View.VISIBLE);
                break;

            case "DRIVER_ARRIVED_AT_DESTINATION":
                tvRideTitle.setText(R.string.arrived_at_destination);
                btnPrimaryAction.setText(R.string.end_ride);
                btnPrimaryAction.setVisibility(View.VISIBLE);
                btnSecondaryAction.setVisibility(View.GONE);
                break;

            default:
                showNoRide();
                break;
        }
    }
    private void drawRideRoute() {
        if (currentRide == null) return;

        if (mapManager == null || mMap == null) {
            pendingDrawRoute = true;
            return;
        }

        if (currentRide.getLatitudes() == null ||
                currentRide.getLongitudes() == null ||
                currentRide.getLatitudes().size() < 2) {
            Log.w("ROUTE_DEBUG", "Not enough points to draw route");
            return;
        }

        List<LatLng> waypoints = new ArrayList<>();
        for (int i = 0; i < currentRide.getLatitudes().size(); i++) {
            waypoints.add(new LatLng(
                    currentRide.getLatitudes().get(i),
                    currentRide.getLongitudes().get(i)
            ));
        }

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(waypoints.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoints.size() - 1)).title("Destination"));

        mapManager.drawRouteOSRM(waypoints, null);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : waypoints) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 = padding u px
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 13f));

        pendingDrawRoute = false;
    }

    private void handlePrimaryAction() {
        if (currentRide == null) return;

        String status = currentRide.getStatus();
        switch (status) {
            case "DRIVER_READY":
                acceptRide();
                break;
            case "DRIVER_ARRIVED":
                startRide();
                break;
            case "ACTIVE":
                stopRide();
                break;
            case "DRIVER_ARRIVED_AT_DESTINATION":
                finishRide();
                break;
        }
    }

    private void handleSecondaryAction() {
        if (currentRide == null) return;

        String status = currentRide.getStatus();
        if (status.equals("ACTIVE")) {
            triggerPanic();
        } else {
            showCancelForm();
        }
    }

    private void showCancelForm() {
        layoutCancelForm.setVisibility(View.VISIBLE);
        etCancelReason.setText("");
        etCancelReason.requestFocus();
    }

    private void dismissCancelForm() {
        layoutCancelForm.setVisibility(View.GONE);
        etCancelReason.setText("");
    }

    private void confirmCancelRide() {
        String reason = etCancelReason.getText() != null ? etCancelReason.getText().toString().trim() : "";

        if (reason.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a reason for cancellation", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmCancel.setEnabled(false);
        btnDismissCancel.setEnabled(false);

        com.example.getgo.api.services.RideApiService service =
                com.example.getgo.api.ApiClient.getClient().create(com.example.getgo.api.services.RideApiService.class);

        com.example.getgo.dtos.ride.CancelRideRequestDTO dto =
                new com.example.getgo.dtos.ride.CancelRideRequestDTO(reason);

        service.cancelRideByDriver(currentRide.getRideId(), dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                requireActivity().runOnUiThread(() -> {
                    btnConfirmCancel.setEnabled(true);
                    btnDismissCancel.setEnabled(true);

                    if (response.isSuccessful()) {
                        dismissCancelForm();
                        currentRide = null;
                        showNoRide();
                        Toast.makeText(requireContext(), "Ride cancelled", Toast.LENGTH_SHORT).show();
                        if (mapManager != null) mapManager.reset();
                    } else {
                        Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    btnConfirmCancel.setEnabled(true);
                    btnDismissCancel.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void cancelRide() {
        // Removed - now using showCancelForm()
    }

    private void handleOkClick() {
        currentRide = null;
        showNoRide();
        mapManager.reset();
    }

    private void acceptRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO response = repo.acceptRide(currentRide.getRideId());

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    currentRide.setStatus(response.getStatus());
                    updateUI();
                    Toast.makeText(requireContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to accept ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to accept ride", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void startRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO response = repo.startRide(currentRide.getRideId());

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    currentRide.setStatus(response.getStatus());
                    updateUI();
                    Toast.makeText(requireContext(), "Ride started", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to start ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to start ride", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void stopRide() {
        if (currentRide == null) return;

        btnPrimaryAction.setEnabled(false);

        com.example.getgo.api.services.RideApiService service =
                com.example.getgo.api.ApiClient.getClient().create(com.example.getgo.api.services.RideApiService.class);

        // Get current timestamp in ISO format
        String stoppedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Try to get last known driver position, fallback to 0.0
        double lat = 0.0;
        double lon = 0.0;
        try {
            if (mMap != null && ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                android.location.Location location = mMap.getMyLocation();
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot access location for stop ride", e);
        }

        StopRideDTO dto = new StopRideDTO(lat, lon, stoppedAt);

        Log.d(TAG, "Stopping ride: " + currentRide.getRideId() + " at " + lat + "," + lon + " time: " + stoppedAt);

        service.stopRide(currentRide.getRideId(), dto).enqueue(new Callback<RideCompletionDTO>() {
            @Override
            public void onResponse(Call<RideCompletionDTO> call, Response<RideCompletionDTO> response) {
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        RideCompletionDTO completion = response.body();
                        Log.d(TAG, "Stop ride success: " + completion.getRideId());
                        showRideCompleted(completion);
                        currentRide = null;
                        if (mapManager != null) mapManager.reset();
                        Toast.makeText(requireContext(), "Ride stopped", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Stop ride failed: " + response.code() + " " + response.message());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read error body", e);
                        }
                        Toast.makeText(requireContext(), "Failed to stop ride: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<RideCompletionDTO> call, Throwable t) {
                Log.e(TAG, "Stop ride network error", t);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void finishRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO response = repo.finishRide(currentRide.getRideId(), "FINISHED");

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Ride finished", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to finish ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to finish ride", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void triggerPanic() {
        // TODO: Implement panic
        Toast.makeText(requireContext(), "Panic not yet implemented", Toast.LENGTH_SHORT).show();
    }

    private void showNoRide() {
        layoutNoRide.setVisibility(View.VISIBLE);
        layoutRideInfo.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.GONE);
    }

    private void showRideInfo() {
        layoutNoRide.setVisibility(View.GONE);
        layoutRideInfo.setVisibility(View.VISIBLE);
        layoutRideCompleted.setVisibility(View.GONE);
    }

    private void showRideCompleted(GetRideFinishedDTO finished) {
        layoutNoRide.setVisibility(View.GONE);
        layoutRideInfo.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.VISIBLE);

        tvFinalPrice.setText(String.format(Locale.ENGLISH, "%.2f RSD", finished.getPrice()));
        tvDuration.setText(String.format(Locale.ENGLISH,"%d minutes", finished.getDurationMinutes()));
    }

    // Overload for RideCompletionDTO
    private void showRideCompleted(RideCompletionDTO completion) {
        layoutNoRide.setVisibility(View.GONE);
        layoutRideInfo.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.VISIBLE);

        tvFinalPrice.setText(String.format(Locale.ENGLISH, "%.2f RSD", completion.getPrice()));
        tvDuration.setText(String.format(Locale.ENGLISH, "%d minutes", completion.getDurationMinutes()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}

