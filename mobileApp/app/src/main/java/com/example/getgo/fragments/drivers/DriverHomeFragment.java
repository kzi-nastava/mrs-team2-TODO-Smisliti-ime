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
import com.example.getgo.utils.ToastHelper;
import com.example.getgo.utils.WebSocketManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// new imports for notifications
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DriverHomeFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "DriverHomeFragment";
    private static final String PREFS_NAME = "getgo_prefs";

    // Notification constants
    private static final String NOTIF_CHANNEL_ID = "getgo_general";
    private static final int NOTIF_ID_PANIC = 2001;
    private static final int NOTIF_ID_CANCEL = 2002;

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

    private int estimatedTime = 0;


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

        // ensure notification channel exists
        createNotificationChannelIfNeeded();

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
        mapManager = new MapManager(requireActivity(), mMap);

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        if (pendingDrawRoute && currentRide != null) {
            Log.d(TAG, "Map ready, drawing pending route");
            pendingDrawRoute = false;
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
                Log.d(TAG, "Ride assigned via WebSocket: " + ride.getRideId());
                currentRide = ride;
                updateUI();
                if (isMapReady()) {
                    drawRideRoute();
                } else {
                    Log.d(TAG, "Map not ready, setting pendingDrawRoute flag");
                    pendingDrawRoute = true;
                }
            });
        });

        webSocketManager.subscribeToRideStatusUpdates(driverEmail, update -> {
            requireActivity().runOnUiThread(() -> {
                if (currentRide != null && currentRide.getRideId().equals(update.getRideId())) {
                    Log.d(TAG, "Status update: " + update.getStatus());
                    currentRide.setStatus(update.getStatus());
                    updateUI();

                    // If the ride was canceled by someone else (passenger or driver), ensure any
                    // open cancel form is dismissed and the driver UI resets to no-ride state.
                    if ("CANCELED".equalsIgnoreCase(update.getStatus())) {
                        dismissCancelForm();
                        currentRide = null;
                        showNoRide();
                        String msg = null;
                        try { msg = update.getMessage(); } catch (Exception ignored) {}
                        if (msg == null || msg.isEmpty()) msg = "Ride cancelled";
                        ToastHelper.showShort(requireContext(), msg);
                    }
                }
            });
        });

        webSocketManager.subscribeToRideFinished(driverEmail, finished -> {
            requireActivity().runOnUiThread(() -> {
                Log.d(TAG, "Ride finished notification received");
                showRideCompleted(finished);
            });
        });

        webSocketManager.subscribeToDriverLocation(driverEmail, location -> {
            requireActivity().runOnUiThread(() -> {
                if (mapManager != null && location.getLatitude() != null && location.getLongitude() != null) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mapManager.updateDriverPosition(position);
                }
            });
        });

        // Subscribe to driver-specific ride cancelled events (server-notified)
        webSocketManager.subscribeToDriverRideCancelled(driverEmail, cancelled -> {
            requireActivity().runOnUiThread(() -> {
                try {
                    Log.d(TAG, "Driver ride cancelled event received for rideId=" + cancelled.getRideId());
                    String by = cancelled.getCancelledBy() != null ? cancelled.getCancelledBy() : "Unknown";
                    String reason = cancelled.getReason() != null ? cancelled.getReason() : "";
                    String text = "Ride cancelled by " + by + (reason.isEmpty() ? "" : (": " + reason));
                    ToastHelper.showShort(requireContext(), text);
                    showSystemNotification("Ride cancelled", text, NOTIF_ID_CANCEL);

                    // Reset UI
                    dismissCancelForm();
                    currentRide = null;
                    showNoRide();
                    if (mapManager != null) mapManager.reset();
                } catch (Exception ex) {
                    Log.e(TAG, "Error handling driver ride cancelled event", ex);
                }
            });
        });
    }

    private boolean isMapReady() {
        return mMap != null && mapManager != null;
    }

    private void loadActiveRide() {
        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                GetDriverActiveRideDTO ride = repo.getDriverActiveRide();

                requireActivity().runOnUiThread(() -> {
                    if (ride != null) {
                        Log.d(TAG, "Loaded active ride: " + ride.getRideId());
                        currentRide = ride;

                        if (ride.getStatus().equals("DRIVER_ARRIVED_AT_DESTINATION")) {
                            Log.d(TAG, "Ride is at destination but not yet ended. Showing it as active.");
                        }

                        updateUI();
                        if (isMapReady()) {
                            drawRideRoute();
                        } else {
                            Log.d(TAG, "Map not ready, setting pendingDrawRoute flag");
                            pendingDrawRoute = true;
                        }
                    } else {
                        Log.d(TAG, "No active ride found");
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
        estimatedTime = (int) Math.round(currentRide.getEstimatedTimeMin());
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
        if (currentRide == null) {
            Log.w(TAG, "Cannot draw route: currentRide is null");
            return;
        }

        if (mapManager == null || mMap == null) {
            pendingDrawRoute = true;
            return;
        }

        if (currentRide.getLatitudes() == null ||
                currentRide.getLongitudes() == null ||
                currentRide.getLatitudes().size() < 2) {
            Log.w(TAG, "Not enough points to draw route. Latitudes: " +
                    (currentRide.getLatitudes() != null ? currentRide.getLatitudes().size() : "null") +
                    ", Longitudes: " +
                    (currentRide.getLongitudes() != null ? currentRide.getLongitudes().size() : "null"));
            return;
        }

        List<LatLng> waypoints = new ArrayList<>();
        for (int i = 0; i < currentRide.getLatitudes().size(); i++) {
            waypoints.add(new LatLng(
                    currentRide.getLatitudes().get(i),
                    currentRide.getLongitudes().get(i)
            ));
        }

        mapManager.clearRoute();
        mapManager.clearWaypoints();

        for (int i = 0; i < waypoints.size(); i++) {
            String title;

            if (i == 0) {
                title = "Start";
            } else if (i == waypoints.size() - 1) {
                title = "Destination";
            } else {
                title = "Waypoint " + i;
            }

            mapManager.addWaypointMarker(waypoints.get(i), i, title);
        }

        // Draw the route using OSRM
        mapManager.drawRouteOSRM(waypoints, new MapManager.RouteCallback() {
            @Override
            public void onRouteFound(int distanceMeters, int durationSeconds) {
                Log.d(TAG, "Route drawn successfully: " + distanceMeters + "m, " +
                        durationSeconds + "s (" + (durationSeconds / 60) + " min)");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to draw route: " + error);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to load route", Toast.LENGTH_SHORT).show()
                );
            }
        });

        // Fit camera to show all waypoints with padding
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : waypoints) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
            Log.d(TAG, "Camera adjusted to show all waypoints");
        } catch (Exception e) {
            Log.e(TAG, "Failed to adjust camera bounds", e);
        }

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
            // Immediately cancel the ride for the driver without asking for a reason
            cancelByDriverImmediate();
        }
    }

    private void showCancelForm() {
        // Kept for compatibility but drivers no longer use the cancel form in the UI.
        layoutCancelForm.setVisibility(View.GONE);
    }

    private void dismissCancelForm() {
        layoutCancelForm.setVisibility(View.GONE);
        etCancelReason.setText("");
    }

    private void confirmCancelRide() {
        // Kept for compatibility but drivers now use immediate cancel via cancelByDriverImmediate().
        cancelByDriverImmediate();
    }

    private void cancelByDriverImmediate() {
        if (currentRide == null) return;

        btnSecondaryAction.setEnabled(false);

        String defaultReason = "Driver cancelled";
        com.example.getgo.api.services.RideApiService service = com.example.getgo.api.ApiClient.getClient().create(com.example.getgo.api.services.RideApiService.class);
        com.example.getgo.dtos.ride.CancelRideRequestDTO dto = new com.example.getgo.dtos.ride.CancelRideRequestDTO(defaultReason);

        service.cancelRideByDriver(currentRide.getRideId(), dto).enqueue(new Callback<RideCompletionDTO>() {
            @Override
            public void onResponse(Call<RideCompletionDTO> call, Response<RideCompletionDTO> response) {
                final Long rideIdForNotif = currentRide != null ? currentRide.getRideId() : null;
                requireActivity().runOnUiThread(() -> {
                    btnSecondaryAction.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        RideCompletionDTO completion = response.body();
                        currentRide = null;
                        showNoRide();
                        String serverMsg = completion.getNotificationMessage();
                        if (serverMsg == null || serverMsg.isEmpty()) serverMsg = "Ride cancelled";
                        Toast.makeText(requireContext(), serverMsg, Toast.LENGTH_SHORT).show();
                        if (mapManager != null) mapManager.reset();

                        if (rideIdForNotif != null) {
                            showSystemNotification("Ride cancelled", serverMsg, NOTIF_ID_CANCEL);
                        }

                        // Refresh notifications
                        try {
                            com.example.getgo.api.services.NotificationApiService notifService = com.example.getgo.api.ApiClient.getNotificationApiService();
                            notifService.getNotifications().enqueue(new retrofit2.Callback<java.util.List<com.example.getgo.dtos.notification.NotificationDTO>>() {
                                @Override
                                public void onResponse(retrofit2.Call<java.util.List<com.example.getgo.dtos.notification.NotificationDTO>> call, retrofit2.Response<java.util.List<com.example.getgo.dtos.notification.NotificationDTO>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        java.util.List<com.example.getgo.dtos.notification.NotificationDTO> list = response.body();
                                        if (!list.isEmpty()) {
                                            android.util.Log.d("NOTIF_SYNC", "Latest notification (driver): " + list.get(0).getMessage());
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<java.util.List<com.example.getgo.dtos.notification.NotificationDTO>> call, Throwable t) {
                                    android.util.Log.e("NOTIF_SYNC", "Failed to refresh notifications (driver)", t);
                                }
                            });
                        } catch (Exception ex) {
                            android.util.Log.e("NOTIF_SYNC", "Error while refreshing notifications (driver)", ex);
                        }
                    } else {
                        ToastHelper.showShort(requireContext(), "Cancel failed");
                    }
                });
            }

            @Override
            public void onFailure(Call<RideCompletionDTO> call, Throwable t) {
                requireActivity().runOnUiThread(() -> {
                    btnSecondaryAction.setEnabled(true);
                    ToastHelper.showShort(requireContext(), "Cancel failed");
                });
            }
        });
    }

    private void handleOkClick() {
        currentRide = null;
        showNoRide();
        if (mapManager != null) mapManager.reset();
    }

    private void acceptRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO updated = repo.acceptRide(currentRide.getRideId());

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);

                    if (updated != null && updated.getStatus() != null) {
                        currentRide.setStatus(updated.getStatus());
                        updateUI();
                        ToastHelper.showShort(requireContext(), "Ride accepted");
                    } else {
                        ToastHelper.showShort(requireContext(), "Accept failed");
                        showNoRide();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to accept ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    ToastHelper.showShort(requireContext(), "Accept failed");
                });
            }
        }).start();
    }

    private void startRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO updated = repo.startRide(currentRide.getRideId());

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);

                    if (updated != null && updated.getStatus() != null) {
                        currentRide.setStatus(updated.getStatus());
                        updateUI();
                        ToastHelper.showShort(requireContext(), "Ride started");
                    } else {
                        ToastHelper.showShort(requireContext(), "Start failed");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to start ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    ToastHelper.showShort(requireContext(), "Start failed");
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
                        ToastHelper.showShort(requireContext(), "Ride stopped");
                    } else {
                        Log.e(TAG, "Stop ride failed: " + response.code() + " " + response.message());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot read error body", e);
                        }
                        ToastHelper.showError(requireContext(), "Failed to stop ride", String.valueOf(response.code()));
                    }
                });
            }

            @Override
            public void onFailure(Call<RideCompletionDTO> call, Throwable t) {
                Log.e(TAG, "Stop ride network error", t);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    ToastHelper.showError(requireContext(), "Failed to stop ride", t.getMessage());
                });
            }
        });
    }

    private void finishRide() {
        btnPrimaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                UpdatedRideDTO updated = repo.finishRide(currentRide.getRideId(), "FINISHED");

                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    if (updated != null && updated.getStatus() != null) {
                        // Show completed UI / navigate as needed
                        ToastHelper.showShort(requireContext(), "Ride finished");
                    } else {
                        ToastHelper.showShort(requireContext(), "Finish failed");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to finish ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnPrimaryAction.setEnabled(true);
                    ToastHelper.showShort(requireContext(), "Finish failed");
                });
            }
        }).start();
    }

    private void triggerPanic() {
        if (currentRide == null) {
            Toast.makeText(requireContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to trigger a panic alert? This will notify authorities.")
                .setPositiveButton("Yes, Send Alert", (dialog, which) -> {
                    btnSecondaryAction.setEnabled(false);

                    com.example.getgo.api.services.RideApiService service =
                            com.example.getgo.api.ApiClient.getClient().create(com.example.getgo.api.services.RideApiService.class);

                    service.triggerPanic(currentRide.getRideId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            requireActivity().runOnUiThread(() -> {
                                btnSecondaryAction.setEnabled(true);
                                if (response.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Emergency alert sent!", Toast.LENGTH_LONG).show();
                                    // Post OS notification for panic
                                    showSystemNotification("Emergency alert sent", "Panic alert sent for ride #" + currentRide.getRideId(), NOTIF_ID_PANIC);
                                } else {
                                    ToastHelper.showShort(requireContext(), "Panic failed");
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Failed to trigger panic", t);
                            requireActivity().runOnUiThread(() -> {
                                btnSecondaryAction.setEnabled(true);
                                ToastHelper.showError(requireContext(), "Failed to send panic alert", t.getMessage());
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Helper: create notification channel for API 26+
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "General";
            String description = "General app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }

    // Helper: show system notification (skips if POST_NOTIFICATIONS missing on Android 13+)
    private void showSystemNotification(String title, String text, int id) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "No POST_NOTIFICATIONS permission - skipping system notification");
                    return;
                }
            }

            Intent intent = new Intent(requireContext(), com.example.getgo.activities.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), NOTIF_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_car)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            NotificationManagerCompat.from(requireContext()).notify(id, builder.build());
        } catch (Exception ex) {
            Log.e(TAG, "Failed to show system notification", ex);
        }
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
        tvDuration.setText(String.format(Locale.ENGLISH,"%d minutes", estimatedTime));
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

