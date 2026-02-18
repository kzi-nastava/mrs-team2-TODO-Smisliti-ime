package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.repositories.RideRepository;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.RideDetailHelper;
import com.example.getgo.utils.ToastHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideDetailFragment extends Fragment {
    private static final String TAG = "PassengerRideDetail";
    private static final String ARG_RIDE = "arg_ride";

    private GetRideDTO ride;
    private GoogleMap mMap;
    private MapManager mapManager;

    private Button btnFavoriteRide, btnUnfavoriteRide;
    private PassengerApiService passengerService;
    private RideRepository rideRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    public PassengerRideDetailFragment() {}

    public static PassengerRideDetailFragment newInstance(Long rideId) {
        PassengerRideDetailFragment fragment = new PassengerRideDetailFragment();
        Bundle args = new Bundle();
        args.putLong("RIDE_ID", rideId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ride = (GetRideDTO) getArguments().getSerializable(ARG_RIDE);
        }
        passengerService = ApiClient.getClient().create(PassengerApiService.class);
        rideRepository = RideRepository.getInstance();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_ride_detail, container, false);

        btnFavoriteRide = view.findViewById(R.id.btnFavoriteRide);
        btnUnfavoriteRide = view.findViewById(R.id.btnUnfavoriteRide);

        Long rideId = getArguments() != null ? getArguments().getLong("RIDE_ID") : null;
        if (rideId == null) {
            Toast.makeText(requireContext(), "Error: No ride ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        passengerService.getRideForReorder(rideId).enqueue(new Callback<GetRideDTO>() {
            @Override
            public void onResponse(Call<GetRideDTO> call, Response<GetRideDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    ride = response.body();

                    displayBasicRideInfo(view);
                    setupMap(view);  // Crtanje mape samo nakon što je ride učitan
                    if (ride.getDriverId() != null) loadDriverInfo(view);
                    loadRatings(view);
                    loadInconsistencyReports(view, inflater);
                    setupButtons(view);
                } else {
                    Toast.makeText(requireContext(), "Failed to load ride", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetRideDTO> call, Throwable t) {
                Log.e(TAG, "Failed to load ride detail", t);
                ToastHelper.showError(requireContext(), "Failed to load ride", t.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void favoriteRide() {
        btnFavoriteRide.setEnabled(false);

        executor.execute(() -> {
            try {
                rideRepository.favoriteRide(ride.getId());
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    btnFavoriteRide.setEnabled(true);
                    Toast.makeText(requireContext(), "Ride added to favorites", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    btnFavoriteRide.setEnabled(true);
                    ToastHelper.showError(requireContext(), "Failed to add favorite", e.getMessage());
                });
            }
        });
    }

    private void unfavoriteRide() {
        btnUnfavoriteRide.setEnabled(false);

        executor.execute(() -> {
            try {
                rideRepository.unfavoriteRide(ride.getId());
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    btnUnfavoriteRide.setEnabled(true);
                    Toast.makeText(requireContext(), "Ride removed from favorites", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    btnUnfavoriteRide.setEnabled(true);
                    ToastHelper.showError(requireContext(), "Failed to remove favorite", e.getMessage());
                });
            }
        });
    }

    private void displayBasicRideInfo(View view) {
        TextView tvRideDetails = view.findViewById(R.id.tvRideDetails);
        TextView date = view.findViewById(R.id.tvDate);
        TextView start = view.findViewById(R.id.tvStartLocation);
        TextView end = view.findViewById(R.id.tvEndLocation);
        TextView startTime = view.findViewById(R.id.tvStartTime);
        TextView endTime = view.findViewById(R.id.tvEndTime);
        TextView tvPanicActivated = view.findViewById(R.id.tvPanicActivated);
        TextView price = view.findViewById(R.id.tvPrice);
        TextView tvPassengers = view.findViewById(R.id.tvPassengers);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        tvRideDetails.setText(getString(R.string.ride_details));

        if (ride.getStartingTime() != null) {
            RideDetailHelper.setStyledText(date, "Date:", ride.getStartingTime().format(dateFormat));
            RideDetailHelper.setStyledText(startTime, "Start time:", ride.getStartingTime().format(timeFormat));
        }
        if (ride.getFinishedTime() != null) {
            RideDetailHelper.setStyledText(endTime, "End time:", ride.getFinishedTime().format(timeFormat));
        }

        RideDetailHelper.setStyledText(start, "Start location:", ride.getStartPoint());
        RideDetailHelper.setStyledText(end, "End location:", ride.getEndPoint());
        RideDetailHelper.setStyledText(price, "Price:", "$" + ride.getPrice());
        RideDetailHelper.setStyledText(tvPanicActivated, "Panic Activated:",
                ride.getPanicActivated() != null && ride.getPanicActivated() ? "Yes" : "No");

        if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (GetRidePassengerDTO p : ride.getPassengers()) {
                sb.append("• ").append(p.getUsername()).append("\n");
            }
            tvPassengers.setText(sb.toString());
        } else {
            tvPassengers.setText("None");
        }
    }

    private void setupMap(View view) {
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFrag != null) {
            mapFrag.getMapAsync(googleMap -> {
                mMap = googleMap;
                mapManager = new MapManager(requireContext(), mMap);

                LatLng noviSad = new LatLng(45.2519, 19.8370);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

                if (ride != null) {
                    if (ride.getRoute() != null && ride.getRoute().getEncodedPolyline() != null) {
                        drawRouteOnMap();
                    } else if (ride.getStartPoint() != null && ride.getEndPoint() != null) {
                        drawRouteByGeocoding();
                    }
                }

            });
        }
    }

    private void drawRouteOnMap() {
        RideDetailHelper.drawRouteOnMap(mMap, mapManager, ride, this::drawRouteByGeocoding);
    }

    private void drawRouteByGeocoding() {
        RideDetailHelper.drawRouteByGeocoding(mapManager, ride.getStartPoint(), ride.getEndPoint());
    }

    private void loadDriverInfo(View view) {
        RideDetailHelper.loadDriverInfo(requireContext(), view, ride.getDriverId(), isAdded());
    }

    private void loadRatings(View view) {
        RideDetailHelper.loadRatings(requireContext(), view, ride.getId(), isAdded());
    }

    private void loadInconsistencyReports(View view, LayoutInflater inflater) {
        RideDetailHelper.loadInconsistencyReports(requireContext(), inflater, view, ride.getId(), isAdded());
    }

    private void setupButtons(View view) {
        Button btnReorderRide = view.findViewById(R.id.btnReorderRide);
        Button btnRateRide = view.findViewById(R.id.btnRateRide);

        if (btnRateRide != null && ride != null) {
            if ("FINISHED".equals(ride.getStatus())) {
                btnRateRide.setVisibility(View.VISIBLE);
                btnRateRide.setOnClickListener(v -> openRateRideFragment());
            } else {
                btnRateRide.setVisibility(View.GONE);
            }
        }


        if (btnReorderRide != null) {
            btnReorderRide.setOnClickListener(v -> reorderRide(ride));
        }

        btnFavoriteRide.setOnClickListener(v -> favoriteRide());
        btnUnfavoriteRide.setOnClickListener(v -> unfavoriteRide());
    }


    private void openRateRideFragment() {
        if (ride == null || ride.getDriverId() == null) return;

        PassengerRateDriverVehicleFragment fragment =
                new PassengerRateDriverVehicleFragment();

        Bundle args = new Bundle();
        args.putLong("rideId", ride.getId());
        args.putLong("driverId", ride.getDriverId());
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }




    private void reorderRide(GetRideDTO ride) {
        PassengerHomeFragment fragment = PassengerHomeFragment.newInstance();

        Bundle args = new Bundle();
        args.putSerializable("REORDER_RIDE", ride);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
