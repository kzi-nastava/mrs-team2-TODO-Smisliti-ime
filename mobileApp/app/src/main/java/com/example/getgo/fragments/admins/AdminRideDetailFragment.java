package com.example.getgo.fragments.admins;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.AdminApiService;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.api.services.RatingApiService;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.route.RouteDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.utils.MapManager;
import com.example.getgo.utils.RideDetailHelper;
import com.example.getgo.utils.ToastHelper;
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

public class AdminRideDetailFragment extends Fragment {
    private Long rideId;
    private String userEmail;
    private String userRole; // "PASSENGER" ili "DRIVER"
    private GetRideDTO ride;
    private GoogleMap mMap;
    private MapManager mapManager;

    private static final String ARG_RIDE_ID = "ride_id";
    private static final String ARG_USER_EMAIL = "user_email";
    private static final String ARG_USER_ROLE = "user_role";

    public AdminRideDetailFragment() {}

    public static AdminRideDetailFragment newInstance(Long rideId, String userEmail, String userRole) {
        AdminRideDetailFragment fragment = new AdminRideDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RIDE_ID, rideId);
        args.putString(ARG_USER_EMAIL, userEmail);
        args.putString(ARG_USER_ROLE, userRole);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AdminRideDetail", "onCreate called");

        if (getArguments() != null) {
            rideId = getArguments().getLong(ARG_RIDE_ID);
            userEmail = getArguments().getString(ARG_USER_EMAIL);
            userRole = getArguments().getString(ARG_USER_ROLE);

            Log.d("AdminRideDetail", "Arguments received: rideId=" + rideId + ", email=" + userEmail + ", role=" + userRole);
        } else {
            Log.w("AdminRideDetail", "No arguments received!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("AdminRideDetail", "onCreateView called");

        View view = inflater.inflate(R.layout.fragment_admin_ride_detail, container, false);

        if (rideId == null || userEmail == null || userRole == null) {
            String errorMsg = "Error: Missing ride data - rideId=" + rideId + ", email=" + userEmail + ", role=" + userRole;
            Log.e("AdminRideDetail", errorMsg);
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            return view;
        }

        Log.d("AdminRideDetail", "Setting up map and loading ride details");
        setupMap(view);
        loadRideDetails(view, inflater);

        return view;
    }

    private void setupMap(View view) {
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFrag != null) {
            mapFrag.getMapAsync(googleMap -> {
                mMap = googleMap;
                mapManager = new MapManager(requireContext(), mMap);

                LatLng noviSad = new LatLng(45.2519, 19.8370);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));
            });
        }
    }

    private void loadRideDetails(View view, LayoutInflater inflater) {
        Log.d("AdminRideDetail", "loadRideDetails called with role: " + userRole);

        AdminApiService adminService = ApiClient.getClient().create(AdminApiService.class);

        Call<GetRideDTO> call;
        if ("DRIVER".equalsIgnoreCase(userRole)) {
            Log.d("AdminRideDetail", "Loading driver ride: email=" + userEmail + ", rideId=" + rideId);
            call = adminService.getDriverRideById(rideId, userEmail);
        } else {
            Log.d("AdminRideDetail", "Loading passenger ride: email=" + userEmail + ", rideId=" + rideId);
            call = adminService.getPassengerRideById(rideId, userEmail);
        }

        call.enqueue(new Callback<GetRideDTO>() {
            @Override
            public void onResponse(Call<GetRideDTO> call, Response<GetRideDTO> response) {
                Log.d("AdminRideDetail", "API Response received: " + response.code());

                if (!isAdded()) {
                    Log.w("AdminRideDetail", "Fragment not added, skipping response");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ride = response.body();
                    Log.d("AdminRideDetail", "Ride loaded successfully: ID=" + ride.getId() + ", Driver=" + ride.getDriverId() + ", Passengers=" + (ride.getPassengers() != null ? ride.getPassengers().size() : 0));

                    displayBasicRideInfo(view);
                    drawRouteOnMap();

                    if (ride.getDriverId() != null) {
                        Log.d("AdminRideDetail", "Loading driver info for ID: " + ride.getDriverId());
                        loadDriverInfo(view);
                    } else {
                        Log.w("AdminRideDetail", "No driver assigned to this ride");
                        TextView tvDriverHeader = view.findViewById(R.id.tvDriverHeader);
                        if (tvDriverHeader != null) {
                            tvDriverHeader.setText("Driver: Not assigned");
                        }
                    }

                    Log.d("AdminRideDetail", "Loading passengers info");
                    loadPassengersInfo(view);

                    Log.d("AdminRideDetail", "Loading ratings");
                    loadRatings(view);

                    Log.d("AdminRideDetail", "Loading inconsistency reports");
                    loadInconsistencyReports(view, inflater);
                } else {
                    String errorMsg = "Failed to load ride: " + response.code() + " - " + response.message();
                    Log.e("AdminRideDetail", errorMsg);
                    ToastHelper.showError(requireContext(), "Failed to load ride", String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<GetRideDTO> call, Throwable t) {
                Log.e("AdminRideDetail", "API call failed", t);

                if (!isAdded()) return;

                ToastHelper.showError(requireContext(), "Failed to load ride", t.getMessage());
            }
        });
    }

    private void displayBasicRideInfo(View view) {
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        TextView tvStartLocation = view.findViewById(R.id.tvStartLocation);
        TextView tvEndLocation = view.findViewById(R.id.tvEndLocation);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvPanicActivated = view.findViewById(R.id.tvPanicActivated);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        if (ride.getStartingTime() != null) {
            RideDetailHelper.setStyledText(tvDate, "Date:", ride.getStartingTime().format(dateFormat));
            RideDetailHelper.setStyledText(tvStartTime, "Start time:", ride.getStartingTime().format(timeFormat));
        }
        if (ride.getFinishedTime() != null) {
            RideDetailHelper.setStyledText(tvEndTime, "End time:", ride.getFinishedTime().format(timeFormat));
        }

        RideDetailHelper.setStyledText(tvStartLocation, "Start location:", ride.getStartPoint());
        RideDetailHelper.setStyledText(tvEndLocation, "End location:", ride.getEndPoint());
        RideDetailHelper.setStyledText(tvPrice, "Price:", "$" + ride.getPrice());
        RideDetailHelper.setStyledText(tvPanicActivated, "Panic Activated:",
                ride.getPanicActivated() != null && ride.getPanicActivated() ? "Yes" : "No");
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

    private void loadPassengersInfo(View view) {
        TextView tvPassengersHeader = view.findViewById(R.id.tvPassengersHeader);
        LinearLayout layoutPassengersDetails = view.findViewById(R.id.layoutPassengersDetails);
        TextView tvPassengersDetails = view.findViewById(R.id.tvPassengersDetails);

        if (tvPassengersHeader == null) return;

        tvPassengersHeader.setOnClickListener(v -> {
            int visibility = layoutPassengersDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            layoutPassengersDetails.setVisibility(visibility);
        });

        if (ride.getPassengers() == null || ride.getPassengers().isEmpty()) {
            tvPassengersHeader.setText("Passengers: None");
            return;
        }

        tvPassengersHeader.setText("Passengers: " + ride.getPassengers().size() + " (Loading details...)");
        layoutPassengersDetails.setVisibility(View.GONE);

        StringBuilder sb = new StringBuilder();
        PassengerApiService passengerService = ApiClient.getClient().create(PassengerApiService.class);

        // Učitaj detalje za svakog passenger-a preko API-ja
        for (GetRidePassengerDTO ridePassenger : ride.getPassengers()) {
            if (ridePassenger.getId() == null) {
                sb.append("• ").append(ridePassenger.getUsername()).append(" (ID missing)\n");
                continue;
            }

            passengerService.getPassengerProfileById(ridePassenger.getId()).enqueue(new Callback<GetPassengerDTO>() {
                @Override
                public void onResponse(Call<GetPassengerDTO> call, Response<GetPassengerDTO> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        GetPassengerDTO passenger = response.body();
                        sb.append("• ").append(passenger.getName()).append(" ").append(passenger.getSurname())
                                .append("\n  Email: ").append(passenger.getEmail())
                                .append("\n  Phone: ").append(passenger.getPhone()).append("\n\n");

                        tvPassengersDetails.setText(sb.toString());
                        tvPassengersHeader.setText("Passengers: " + ride.getPassengers().size());
                        Log.d("AdminRideDetail", "Passenger loaded: " + passenger.getName());
                    } else {
                        sb.append("• ").append(ridePassenger.getUsername()).append(" (Failed to load)\n");
                        tvPassengersDetails.setText(sb.toString());
                        Log.w("AdminRideDetail", "Failed to load passenger: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<GetPassengerDTO> call, Throwable t) {
                    if (!isAdded()) return;
                    sb.append("• ").append(ridePassenger.getUsername()).append(" (Error)\n");
                    tvPassengersDetails.setText(sb.toString());
                    Log.e("AdminRideDetail", "Error loading passenger", t);
                }
            });
        }
    }

    private void loadRatings(View view) {
        TextView tvAvgDriverRating = view.findViewById(R.id.tvAvgDriverRating);
        TextView tvAvgVehicleRating = view.findViewById(R.id.tvAvgVehicleRating);
        LinearLayout ratingsContainer = view.findViewById(R.id.ratingsContainer);

        if (tvAvgDriverRating == null || tvAvgVehicleRating == null || ratingsContainer == null) {
            Log.w("AdminRideDetail", "Ratings views missing in layout");
            return;
        }

        tvAvgDriverRating.setText("Driver rating: Loading...");
        tvAvgVehicleRating.setText("Vehicle rating: Loading...");
        ratingsContainer.removeAllViews();

        RatingApiService ratingService = ApiClient.getClient().create(RatingApiService.class);
        ratingService.getRatings(ride.getId()).enqueue(new Callback<List<GetRatingDTO>>() {
            @Override
            public void onResponse(Call<List<GetRatingDTO>> call, Response<List<GetRatingDTO>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GetRatingDTO> ratings = response.body();

                    if (ratings.isEmpty()) {
                        tvAvgDriverRating.setText("Driver rating: No ratings yet");
                        tvAvgVehicleRating.setText("Vehicle rating: No ratings yet");
                        TextView tvEmpty = new TextView(requireContext());
                        tvEmpty.setText("No ratings for this ride.");
                        tvEmpty.setTextColor(getResources().getColor(android.R.color.white, null));
                        tvEmpty.setPadding(16, 8, 16, 8);
                        ratingsContainer.addView(tvEmpty);
                        return;
                    }

                    double sumDriver = 0;
                    double sumVehicle = 0;

                    for (GetRatingDTO rating : ratings) {
                        sumDriver += rating.getDriverRating() != null ? rating.getDriverRating() : 0;
                        sumVehicle += rating.getVehicleRating() != null ? rating.getVehicleRating() : 0;

                        // Prikaz pojedinačne ocene
                        TextView tvRating = new TextView(requireContext());
                        tvRating.setPadding(16, 8, 16, 8);
                        tvRating.setTextColor(getResources().getColor(android.R.color.white, null));

                        String passengerId = rating.getPassengerId() != null ? "Passenger #" + rating.getPassengerId() : "Anonymous";
                        String comment = rating.getComment() != null && !rating.getComment().isEmpty()
                                ? "\n\"" + rating.getComment() + "\""
                                : "";

                        String ratingText = passengerId + "\n" +
                                "Driver: " + (rating.getDriverRating() != null ? rating.getDriverRating() + "⭐" : "-") +
                                " | Vehicle: " + (rating.getVehicleRating() != null ? rating.getVehicleRating() + "⭐" : "-") +
                                comment;

                        tvRating.setText(ratingText);
                        ratingsContainer.addView(tvRating);
                    }

                    double avgDriver = sumDriver / ratings.size();
                    double avgVehicle = sumVehicle / ratings.size();

                    tvAvgDriverRating.setText(String.format("Average Driver Rating: %.1f ⭐", avgDriver));
                    tvAvgVehicleRating.setText(String.format("Average Vehicle Rating: %.1f ⭐", avgVehicle));

                    Log.d("AdminRideDetail", "Loaded " + ratings.size() + " ratings");
                } else {
                    tvAvgDriverRating.setText("Driver rating: Failed to load");
                    tvAvgVehicleRating.setText("Vehicle rating: Failed to load");
                    Log.w("AdminRideDetail", "Failed to load ratings: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GetRatingDTO>> call, Throwable t) {
                if (!isAdded()) return;
                tvAvgDriverRating.setText("Driver rating: Error");
                tvAvgVehicleRating.setText("Vehicle rating: Error");
                Log.e("AdminRideDetail", "Error loading ratings", t);
            }
        });
    }

    private void loadInconsistencyReports(View view, LayoutInflater inflater) {
        TextView tvReportsLoading = view.findViewById(R.id.tvReportsLoading);
        TextView tvNoReports = view.findViewById(R.id.tvNoReports);
        LinearLayout reportsContainer = view.findViewById(R.id.reportsContainer);

        if (tvReportsLoading != null) {
            tvReportsLoading.setVisibility(View.VISIBLE);
        }
        if (tvNoReports != null) {
            tvNoReports.setVisibility(View.GONE);
        }

        RideApiService rideService = ApiClient.getClient().create(RideApiService.class);
        rideService.getInconsistencyReports(ride.getId()).enqueue(new Callback<List<GetInconsistencyReportDTO>>() {
            @Override
            public void onResponse(Call<List<GetInconsistencyReportDTO>> call, Response<List<GetInconsistencyReportDTO>> response) {
                if (!isAdded()) return;

                if (tvReportsLoading != null) {
                    tvReportsLoading.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<GetInconsistencyReportDTO> reports = response.body();

                    if (reports.isEmpty()) {
                        if (tvNoReports != null) {
                            tvNoReports.setVisibility(View.VISIBLE);
                        }
                        return;
                    }

                    for (GetInconsistencyReportDTO report : reports) {
                        View reportView = inflater.inflate(R.layout.item_report, reportsContainer, false);
                        TextView tvText = reportView.findViewById(R.id.tvReportText);
                        TextView tvMeta = reportView.findViewById(R.id.tvReportMeta);

                        tvText.setText(report.getText());

                        DateTimeFormatter in = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                        DateTimeFormatter out = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
                        String formatted = LocalDateTime.parse(report.getCreatedAt(), in).format(out);
                        String meta = "Reported by <b>" + report.getPassengerEmail() + "</b> • " + formatted;
                        tvMeta.setText(Html.fromHtml(meta, Html.FROM_HTML_MODE_LEGACY));

                        reportsContainer.addView(reportView);
                    }

                    Log.d("AdminRideDetail", "Loaded " + reports.size() + " inconsistency reports");
                } else {
                    if (tvNoReports != null) {
                        tvNoReports.setVisibility(View.VISIBLE);
                    }
                    Log.w("AdminRideDetail", "Failed to load reports: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GetInconsistencyReportDTO>> call, Throwable t) {
                if (!isAdded()) return;
                if (tvReportsLoading != null) {
                    tvReportsLoading.setVisibility(View.GONE);
                }
                if (tvNoReports != null) {
                    tvNoReports.setVisibility(View.VISIBLE);
                }
                Log.e("AdminRideDetail", "Error loading reports", t);
            }
        });
    }
}
