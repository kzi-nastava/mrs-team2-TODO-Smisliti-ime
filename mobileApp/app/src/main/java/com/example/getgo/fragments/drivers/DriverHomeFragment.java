package com.example.getgo.fragments.drivers;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.getgo.dtos.ride.UpdatedRideDTO;
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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverHomeFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "DriverHomeFragment";
    private static final String PREFS_NAME = "getgo_prefs";

    private GoogleMap mMap;
    private MapManager mapManager;
    private WebSocketManager webSocketManager;

    private LinearLayout layoutNoRide, layoutRideInfo, layoutRideCompleted, layoutScheduledTime;
    private TextView tvRideId, tvRideTitle, tvStatus, tvStartPoint, tvDestination, tvPassengerInfo, tvPassengerCount;
    private TextView tvEstimatedTime, tvEstimatedPrice, tvScheduledTime;
    private TextView tvFinalPrice, tvDuration;
    private Button btnPrimaryAction, btnSecondaryAction, btnOk;

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

        btnPrimaryAction.setOnClickListener(v -> handlePrimaryAction());
        btnSecondaryAction.setOnClickListener(v -> handleSecondaryAction());
        btnOk.setOnClickListener(v -> handleOkClick());
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
                    currentRide = ride;
                    updateUI();
                    if (ride != null) {
                        drawRideRoute();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load active ride", e);
                requireActivity().runOnUiThread(this::showNoRide);
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

//    private void drawRideRoute() {
//        Log.d("ROUTE_DEBUG", "Latitudes size: " + currentRide.getLatitudes().size());
//        Log.d("ROUTE_DEBUG", "Longitudes size: " + currentRide.getLongitudes().size());
//        if (currentRide == null || mapManager == null) return;
//
//        List<LatLng> waypoints = new ArrayList<>();
//        for (int i = 0; i < currentRide.getLatitudes().size(); i++) {
//            waypoints.add(new LatLng(
//                    currentRide.getLatitudes().get(i),
//                    currentRide.getLongitudes().get(i)
//            ));
//        }
//
//        if (!waypoints.isEmpty()) {
//            mapManager.drawRoute(waypoints, null);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 13f));
//        }
//    }
//    private void drawRideRoute() {
//        if (currentRide == null) return;
//
//        if (mapManager == null || mMap == null) {
//            pendingDrawRoute = true;
//            return;
//        }
//
//        if (currentRide.getLatitudes() == null ||
//                currentRide.getLongitudes() == null ||
//                currentRide.getLatitudes().size() < 2) {
//            Log.w("ROUTE_DEBUG", "Not enough points to draw route");
//            return;
//        }
//
//        List<LatLng> waypoints = new ArrayList<>();
//        for (int i = 0; i < currentRide.getLatitudes().size(); i++) {
//            waypoints.add(new LatLng(
//                    currentRide.getLatitudes().get(i),
//                    currentRide.getLongitudes().get(i)
//            ));
//        }
//
//        mMap.clear();
//
//        mMap.addMarker(new MarkerOptions().position(waypoints.get(0)).title("Start"));
//        mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoints.size() - 1)).title("Destination"));
//
////        mapManager.drawRoute(waypoints, null);
////        mMap.animateCamera(
////                CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 13f)
////        );
////
////        pendingDrawRoute = false;
//
//        mapManager.drawRoute(waypoints, new MapManager.RouteCallback() {
//            @Override
//            public void onRouteFound(int distanceMeters, int durationSeconds) {
//                // Kada ruta stigne, obuhvati kamerom celu rutu
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                for (LatLng point : waypoints) {
//                    builder.include(point);
//                }
//                LatLngBounds bounds = builder.build();
//                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
//            }
//
//            @Override
//            public void onError(String error) {
//                Log.e("ROUTE_DEBUG", "Route error: " + error);
//            }
//        });
//
//        pendingDrawRoute = false;
//    }

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

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(waypoints.get(0), 13f));

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
                // TODO: Implement stop ride
                Toast.makeText(requireContext(), "Stop ride not yet implemented", Toast.LENGTH_SHORT).show();
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
            cancelRide();
        }
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

    private void cancelRide() {
        // TODO: Show dialog to enter cancellation reason
        btnSecondaryAction.setEnabled(false);

        new Thread(() -> {
            try {
                RideRepository repo = RideRepository.getInstance();
                repo.cancelRide(currentRide.getRideId(), "Driver cancelled");

                requireActivity().runOnUiThread(() -> {
                    btnSecondaryAction.setEnabled(true);
                    currentRide = null;
                    showNoRide();
                    Toast.makeText(requireContext(), "Ride cancelled", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to cancel ride", e);
                requireActivity().runOnUiThread(() -> {
                    btnSecondaryAction.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to cancel ride", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}