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
import com.example.getgo.dtos.ride.GetReorderRideDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.utils.MapManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public static PassengerRideDetailFragment newInstance(GetRideDTO ride) {
        PassengerRideDetailFragment fragment = new PassengerRideDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
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

        if (ride == null) {
            Toast.makeText(requireContext(), "Error: No ride data", Toast.LENGTH_SHORT).show();
            return view;
        }

        displayBasicRideInfo(view);

        setupMap(view);

        if (ride.getDriverId() != null) {
            loadDriverInfo(view);
        } else {
            TextView tvDriverHeader = view.findViewById(R.id.tvDriverHeader);
            if (tvDriverHeader != null) {
                tvDriverHeader.setText("Driver: Not assigned");
            }
        }

        loadRatings(view);

        loadInconsistencyReports(view, inflater);

        setupButtons(view);

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
            setStyledText(date, "Date:", ride.getStartingTime().format(dateFormat));
            setStyledText(startTime, "Start time:", ride.getStartingTime().format(timeFormat));
        }
        if (ride.getFinishedTime() != null) {
            setStyledText(endTime, "End time:", ride.getFinishedTime().format(timeFormat));
        }

        setStyledText(start, "Start location:", ride.getStartPoint());
        setStyledText(end, "End location:", ride.getEndPoint());
        setStyledText(price, "Price:", "$" + ride.getPrice());
        setStyledText(tvPanicActivated, "Panic Activated:",
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

                // Centriraj mapu na Novi Sad
                LatLng noviSad = new LatLng(45.2519, 19.8370);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

                drawRouteOnMap();
            });
        }
    }

    private void drawRouteOnMap() {
        if (mapManager == null || ride == null) {
            Log.w("RideDetail", "MapManager or ride is null");
            return;
        }

        String startAddr = ride.getStartPoint();
        String endAddr = ride.getEndPoint();

        if (startAddr == null || endAddr == null) {
            Log.w("RideDetail", "Start or end address is null");
            return;
        }

        mapManager.getCoordinatesFromAddress(startAddr, new MapManager.CoordinatesCallback() {
            @Override
            public void onCoordinatesFound(LatLng startLatLng) {
                mapManager.addWaypointMarker(startLatLng, 0, "Start");

                mapManager.getCoordinatesFromAddress(endAddr, new MapManager.CoordinatesCallback() {
                    @Override
                    public void onCoordinatesFound(LatLng endLatLng) {
                        mapManager.addWaypointMarker(endLatLng, 100, "End");
                        mapManager.drawRoute(java.util.Arrays.asList(startLatLng, endLatLng), null);
                    }

                    @Override
                    public void onError(String error) {
                        Log.w("RideDetail", "Geocode end failed: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.w("RideDetail", "Geocode start failed: " + error);
            }
        });
    }

    private void loadDriverInfo(View view) {
        TextView tvDriverHeader = view.findViewById(R.id.tvDriverHeader);
        LinearLayout layoutDriverDetails = view.findViewById(R.id.layoutDriverDetails);
        TextView tvDriverDetails = view.findViewById(R.id.tvDriverDetails);

        if (tvDriverHeader == null) {
            Log.w("RideDetail", "Driver header view not found");
            return;
        }

        tvDriverHeader.setText("Loading driver...");
        layoutDriverDetails.setVisibility(View.GONE);

        tvDriverHeader.setOnClickListener(v -> {
            int visibility = layoutDriverDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            layoutDriverDetails.setVisibility(visibility);
        });

        DriverApiService driverService = ApiClient.getClient().create(DriverApiService.class);
        driverService.getDriverProfileById(ride.getDriverId()).enqueue(new Callback<GetDriverDTO>() {
            @Override
            public void onResponse(Call<GetDriverDTO> call, Response<GetDriverDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    GetDriverDTO driver = response.body();

                    tvDriverHeader.setText("Driver: " + driver.getName() + " " + driver.getSurname());

                    StringBuilder sb = new StringBuilder();
                    sb.append("Email: ").append(driver.getEmail()).append("\n");
                    sb.append("Phone: ").append(driver.getPhone()).append("\n\n");
                    sb.append("Vehicle Information:\n");
                    sb.append("Model: ").append(driver.getVehicleModel() != null ? driver.getVehicleModel() : "N/A").append("\n");
                    sb.append("Type: ").append(driver.getVehicleType() != null ? driver.getVehicleType() : "N/A").append("\n");
                    sb.append("License: ").append(driver.getVehicleLicensePlate() != null ? driver.getVehicleLicensePlate() : "N/A").append("\n");
                    sb.append("Seats: ").append(driver.getVehicleSeats() != null ? driver.getVehicleSeats() : "N/A").append("\n");
                    sb.append("Baby seats: ").append(Boolean.TRUE.equals(driver.getVehicleHasBabySeats()) ? "Yes" : "No").append("\n");
                    sb.append("Pet friendly: ").append(Boolean.TRUE.equals(driver.getVehicleAllowsPets()) ? "Yes" : "No");

                    tvDriverDetails.setText(sb.toString());
                    Log.d("RideDetail", "Driver loaded: " + driver.getName());
                } else {
                    tvDriverHeader.setText("Driver: Failed to load");
                    Log.w("RideDetail", "Failed to load driver: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetDriverDTO> call, Throwable t) {
                if (!isAdded()) return;
                tvDriverHeader.setText("Driver: Error");
                Log.e("RideDetail", "Error loading driver", t);
            }
        });
    }

    private void loadRatings(View view) {
        TextView tvAvgDriverRating = view.findViewById(R.id.tvAvgDriverRating);
        TextView tvAvgVehicleRating = view.findViewById(R.id.tvAvgVehicleRating);
        LinearLayout ratingsContainer = view.findViewById(R.id.ratingsContainer);

        if (tvAvgDriverRating == null || tvAvgVehicleRating == null || ratingsContainer == null) {
            Log.w("RideDetail", "Ratings views missing in layout");
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

                    Log.d("RideDetail", "Loaded " + ratings.size() + " ratings");
                } else {
                    tvAvgDriverRating.setText("Driver rating: Failed to load");
                    tvAvgVehicleRating.setText("Vehicle rating: Failed to load");
                    Log.w("RideDetail", "Failed to load ratings: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GetRatingDTO>> call, Throwable t) {
                if (!isAdded()) return;
                tvAvgDriverRating.setText("Driver rating: Error");
                tvAvgVehicleRating.setText("Vehicle rating: Error");
                Log.e("RideDetail", "Error loading ratings", t);
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

                    Log.d("RideDetail", "Loaded " + reports.size() + " inconsistency reports");
                } else {
                    if (tvNoReports != null) {
                        tvNoReports.setVisibility(View.VISIBLE);
                    }
                    Log.w("RideDetail", "Failed to load reports: " + response.code());
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
                Log.e("RideDetail", "Error loading reports", t);
            }
        });
    }

    private void setupButtons(View view) {
        Button btnReorderRide = view.findViewById(R.id.btnReorderRide);
        Button btnRateRide = view.findViewById(R.id.btnRateRide);

        if (btnRateRide != null) {
            btnRateRide.setVisibility(View.GONE);
        }

        if (btnReorderRide != null) {
            btnReorderRide.setOnClickListener(v -> reorderRide());
        }
    }

    private void reorderRide() {
        PassengerApiService passengerService = ApiClient.getClient().create(PassengerApiService.class);
        passengerService.getRideForReorder(ride.getId()).enqueue(new Callback<GetReorderRideDTO>() {
            @Override
            public void onResponse(Call<GetReorderRideDTO> call, Response<GetReorderRideDTO> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    GetReorderRideDTO reorderRide = response.body();
                    Bundle args = new Bundle();
                    args.putSerializable("REORDER_RIDE", reorderRide);

                    PassengerHomeFragment fragment = PassengerHomeFragment.newInstance();
                    fragment.setArguments(args);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(requireContext(), "Failed to load ride for reorder", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetReorderRideDTO> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStyledText(TextView tv, String label, String value) {
        String html = "<b><font color='#FFFFFF'>" + label + "</font></b> " +
                "<font color='#FFFFFF'>" + (value == null ? "-" : value) + "</font>";
        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
    }
}
