package com.example.getgo.fragments.passengers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.activities.MainActivity;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.api.services.UserApiService;
import com.example.getgo.api.services.VehicleApiService;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.driver.GetDriverLocationDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetFavoriteRideDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.model.UserProfile;
import com.example.getgo.repositories.DriverRepository;
import com.example.getgo.repositories.RideRepository;
import com.example.getgo.utils.JwtUtils;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapManager mapManager;
    private VehicleApiService vehicleApiService;
    private RideApiService rideApiService;

    private TextInputEditText etStartPoint, etDestination;
    private LinearLayout layoutWaypoints, layoutFriendEmailsList, layoutScheduledTime, layoutFriendEmails;
    private AutoCompleteTextView actvVehicleType, actvOrderTiming, actvTravelOption;
    private CheckBox cbHasBaby, cbHasPets;
    private TextInputEditText etScheduledTime;
    private Button btnOrderRide, btnCancel, btnAddWaypoint, btnRemoveWaypoint, btnAddFriendEmail, btnRemoveFriendEmail;

    private MaterialButton btnToggleFavorites;
    private MaterialCardView cvFavoritesContainer;
    private LinearLayout layoutFavoritesList;
    private boolean showFavorites = false;
    private List<GetFavoriteRideDTO> favoriteRides = new ArrayList<>();

    private RideRepository rideRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    private List<TextInputEditText> waypointInputs = new ArrayList<>();
    private List<LatLng> waypointCoords = new ArrayList<>();
    private List<TextInputEditText> friendEmailInputs = new ArrayList<>();

    private LatLng startPointCoord = null;
    private LatLng destinationCoord = null;

    private Integer activeInputIndex = null;

    private UserApiService userApiService;
    private Long passengerId;

    private WebSocketManager webSocketManager;
    private final Map<Long, Marker> driverMarkers = new HashMap<>();


    public PassengerHomeFragment() {}

    public static PassengerHomeFragment newInstance() {
        return new PassengerHomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vehicleApiService = ApiClient.getClient().create(VehicleApiService.class);
        rideApiService = ApiClient.getClient().create(RideApiService.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_passenger_home, container, false);

        rideRepository = RideRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews(root);
        setupMap();
        setupDropdowns();
        setupListeners();
        loadFavoriteRides();

        // Check for re-order data
        if (getArguments() != null && getArguments().containsKey("REORDER_RIDE")) {
            GetRideDTO reorderRide = (GetRideDTO) getArguments().getSerializable("REORDER_RIDE");
            prefillRideData(reorderRide);
        }

        userApiService = ApiClient.getUserApiService();
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("getgo_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("jwt_token", null);

        Long userIdFromToken = JwtUtils.getUserIdFromToken(token);

        Log.d("PassengerHome", "UserId from token: " + userIdFromToken);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
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

        btnToggleFavorites = root.findViewById(R.id.btnToggleFavorites);
        cvFavoritesContainer = root.findViewById(R.id.cvFavoritesContainer);
        layoutFavoritesList = root.findViewById(R.id.layoutFavoritesList);
    }

    private void setupDropdowns() {
        loadVehicleTypes();

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

    private void loadVehicleTypes() {
        executor.execute(() -> {
            try {
                VehicleApiService vehicleApi = ApiClient.getClient().create(VehicleApiService.class);
                retrofit2.Response<List<String>> response = vehicleApi.getVehicleTypes().execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<String> types = new ArrayList<>();
                    types.add(0, "ANY");
                    types.addAll(response.body());

                    mainHandler.post(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                types
                        );
                        actvVehicleType.setAdapter(adapter);
                        actvVehicleType.setText("ANY", false);
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(requireContext(),
                        "Failed to load vehicle types", Toast.LENGTH_SHORT).show());
            }
        });
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

        btnToggleFavorites.setOnClickListener(v -> toggleFavorites());
    }

    private void loadFavoriteRides() {
        executor.execute(() -> {
            try {
                List<GetFavoriteRideDTO> favorites = rideRepository.getFavoriteRides();

                mainHandler.post(() -> {
                    favoriteRides = favorites;
                    btnToggleFavorites.setText(favorites.isEmpty()
                            ? "Favorites"
                            : String.format(Locale.ENGLISH, "Favorites (%d)", favorites.size()));
                });
            } catch (Exception e) {
                Log.e("PassengerHome", "Failed to load favorites", e);
            }
        });
    }

    private void toggleFavorites() {
        showFavorites = !showFavorites;

        if (showFavorites) {
            btnToggleFavorites.setText("Hide");
            cvFavoritesContainer.setVisibility(View.VISIBLE);
            populateFavoritesList();
        } else {
            btnToggleFavorites.setText(favoriteRides.isEmpty()
                    ? "Favorites"
                    : String.format(Locale.ENGLISH, "Favorites (%d)", favoriteRides.size()));
            cvFavoritesContainer.setVisibility(View.GONE);
        }
    }

    private void populateFavoritesList() {
        layoutFavoritesList.removeAllViews();

        if (favoriteRides.isEmpty()) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("No favorite rides yet");
            tvEmpty.setTextColor(0xFF133E87);
            tvEmpty.setPadding(16, 16, 16, 16);
            layoutFavoritesList.addView(tvEmpty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (GetFavoriteRideDTO favorite : favoriteRides) {
            View itemView = inflater.inflate(R.layout.item_favorite_ride, layoutFavoritesList, false);

            TextView tvStart = itemView.findViewById(R.id.tvFavStart);
            TextView tvDest = itemView.findViewById(R.id.tvFavDestination);
            TextView tvBadges = itemView.findViewById(R.id.tvFavBadges);

            List<String> addresses = favorite.getAddresses();
            tvStart.setText("Start: " + addresses.get(0));
            tvDest.setText("Dest: " + addresses.get(addresses.size() - 1));

            StringBuilder badges = new StringBuilder();
            if (favorite.isNeedsBabySeats()) badges.append("BABY  ");
            if (favorite.isNeedsPetFriendly()) badges.append("PETS  ");
            if (favorite.getVehicleType() != null && !favorite.getVehicleType().equals("ANY")) {
                badges.append(favorite.getVehicleType());
            }
            tvBadges.setText(badges.toString().trim());
            tvBadges.setVisibility(badges.length() > 0 ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> loadFavoriteRide(favorite));
            layoutFavoritesList.addView(itemView);
        }
    }

    private void loadFavoriteRide(GetFavoriteRideDTO favorite) {
        resetForm();

        List<String> addresses = favorite.getAddresses();
        List<Double> lats = favorite.getLatitudes();
        List<Double> lngs = favorite.getLongitudes();

        // Set start point with marker
        etStartPoint.setText(addresses.get(0));
        startPointCoord = new LatLng(lats.get(0), lngs.get(0));
        mapManager.addWaypointMarker(startPointCoord, 0, "Start Point");

        // Set destination with marker
        etDestination.setText(addresses.get(addresses.size() - 1));
        destinationCoord = new LatLng(lats.get(lats.size() - 1), lngs.get(lngs.size() - 1));
        mapManager.addWaypointMarker(destinationCoord, 100, "Destination");

        // Add waypoints (intermediate points between start and destination)
        for (int i = 1; i < addresses.size() - 1; i++) {
            addWaypoint();
            int waypointIndex = i - 1;
            waypointInputs.get(waypointIndex).setText(addresses.get(i));
            LatLng waypointCoord = new LatLng(lats.get(i), lngs.get(i));
            waypointCoords.set(waypointIndex, waypointCoord);
            mapManager.addWaypointMarker(waypointCoord, waypointIndex + 1, "Waypoint " + (waypointIndex + 1));
        }

        // Set vehicle type
        String vehicleType = favorite.getVehicleType();
        if (vehicleType != null && !vehicleType.equals("ANY")) {
            actvVehicleType.setText(vehicleType, false);
        }

        // Set preferences
        cbHasBaby.setChecked(favorite.isNeedsBabySeats());
        cbHasPets.setChecked(favorite.isNeedsPetFriendly());

        // Set friend emails if any
        List<String> emails = favorite.getLinkedPassengerEmails();
        if (emails != null && !emails.isEmpty()) {
            actvTravelOption.setText("With friends", false);
            layoutFriendEmails.setVisibility(View.VISIBLE);
            for (String email : emails) {
                addFriendEmail();
                friendEmailInputs.get(friendEmailInputs.size() - 1).setText(email);
            }
        }

        // Draw the complete route on the map
        drawRouteIfReady();

        // Hide favorites panel
        showFavorites = false;
        cvFavoritesContainer.setVisibility(View.GONE);
        btnToggleFavorites.setText(String.format(Locale.ENGLISH, "Favorites (%d)", favoriteRides.size()));

        ToastHelper.showShort(requireContext(), "Favorite ride loaded");
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
        subscribeToDriverLocationUpdates();

        mMap.setOnMapClickListener(this::handleMapClick);
    }

    private void subscribeToDriverLocationUpdates() {

        webSocketManager.subscribeToAllDriversLocations(location -> {

            if (!isAdded() || getActivity() == null) return;

            getActivity().runOnUiThread(() -> {

                if (!isAdded() || getContext() == null || mMap == null) return;

                Long driverId = location.getDriverId();
                LatLng newPosition =
                        new LatLng(location.getLatitude(), location.getLongitude());

                Marker marker = driverMarkers.get(driverId);

                if (marker != null) {

                    marker.setPosition(newPosition);

                    marker.setIcon(bitmapDescriptorFromVector(
                            getContext(),   // više ne koristimo requireContext()
                            "".equals(location.getStatus())
                                    ? R.drawable.ic_car_green
                                    : R.drawable.ic_car_red,
                            120, 120
                    ));

                } else {

                    Marker newMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(newPosition)
                                    .icon(bitmapDescriptorFromVector(
                                            getContext(),
                                            "".equals(location.getStatus())
                                                    ? R.drawable.ic_car_green
                                                    : R.drawable.ic_car_red,
                                            120, 120
                                    ))
                    );

                    driverMarkers.put(driverId, newMarker);
                }
            });
        });
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
            mapManager.addWaypointMarker(coordinates, activeInputIndex + 1, "Waypoint " + (activeInputIndex + 1));
        }
    }

    private void drawRouteIfReady() {
        List<LatLng> allPoints = new ArrayList<>();

        if (startPointCoord != null) allPoints.add(startPointCoord);
        for (LatLng waypoint : waypointCoords) {
            if (waypoint != null) allPoints.add(waypoint);
        }
        if (destinationCoord != null) allPoints.add(destinationCoord);

        if (allPoints.size() < 2) return;

        mapManager.drawRouteOSRM(allPoints, null);
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
            String time = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
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
                ToastHelper.showError(requireContext(), "Failed to geocode", error);
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
                ToastHelper.showError(requireContext(), "Failed to geocode waypoint", error);
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

        String vehicleType = actvVehicleType.getText().toString();
        if ("ANY".equals(vehicleType)) vehicleType = "";

        CreateRideRequestDTO request = new CreateRideRequestDTO(
                lats,
                lngs,
                addrs,
                isOrderLater() ? etScheduledTime.getText().toString() : null,
                isWithFriends() ? getFriendEmails() : null,
                cbHasBaby.isChecked(),
                cbHasPets.isChecked(),
                vehicleType
        );

        btnOrderRide.setEnabled(false);

        new Thread(() -> {
            try {
                CreatedRideResponseDTO response = rideRepository.orderRide(request);

                requireActivity().runOnUiThread(() -> {
                    btnOrderRide.setEnabled(true);
                    if ("blocked".equals(response.getStatus())) {
                        ToastHelper.showShort(requireContext(), response.getMessage() != null ? response.getMessage() : "Blocked");
                    } else if ("SUCCESS".equals(response.getStatus())) {
                        ToastHelper.showShort(requireContext(), "Ride ordered");
                        resetForm();
                    } else {
                        ToastHelper.showShort(requireContext(), response.getMessage() != null ? response.getMessage() : "Order failed");
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    btnOrderRide.setEnabled(true);
                    ToastHelper.showError(requireContext(), "Failed to order ride", e.getMessage());
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

        ArrayAdapter<String> timingAdapter = (ArrayAdapter<String>) actvOrderTiming.getAdapter();
        ArrayAdapter<String> travelAdapter = (ArrayAdapter<String>) actvTravelOption.getAdapter();
        actvVehicleType.setText("ANY", false);
        actvOrderTiming.setText(timingAdapter.getItem(0), false);
        actvTravelOption.setText(travelAdapter.getItem(0), false);

        cbHasBaby.setChecked(false);
        cbHasPets.setChecked(false);

        etScheduledTime.setText("");

        layoutScheduledTime.setVisibility(View.GONE);
        layoutFriendEmails.setVisibility(View.GONE);

        activeInputIndex = null;

        if (mapManager != null) {
            mapManager.clearWaypoints();
            mapManager.clearRoute();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webSocketManager = new WebSocketManager();
        webSocketManager.connect();

        fetchLoggedInUserIdAndSubscribe();
//        subscribeToAllDriversLiveUpdates();
    }

    private void subscribeToAllDriversLiveUpdates() {
        webSocketManager.subscribeToAllDriversLocations(driverLocation -> {
            mainHandler.post(() -> updateDriverMarker(driverLocation));
        });
    }

    private void updateDriverMarker(GetDriverLocationDTO driverLocation) {
        Long driverId = driverLocation.getDriverId();
        LatLng latLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());

        Marker marker = driverMarkers.get(driverId);

        if (marker != null) {
            // Ako postoji, samo ažuriraj lokaciju
            marker.setPosition(latLng);

            // Ako marker ne postoji, dodaj novi
//            marker = mMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .title("Driver " + driverId)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//            );
//            driverMarkers.put(driverId, marker);
        } else {
            // Ako marker ne postoji, dodaj novi
//            marker = mMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .title("Driver " + driverId)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//            );
//            driverMarkers.put(driverId, marker);
        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
        driverMarkers.clear();
    }

    private void showRideTrackingNotification(Long rideId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
                return;
            }
        }

        String channelId = "ride_channel";
        String channelName = "Ride Notifications";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications about rides");
            NotificationManager manager = requireContext()
                    .getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("OPEN_RIDE_TRACKING_FRAGMENT", true);
        intent.putExtra("RIDE_ID", rideId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                rideId.intValue(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(requireContext(), channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Your ride is active!")
                        .setContentText("Tap to track your ride")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(requireContext())
                .notify(rideId.intValue(), builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("PassengerHome", "Notification permission granted");
            } else {
                Log.d("PassengerHome", "Notification permission denied");
            }
        }
    }
//    private void fetchLoggedInUserIdAndSubscribe() {
//        userApiService.getUserProfile().enqueue(new Callback<UserProfile>() {
//            @Override
//            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    passengerId = response.body().getId();
//                    Log.d("PassengerHome", "Fetched userId: " + passengerId);
//                    subscribeToLinkedRideAccepted(passengerId);
//                    Log.d("PassengerHome", "Subscribing to linked ride accepted");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<UserProfile> call, Throwable t) {
//                Log.e("PassengerHome", "Failed to fetch user profile", t);
//            }
//        });
//    }

    private void fetchLoggedInUserIdAndSubscribe() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("getgo_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("jwt_token", null);
        if (token == null) {
            Log.e("PassengerHome", "JWT token not found");
            return;
        }

        passengerId = JwtUtils.getUserIdFromToken(token);

        if (passengerId != null) {
            Log.d("PassengerHome", "UserId from token in fetchLoggedInUser: " + passengerId);
            subscribeToLinkedRideAccepted(passengerId);
        } else {
            Log.e("PassengerHome", "Failed to extract userId from token");
        }
    }

    private void subscribeToLinkedRideAccepted(Long passengerId) {
        if (passengerId == null) return;

        webSocketManager.setLinkedRideAcceptedListener(linkedRide -> {
            Log.d("PassengerHome", "Received linked ride accepted WS event for rideId: " + linkedRide.getRideId());
            mainHandler.post(() -> showRideTrackingNotification(linkedRide.getRideId()));
        });

        Log.d("PassengerHome", "Calling WebSocketManager subscribe for passengerId: " + passengerId);
        webSocketManager.subscribeToLinkedRideAccepted(passengerId);
    }



}
