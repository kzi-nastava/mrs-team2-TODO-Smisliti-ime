package com.example.getgo.fragments.passengers;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.api.services.RatingApiService;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.route.RouteDTO;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.RideDetailHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideDetailFragment extends Fragment {
    private GetRideDTO ride;
    private static final String ARG_RIDE = "arg_ride";
    private GoogleMap mMap;
    private MapManager mapManager;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_ride_detail, container, false);

        Long rideId = getArguments() != null ? getArguments().getLong("RIDE_ID") : null;
        if (rideId == null) {
            Toast.makeText(requireContext(), "Error: No ride ID", Toast.LENGTH_SHORT).show();
            return view;
        }

        PassengerApiService passengerService = ApiClient.getClient().create(PassengerApiService.class);
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
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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

        if (btnRateRide != null) {
            btnRateRide.setVisibility(View.GONE);
        }

        if (btnReorderRide != null) {
            btnReorderRide.setOnClickListener(v -> reorderRide(ride));
        }
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
