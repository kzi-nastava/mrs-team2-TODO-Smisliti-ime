package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import com.example.getgo.repositories.AdminRepository;
import com.example.getgo.utils.MapManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class AdminActiveRideDetailsFragment extends Fragment {
    private long rideId;
    private MapManager mapManager;
    private TextView tvDriverName, tvDriverEmail, tvStatus, tvVehicleType,
            tvScheduledTime, tvActualStartTime, tvEstimatedDuration, tvEstimatedPrice,
            tvCurrentAddress;

    public AdminActiveRideDetailsFragment() {
        // Required empty public constructor
    }

    public static AdminActiveRideDetailsFragment newInstance(String param1, String param2) {
        AdminActiveRideDetailsFragment fragment = new AdminActiveRideDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_active_ride_details, container, false);

        if (getArguments() == null) {
            Log.e("AdminRideDetails", " getArguments() == null");
            return view;
        }

        rideId = getArguments().getLong("rideId", -1);
        Log.d("AdminRideDetails", " rideId received = " + rideId);

        if (rideId == -1) {
            Log.e("AdminRideDetails", " rideId NOT FOUND in bundle");
        }

        tvDriverName = view.findViewById(R.id.tvDriverName);
        tvDriverEmail = view.findViewById(R.id.tvDriverEmail);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvVehicleType = view.findViewById(R.id.tvVehicleType);
        tvScheduledTime = view.findViewById(R.id.tvScheduledTime);
        tvActualStartTime = view.findViewById(R.id.tvActualStartTime);
        tvEstimatedDuration = view.findViewById(R.id.tvEstimatedDuration);
        tvEstimatedPrice = view.findViewById(R.id.tvEstimatedPrice);
        tvCurrentAddress = view.findViewById(R.id.tvCurrentAddress);

        rideId = getArguments().getLong("rideId");

        setupMap();

        Log.d("AdminRideDetails", "Prosao sam setupMap");

        return view;
    }

    private void setupMap() {
        Log.d("AdminRideDetails", " usao sam u setup mape ");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapContainer);

        Log.d("AdminRideDetails", " drugi deo setup mape ");

        mapFragment.getMapAsync(googleMap -> {
            mapManager = new MapManager(requireContext(), googleMap);
            loadRideDetails();
        });
    }


    private void loadRideDetails() {
        new Thread(() -> {
            try {
                Log.d("AdminRideDetails", " Calling API for rideId = " + rideId);

                GetActiveRideAdminDetailsDTO rideDetails = AdminRepository.getInstance().getActiveRideDetails((int) rideId);

                Log.d("AdminRideDetails", "API SUCCESS for rideId = " + rideId);

                requireActivity().runOnUiThread(() -> {
                    tvDriverName.setText("🚗 " + rideDetails.getDriverName());
                    tvDriverEmail.setText(rideDetails.getDriverEmail());
                    tvStatus.setText("Status: " + rideDetails.getStatus());
                    tvVehicleType.setText("Vehicle: " + rideDetails.getVehicleType());
                    tvScheduledTime.setText("Scheduled Start: " + rideDetails.getScheduledTime());
                    tvActualStartTime.setText("Actual Start: " + rideDetails.getActualStartTime());
                    tvEstimatedDuration.setText("Estimated Duration: " + rideDetails.getEstimatedDurationMin() + " min");
                    tvEstimatedPrice.setText("Estimated Price: " + rideDetails.getEstimatedPrice() + " RSD");
                    tvCurrentAddress.setText("Current Address: " + rideDetails.getCurrentAddress());

                    if (rideDetails.getLatitudes() != null && rideDetails.getLongitudes() != null
                            && rideDetails.getLatitudes().size() == rideDetails.getLongitudes().size()) {

                        List<LatLng> routePoints = new ArrayList<>();
                        for (int i = 0; i < rideDetails.getLatitudes().size(); i++) {
                            routePoints.add(new LatLng(
                                    rideDetails.getLatitudes().get(i),
                                    rideDetails.getLongitudes().get(i)
                            ));
                        }

                        mapManager.drawRouteOSRM(routePoints, new MapManager.RouteCallback() {
                            @Override
                            public void onRouteFound(int distanceMeters, int durationSeconds) {
                                Log.d("AdminRideDetails", "OSRM Route drawn. Distance=" + distanceMeters + "m, Duration=" + durationSeconds + "s");
                                if (routePoints.size() > 0) {
                                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                                    for (LatLng point : routePoints) {
                                        boundsBuilder.include(point);
                                    }
                                    LatLngBounds bounds = boundsBuilder.build();
                                    mapManager.getGoogleMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)
                                    );
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("AdminRideDetails", "OSRM Route error: " + error);
                            }
                        });


                        mapManager.updateDriverPosition(
                                new LatLng(rideDetails.getCurrentLat(), rideDetails.getCurrentLng())
                        );
                    }
                });
            } catch (Exception e) {
                Log.e("AdminRideDetails", " Exception in loadRideDetails", e);
            }
        }).start();
    }




}