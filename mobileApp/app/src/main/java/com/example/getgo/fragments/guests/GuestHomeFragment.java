package com.example.getgo.fragments.guests;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.api.services.VehicleApiService;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.ride.CreateRideEstimateDTO;
import com.example.getgo.dtos.ride.CreatedRideEstimateDTO;
import com.example.getgo.dtos.vehicle.GetVehicleDTO;
import com.example.getgo.repositories.DriverRepository;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.ToastHelper;
import com.example.getgo.utils.WebSocketManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapManager mapManager;
    private VehicleApiService vehicleApiService;
    private RideApiService rideApiService;
    private MaterialButton btnZoomIn, btnZoomOut, btnEstimate;
    private TextInputEditText etPickupAddress, etDropoffAddress;
    private LinearLayout estimateResultLayout;
    private TextView tvEstimatedDistance, tvEstimatedTime;

    private LatLng pickupCoord = null;
    private LatLng dropoffCoord = null;
    private Integer activeInputIndex = null; // -1 for pickup, -2 for dropoff

    private final Map<Long, Marker> driverMarkers = new HashMap<>();
    private WebSocketManager webSocketManager;

    public GuestHomeFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webSocketManager = new WebSocketManager();
        webSocketManager.connect();

        vehicleApiService = ApiClient.getClient().create(VehicleApiService.class);
        rideApiService = ApiClient.getClient().create(RideApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_guest_home, container, false);

        try {
            // Initialize views
            btnZoomIn = root.findViewById(R.id.btn_zoom_in);
            btnZoomOut = root.findViewById(R.id.btn_zoom_out);
            btnEstimate = root.findViewById(R.id.btnEstimate);
            etPickupAddress = root.findViewById(R.id.etPickupAddress);
            etDropoffAddress = root.findViewById(R.id.etDropoffAddress);
            estimateResultLayout = root.findViewById(R.id.estimateResultLayout);
            tvEstimatedDistance = root.findViewById(R.id.tvEstimatedDistance);
            tvEstimatedTime = root.findViewById(R.id.tvEstimatedTime);

            // Setup focus listeners for map interaction
            etPickupAddress.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    activeInputIndex = -1;
                    Toast.makeText(getContext(), "Tap on map to select pickup location", Toast.LENGTH_SHORT).show();
                }
            });

            etDropoffAddress.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    activeInputIndex = -2;
                    Toast.makeText(getContext(), "Tap on map to select dropoff location", Toast.LENGTH_SHORT).show();
                }
            });

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            if (btnZoomIn != null) {
                btnZoomIn.setOnClickListener(v -> {
                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    }
                });
            }

            if (btnZoomOut != null) {
                btnZoomOut.setOnClickListener(v -> {
                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.zoomOut());
                    }
                });
            }

            if (btnEstimate != null) {
                btnEstimate.setOnClickListener(v -> estimateRide());
            }

        } catch (Exception e) {
            Log.e("GuestHome", "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error initializing view: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mapManager = new MapManager(requireContext(), mMap);

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        loadActiveDrivers();
        subscribeToDriverLocationUpdates();

        // Setup map click listener for location selection
        mMap.setOnMapClickListener(this::handleMapClick);
    }

    private void subscribeToDriverLocationUpdates() {

        webSocketManager.subscribeToAllDriversLocations(location -> {

            requireActivity().runOnUiThread(() -> {

                Long driverId = location.getDriverId();
                LatLng newPosition =
                        new LatLng(location.getLatitude(), location.getLongitude());

                if (driverMarkers.containsKey(driverId)) {

                    // UPDATE - we just move the existing marker. In a real app, you might want to animate this movement.
                    driverMarkers.get(driverId).setPosition(newPosition);

                    driverMarkers.get(driverId).setIcon(bitmapDescriptorFromVector(
                            requireContext(),
                            "".equals(location.getStatus()) ? R.drawable.ic_car_green : R.drawable.ic_car_red,
                            120, 120
                    )); // IDLE
                } else {

                    // CREATE
//                    Marker marker = mMap.addMarker(new MarkerOptions()
//                            .position(newPosition)
//                            .icon(bitmapDescriptorFromVector(
//                                    requireContext(),
//                                    R.drawable.ic_car_green,
//                                    120, 120
//                            )));
//
//                    driverMarkers.put(driverId, marker);
                }
            });
        });
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
                String displayValue = String.format(Locale.ENGLISH, "%.5f, %.5f", latLng.latitude, latLng.longitude);
                setLocationForActiveInput(latLng, displayValue);
                activeInputIndex = null;
                Toast.makeText(requireContext(), "Location set (geocoding failed)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocationForActiveInput(LatLng coordinates, String displayText) {
        if (activeInputIndex == -1) {
            pickupCoord = coordinates;
            etPickupAddress.setText(displayText);
            etPickupAddress.clearFocus();
            mapManager.addWaypointMarker(coordinates, 0, "Pickup");
        } else if (activeInputIndex == -2) {
            dropoffCoord = coordinates;
            etDropoffAddress.setText(displayText);
            etDropoffAddress.clearFocus();
            mapManager.addWaypointMarker(coordinates, 100, "Dropoff");
        }
    }

    private void drawRouteIfReady() {
        if (pickupCoord != null && dropoffCoord != null) {
            List<LatLng> points = new ArrayList<>();
            points.add(pickupCoord);
            points.add(dropoffCoord);
            mapManager.drawRouteOSRM(points, null);
        }
    }

    private void loadActiveDrivers() {
        new Thread(() -> {
            try {
                DriverRepository repo = DriverRepository.getInstance();
                List<GetActiveDriverLocationDTO> drivers = repo.getActiveDriverLocations();

                requireActivity().runOnUiThread(() -> {
                    showDriversOnMap(drivers);
                    Log.d("GuestHome", "Loaded " + drivers.size() + " active drivers");
                });
            } catch (Exception e) {
                Log.e("GuestHome", "Failed to load active drivers", e);
            }
        }).start();
    }

    private void showDriversOnMap(List<GetActiveDriverLocationDTO> drivers) {
        if (mMap == null) return;

        for (GetActiveDriverLocationDTO d : drivers) {
            if (d.getLatitude() == null || d.getLongitude() == null) continue;

            LatLng position = new LatLng(d.getLatitude(), d.getLongitude());

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(d.getVehicleType())
                    .snippet("Status: " + (d.getIsAvailable() ? "Free" : "Occupied"))
                    .icon(bitmapDescriptorFromVector(
                            getContext(),
                            d.getIsAvailable() ? R.drawable.ic_car_green : R.drawable.ic_car_red,
                            120, 120
                    )));

            driverMarkers.put(d.getDriverId(), marker);
        }
    }


    private BitmapDescriptor bitmapDescriptorFromVector(@NonNull Context context, int vectorResId, int width, int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void estimateRide() {
        String pickup = etPickupAddress.getText() != null ?
                        etPickupAddress.getText().toString().trim() : "";
        String dropoff = etDropoffAddress.getText() != null ?
                         etDropoffAddress.getText().toString().trim() : "";

        if (pickup.isEmpty()) {
            etPickupAddress.setError("Pickup address is required");
            return;
        }
        if (dropoff.isEmpty()) {
            etDropoffAddress.setError("Dropoff address is required");
            return;
        }

        // Geocode if coordinates not set
        if (pickupCoord == null) {
            geocodeAndEstimate(pickup, true);
            return;
        }
        if (dropoffCoord == null) {
            geocodeAndEstimate(dropoff, false);
            return;
        }

        // Both coordinates ready, submit estimate
        submitEstimateRequest();
    }

    private void geocodeAndEstimate(String address, boolean isPickup) {
        mapManager.getCoordinatesFromAddress(address, new MapManager.CoordinatesCallback() {
            @Override
            public void onCoordinatesFound(LatLng coordinates) {
                if (isPickup) {
                    pickupCoord = coordinates;
                    mapManager.addWaypointMarker(coordinates, 0, "Pickup");
                } else {
                    dropoffCoord = coordinates;
                    mapManager.addWaypointMarker(coordinates, 100, "Dropoff");
                }
                drawRouteIfReady();
                estimateRide(); // Retry
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to geocode: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitEstimateRequest() {
        // Build coordinate list
        List<CreateRideEstimateDTO.Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new CreateRideEstimateDTO.Coordinate(pickupCoord.latitude, pickupCoord.longitude));
        coordinates.add(new CreateRideEstimateDTO.Coordinate(dropoffCoord.latitude, dropoffCoord.longitude));

        CreateRideEstimateDTO request = new CreateRideEstimateDTO(coordinates);

        btnEstimate.setEnabled(false);

        rideApiService.estimateRide(request).enqueue(new Callback<CreatedRideEstimateDTO>() {
            @Override
            public void onResponse(Call<CreatedRideEstimateDTO> call, Response<CreatedRideEstimateDTO> response) {
                btnEstimate.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    displayEstimateResult(response.body());
                } else {
                    String errorMsg = "Failed to get estimate";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("GuestHome", "Error reading error body", e);
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("GuestHome", "Estimate failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreatedRideEstimateDTO> call, Throwable t) {
                btnEstimate.setEnabled(true);
                Log.e("GuestHome", "Estimate failed", t);
                ToastHelper.showError(getContext(), "Failed to estimate ride", t.getMessage());
            }
        });
    }

    private void displayEstimateResult(CreatedRideEstimateDTO estimate) {
        if (estimateResultLayout == null || tvEstimatedDistance == null || tvEstimatedTime == null) {
            Log.e("GuestHome", "Result views not initialized");
            return;
        }

        estimateResultLayout.setVisibility(View.VISIBLE);

        if (estimate.getDistanceKm() != null) {
            tvEstimatedDistance.setText(String.format(Locale.ENGLISH, "%.2f km", estimate.getDistanceKm()));
        }
        if (estimate.getDurationMinutes() != null) {
            tvEstimatedTime.setText(String.format(Locale.ENGLISH, "%d min", estimate.getDurationMinutes()));
        }
    }

}
