package com.example.getgo.fragments.passengers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.activities.MainActivity;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import com.example.getgo.dtos.ride.GetPassengerActiveRideDTO;
import com.example.getgo.dtos.ride.GetRideFinishedDTO;
import com.example.getgo.repositories.RideRepository;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.WebSocketManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideTrackingFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "PassengerRideTracking";
    private static final String PREFS_NAME = "getgo_prefs";
    private static final String NOTIF_TAG = "RIDE_NOTIF";


    private GoogleMap mMap;
    private MapManager mapManager;
    private WebSocketManager webSocketManager;
    private RideApiService rideApiService;

    private LinearLayout layoutLoading, layoutNoRide, layoutRideTracking, layoutRideCompleted;
    private TextView tvStatusMessage, tvStartPoint, tvDestination, tvDriverName;
    private TextView tvEstimatedTime, tvEstimatedPrice;
    private TextView tvFinalPrice, tvDuration, tvStartTime, tvEndTime;
    private TextView tvProgressPercent, tvTimeRemaining;
    private ProgressBar progressBar;
    private Button btnCancelRide, btnPanic, btnOk;

    private Button btnReport;
    private LinearLayout reportForm;
    private EditText editReport;
    private Button btnSubmitReport, btnCancelReport;

    private GetPassengerActiveRideDTO currentRide;
    private boolean panicSent = false;
    private int totalRouteDistanceMeters = 0;
    private Long finishedRideDriverId;

    private int estimatedTime = 0;


    public PassengerRideTrackingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_passenger_ride_tracking, container, false);

        initializeViews(root);
        setupMap();
        showLoading();
        setupWebSocket();
        loadActiveRide();

        requestNotificationPermission();

        return root;
    }

    private void initializeViews(View root) {
        layoutLoading = root.findViewById(R.id.layoutLoading);
        layoutNoRide = root.findViewById(R.id.layoutNoRide);
        layoutRideTracking = root.findViewById(R.id.layoutRideTracking);
        layoutRideCompleted = root.findViewById(R.id.layoutRideCompleted);

        tvStatusMessage = root.findViewById(R.id.tvStatusMessage);
        tvStartPoint = root.findViewById(R.id.tvStartPoint);
        tvDestination = root.findViewById(R.id.tvDestination);
        tvDriverName = root.findViewById(R.id.tvDriverName);
        tvEstimatedTime = root.findViewById(R.id.tvEstimatedTime);
        tvEstimatedPrice = root.findViewById(R.id.tvEstimatedPrice);

        tvFinalPrice = root.findViewById(R.id.tvFinalPrice);
        tvDuration = root.findViewById(R.id.tvDuration);
        tvStartTime = root.findViewById(R.id.tvStartTime);
        tvEndTime = root.findViewById(R.id.tvEndTime);

        tvProgressPercent = root.findViewById(R.id.tvProgressPercent);
        tvTimeRemaining = root.findViewById(R.id.tvTimeRemaining);
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        btnCancelRide = root.findViewById(R.id.btnCancelRide);
        btnPanic = root.findViewById(R.id.btnPanic);
        btnOk = root.findViewById(R.id.btnOk);

        btnReport = root.findViewById(R.id.btnReport);
        reportForm = root.findViewById(R.id.reportForm);
        editReport = root.findViewById(R.id.editReport);
        btnSubmitReport = root.findViewById(R.id.btnSubmitReport);
        btnCancelReport = root.findViewById(R.id.btnCancelReport);

        btnCancelRide.setOnClickListener(v -> cancelRide());
        btnPanic.setOnClickListener(v -> triggerPanic());
        btnOk.setOnClickListener(v -> handleFinishedRideOkClick());

        setupReportListeners();
    }

    private void setupReportListeners() {
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> {
                if (reportForm != null) {
                    reportForm.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnCancelReport != null) {
            btnCancelReport.setOnClickListener(v -> {
                if (reportForm != null) {
                    reportForm.setVisibility(View.GONE);
                }
            });
        }

        if (btnSubmitReport != null) {
            btnSubmitReport.setOnClickListener(v -> submitReport());
        }
    }

    private void submitReport() {
        if (currentRide == null) {
            Toast.makeText(requireContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        String reportText = editReport.getText().toString();
        if (reportText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a note", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateInconsistencyReportDTO dto = new CreateInconsistencyReportDTO(reportText);

        rideApiService.createInconsistencyReport(currentRide.getRideId(), dto)
                .enqueue(new Callback<CreatedInconsistencyReportDTO>() {
                    @Override
                    public void onResponse(Call<CreatedInconsistencyReportDTO> call,
                                           Response<CreatedInconsistencyReportDTO> response) {
                        if (response.isSuccessful()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        "Report sent successfully",
                                        Toast.LENGTH_SHORT).show();

                                if (reportForm != null) {
                                    reportForm.setVisibility(View.GONE);
                                }
                                if (editReport != null) {
                                    editReport.setText("");
                                }
                            });
                        } else {
                            Log.e(TAG, "Report error: " + response.code());
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        "Failed to send report",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<CreatedInconsistencyReportDTO> call, Throwable t) {
                        Log.e(TAG, "Report POST failed", t);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Failed to send report",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressBar.setMax(100);
            progressBar.setProgress(50);
            tvProgressPercent.setText("50%");
            tvTimeRemaining.setText("10 min");
        }, 3000);

    }

    private void setupWebSocket() {
        Log.d("WS_TEST", "Connecting WebSocket...");
        webSocketManager = new WebSocketManager();
        webSocketManager.connect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideApiService = ApiClient.getClient().create(RideApiService.class);
    }

    private void loadActiveRide() {
        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                GetPassengerActiveRideDTO ride = repo.getPassengerActiveRide();

                requireActivity().runOnUiThread(() -> {
                    if (ride != null) {
                        currentRide = ride;
                        updateUI();
                        drawRideRoute();
                        Log.d("RIDE_DEBUG", "Calling subscribeToRideDriverLocation");
                        subscribeToWebSocketUpdates(ride.getRideId());
                    } else {
                        showNoRide();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load active ride", e);
                requireActivity().runOnUiThread(this::showNoRide);
            }
        }).start();
    }

    private void subscribeToWebSocketUpdates(Long rideId) {
        Log.d("WS_TEST", "Subscribed to rideId = " + rideId);
        webSocketManager.subscribeToRideDriverLocation(rideId, location -> {
            Log.d("WS_TEST", "RAW LOCATION MESSAGE RECEIVED");
            Log.d("RIDE_DEBUG", "LOCATION RECEIVED");
            requireActivity().runOnUiThread(() -> {
                if (mapManager != null && location.getLatitude() != null && location.getLongitude() != null) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mapManager.updateDriverPosition(position);

                    if (currentRide != null && totalRouteDistanceMeters > 0) {
                        double traveled = mapManager.getDistanceAlongRoute(position);
                        Log.d(TAG, "Traveled: " + traveled);
                        int progressPercent = (int) ((traveled / totalRouteDistanceMeters) * 100);
                        Log.d(TAG, "Traveled: " + traveled + " / " + totalRouteDistanceMeters + " -> " + progressPercent + "%");

                        progressBar.setProgress(Math.min(progressPercent, 100));
                        tvProgressPercent.setText(progressPercent + "%");

                        // Remaining time estimation
                        double remainingPercent = 1.0 - ((double) progressPercent / 100);
                        double totalEstimatedMinutes = currentRide.getEstimatedTimeMin();
                        double remainingMinutes = totalEstimatedMinutes * remainingPercent;

                        tvTimeRemaining.setText(String.format(Locale.ENGLISH, "%.0f min", remainingMinutes));
                    }

                }
            });
        });

        webSocketManager.subscribeToPassengerRideStatusUpdates(rideId, update -> {
            requireActivity().runOnUiThread(() -> {
                Log.d(TAG, "Status update: " + update.getStatus());
                if (currentRide != null) {
                    currentRide.setStatus(update.getStatus());
                    updateStatusMessage(update.getStatus(), update.getMessage());
                }
            });
        });

        webSocketManager.subscribeToPassengerRideFinished(rideId, finished -> {
            requireActivity().runOnUiThread(() -> {

                Log.d(NOTIF_TAG, "Ride finished received! rideId = " + finished.getRideId());
                Log.i(NOTIF_TAG, "Ride finished received! rideId = " + finished.getRideId());
                Log.e(NOTIF_TAG, "Ride finished received! rideId = " + finished.getRideId());

                showRideCompleted(finished);
                showRideFinishedNotification(finished);
            });
        });

        webSocketManager.subscribeToPassengerRideStopped(rideId, stopped -> {
            requireActivity().runOnUiThread(() -> {
                Log.d(TAG, "Ride stopped early");
                GetRideFinishedDTO finished = new GetRideFinishedDTO(
                        stopped.getRideId(),
                        stopped.getStatus(),
                        stopped.getPrice(),
                        stopped.getStartTime(),
                        stopped.getEndTime(),
                        stopped.getDurationMinutes(),
                        stopped.getDriverId()
                );
                showRideCompleted(finished);
            });
        });
    }

    private void updateUI() {
        if (currentRide == null) {
            showNoRide();
            return;
        }

        showRideTracking();

        tvStartPoint.setText(currentRide.getStartingPoint());
        tvDestination.setText(currentRide.getEndingPoint());
        tvDriverName.setText(currentRide.getDriverName() != null ? currentRide.getDriverName() : "Assigning...");
        estimatedTime = (int) Math.round(currentRide.getEstimatedTimeMin());
        tvEstimatedTime.setText(String.format(Locale.ENGLISH, "%.0f min", currentRide.getEstimatedTimeMin()));
        tvEstimatedPrice.setText(String.format(Locale.ENGLISH, "%.2f RSD", currentRide.getEstimatedPrice()));

        if (tvTimeRemaining != null) {
            tvTimeRemaining.setText(String.format(Locale.ENGLISH, "%.0f min", currentRide.getEstimatedTimeMin()));
        }

        updateStatusMessage(currentRide.getStatus(), null);
        updateButtonVisibility(currentRide.getStatus());
    }

    private void updateStatusMessage(String status, String customMessage) {
        if (customMessage != null && !customMessage.isEmpty()) {
            tvStatusMessage.setText(customMessage);
            return;
        }

        switch (status) {
            case "DRIVER_FINISHING_PREVIOUS_RIDE":
                tvStatusMessage.setText("Driver is finishing their current ride. Please wait...");
                break;
            case "DRIVER_READY":
                tvStatusMessage.setText("Driver is ready! Waiting to start...");
                break;
            case "DRIVER_INCOMING":
                tvStatusMessage.setText("Driver is on the way to pick you up!");
                break;
            case "DRIVER_ARRIVED":
                tvStatusMessage.setText("Driver has arrived at pickup location!");
                break;
            case "ACTIVE":
                tvStatusMessage.setText("Ride in progress!");
                break;
            case "DRIVER_ARRIVED_AT_DESTINATION":
                tvStatusMessage.setText("Driver has arrived at the destination!");
                break;
            case "FINISHED":
                tvStatusMessage.setText("Ride completed!");
                break;
            default:
                tvStatusMessage.setText("Tracking ride...");
        }
    }

    private void updateButtonVisibility(String status) {
        boolean canCancel = !status.equals("ACTIVE") &&
                !status.equals("FINISHED") &&
                !status.equals("DRIVER_ARRIVED_AT_DESTINATION");

        btnCancelRide.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        btnPanic.setVisibility(View.VISIBLE);
    }

    private void drawRideRoute() {
        if (currentRide == null || mapManager == null) return;

        List<LatLng> waypoints = new ArrayList<>();
        for (int i = 0; i < currentRide.getLatitudes().size(); i++) {
            waypoints.add(new LatLng(
                    currentRide.getLatitudes().get(i),
                    currentRide.getLongitudes().get(i)
            ));
        }

        if (waypoints.size() < 2) return;

        mapManager.drawRouteOSRM(waypoints, new MapManager.RouteCallback() {
            @Override
            public void onRouteFound(int distanceMeters, int durationSeconds) {
                totalRouteDistanceMeters = distanceMeters;

                requireActivity().runOnUiThread(() -> {
                    double durationMin = durationSeconds / 60.0;

                    tvEstimatedTime.setText(String.format(Locale.ENGLISH, "%.0f min", durationMin));
                    tvTimeRemaining.setText(String.format(Locale.ENGLISH, "%.0f min", durationMin));

                    tvEstimatedPrice.setText(String.format(Locale.ENGLISH, "%.2f RSD",
                            currentRide.getEstimatedPrice()));
                    progressBar.setProgress(0);
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Route error: " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });

        for (int i = 0; i < waypoints.size(); i++) {
            mapManager.addWaypointMarker(waypoints.get(i), i, currentRide.getAddresses().get(i));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : waypoints) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 = padding u px


//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 13f));
    }

    private void cancelRide() {
        if (currentRide == null) return;

        // Debug: Log current ride details
        Log.d(TAG, "Current ride ID: " + currentRide.getRideId());
        Log.d(TAG, "Scheduled time: " + currentRide.getScheduledTime());
        Log.d(TAG, "Current time: " + LocalDateTime.now());

        // Check scheduled time - use startingTime if scheduledTime doesn't exist
        LocalDateTime scheduled = currentRide.getScheduledTime();

        if (scheduled == null) {
            Toast.makeText(requireContext(), "Cannot cancel: ride already started or not scheduled", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "No scheduled or starting time found");
            return;
        }

        long minutesUntilStart;
        try {
            minutesUntilStart = Duration.between(LocalDateTime.now(), scheduled).toMinutes();
            Log.d(TAG, "Minutes until start: " + minutesUntilStart);
        } catch (Exception ex) {
            Log.e(TAG, "Error calculating time difference", ex);
            minutesUntilStart = Long.MIN_VALUE;
        }

        if (minutesUntilStart < 10) {
            String msg = String.format(Locale.ENGLISH,
                "Cannot cancel: only %d minutes until start (min 10 required)", minutesUntilStart);
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            Log.w(TAG, msg);
            return;
        }

        // Ask for reason
        EditText input = new EditText(requireContext());
        input.setHint("Reason for cancellation (required)");

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel ride")
                .setMessage("Please provide a reason for cancelling the ride:")
                .setView(input)
                .setPositiveButton("Cancel ride", (dialog, which) -> {
                    String reason = input.getText() != null ? input.getText().toString().trim() : "";
                    if (reason.isEmpty()) reason = "Passenger cancelled";

                    btnCancelRide.setEnabled(false);

                    // Call passenger cancel endpoint
                    rideApiService.cancelRideByPassenger(currentRide.getRideId(),
                            new com.example.getgo.dtos.ride.CancelRideRequestDTO(reason))
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    requireActivity().runOnUiThread(() -> {
                                        btnCancelRide.setEnabled(true);
                                        if (response.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Ride cancelled", Toast.LENGTH_SHORT).show();
                                            currentRide = null;
                                            showNoRide();
                                            if (mapManager != null) mapManager.reset();
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    requireActivity().runOnUiThread(() -> {
                                        btnCancelRide.setEnabled(true);
                                        Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });

                })
                .setNegativeButton("Dismiss", (d, w) -> {
                    // do nothing
                })
                .show();
    }

    private void triggerPanic() {
        if (currentRide == null) {
            Toast.makeText(requireContext(), "No active ride", Toast.LENGTH_SHORT).show();
            return;
        }

        if (panicSent) {
            Toast.makeText(requireContext(), "Emergency alert already sent", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to trigger a panic alert? This will notify authorities.")
                .setPositiveButton("Yes, Send Alert", (dialog, which) -> {
                    btnPanic.setEnabled(false);

                    rideApiService.triggerPanic(currentRide.getRideId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            requireActivity().runOnUiThread(() -> {
                                btnPanic.setEnabled(true);
                                if (response.isSuccessful()) {
                                    panicSent = true;
                                    Toast.makeText(requireContext(), "Emergency alert sent!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to send panic alert", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Failed to trigger panic", t);
                            requireActivity().runOnUiThread(() -> {
                                btnPanic.setEnabled(true);
                                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleFinishedRideOkClick() {
        currentRide = null;
        panicSent = false;

        if (mapManager != null) {
            mapManager.reset();
        }

        showNoRide();
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutNoRide.setVisibility(View.GONE);
        layoutRideTracking.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.GONE);
    }

    private void showNoRide() {
        layoutLoading.setVisibility(View.GONE);
        layoutNoRide.setVisibility(View.VISIBLE);
        layoutRideTracking.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.GONE);
    }

    private void showRideTracking() {
        layoutLoading.setVisibility(View.GONE);
        layoutNoRide.setVisibility(View.GONE);
        layoutRideTracking.setVisibility(View.VISIBLE);
        layoutRideCompleted.setVisibility(View.GONE);
    }

    private void showRideCompleted(GetRideFinishedDTO finished) {
        layoutLoading.setVisibility(View.GONE);
        layoutNoRide.setVisibility(View.GONE);
        layoutRideTracking.setVisibility(View.GONE);
        layoutRideCompleted.setVisibility(View.VISIBLE);

        tvFinalPrice.setText(String.format(Locale.ENGLISH, "%.2f RSD", finished.getPrice()));
        tvDuration.setText(String.format(Locale.ENGLISH, "%d minutes", estimatedTime));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        tvStartTime.setText(finished.getStartTime().format(timeFormatter));
        tvEndTime.setText(finished.getEndTime().format(timeFormatter));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }

    private void showRideFinishedNotification(GetRideFinishedDTO finished) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(NOTIF_TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.");
                return;
            }
        }

        Log.d(NOTIF_TAG, "Preparing ride finished notification for rideId = " + finished.getRideId());

        if (finished.getRideId() == null) {
            Log.e(NOTIF_TAG, "RideId is null! This should never happen.");
            return;
        }
        // Create notification channel (only once)
        String channelId = "ride_channel";
        String channelName = "Ride Notifications";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("OPEN_RATE_FRAGMENT", true);
        intent.putExtra("RIDE_ID", finished.getRideId());
        intent.putExtra("driverId", finished.getDriverId());
        if (finished.getRideId() == null) {
            Log.e(TAG, "RideId is null! This should never happen.");
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int requestCode = finished.getRideId().intValue();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("Ride Finished")
                .setContentText(String.format(Locale.ENGLISH,
                        "Your ride has finished. Price: %.2f RSD. Tap to rate your driver!", finished.getPrice()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        Log.d("NOTIF_TEST", "Ride finished notification intent prepared, rideId=" + finished.getRideId());
        notificationManager.notify(1001, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1002); // request code
            }
        }
    }

}