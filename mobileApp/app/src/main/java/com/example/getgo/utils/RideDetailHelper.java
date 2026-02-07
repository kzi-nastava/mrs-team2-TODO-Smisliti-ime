package com.example.getgo.utils;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.api.services.RatingApiService;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.route.RouteDTO;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

public class RideDetailHelper {

    public static void setStyledText(TextView tv, String label, String value) {
        String html = "<b><font color='#FFFFFF'>" + label + "</font></b> " +
                "<font color='#FFFFFF'>" + (value == null ? "-" : value) + "</font>";
        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
    }

    public static void drawRouteOnMap(GoogleMap mMap, MapManager mapManager, GetRideDTO ride, Runnable fallbackGeocoding) {
        if (mapManager == null || ride == null) {
            Log.w("RideDetailHelper", "MapManager or ride is null");
            return;
        }

        RouteDTO route = ride.getRoute();
        if (route == null || route.getEncodedPolyline() == null) {
            Log.w("RideDetailHelper", "Route or polyline is null, falling back to geocoding");
            if (fallbackGeocoding != null) fallbackGeocoding.run();
            return;
        }

        try {
            JSONArray polylineArray = new JSONArray(route.getEncodedPolyline());
            List<LatLng> routePoints = new ArrayList<>();

            for (int i = 0; i < polylineArray.length(); i++) {
                JSONArray point = polylineArray.getJSONArray(i);
                double lng = point.getDouble(0);
                double lat = point.getDouble(1);
                routePoints.add(new LatLng(lat, lng));
            }

            if (routePoints.isEmpty()) {
                Log.w("RideDetailHelper", "No points in polyline");
                return;
            }

            LatLng start = routePoints.get(0);
            LatLng end = routePoints.get(routePoints.size() - 1);

            mapManager.addWaypointMarker(start, 0, "Start");
            mapManager.addWaypointMarker(end, 100, "End");

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(10)
                    .color(0xFF0000FF)
                    .geodesic(true);

            mMap.addPolyline(polylineOptions);

            if (routePoints.size() > 1) {
                com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder =
                        new com.google.android.gms.maps.model.LatLngBounds.Builder();
                for (LatLng point : routePoints) {
                    boundsBuilder.include(point);
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
            }

            Log.d("RideDetailHelper", "Route drawn with " + routePoints.size() + " points");

        } catch (JSONException e) {
            Log.e("RideDetailHelper", "Error parsing polyline: " + e.getMessage());
            if (fallbackGeocoding != null) fallbackGeocoding.run();
        }
    }

    public static void drawRouteByGeocoding(MapManager mapManager, String startAddr, String endAddr) {
        if (startAddr == null || endAddr == null) {
            Log.w("RideDetailHelper", "Start or end address is null");
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
                        Log.w("RideDetailHelper", "Geocode end failed: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.w("RideDetailHelper", "Geocode start failed: " + error);
            }
        });
    }

    public static void loadDriverInfo(Context context, View view, Long driverId, boolean isAdded) {
        TextView tvDriverHeader = view.findViewById(R.id.tvDriverHeader);
        LinearLayout layoutDriverDetails = view.findViewById(R.id.layoutDriverDetails);
        TextView tvDriverDetails = view.findViewById(R.id.tvDriverDetails);

        if (tvDriverHeader == null) {
            Log.w("RideDetailHelper", "Driver header view not found");
            return;
        }

        tvDriverHeader.setText("Driver: Loading...");
        layoutDriverDetails.setVisibility(View.GONE);

        tvDriverHeader.setOnClickListener(v -> {
            int visibility = layoutDriverDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            layoutDriverDetails.setVisibility(visibility);
        });

        DriverApiService driverService = ApiClient.getClient().create(DriverApiService.class);
        driverService.getDriverProfileById(driverId).enqueue(new Callback<GetDriverDTO>() {
            @Override
            public void onResponse(Call<GetDriverDTO> call, Response<GetDriverDTO> response) {
                if (!isAdded) return;

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
                } else {
                    tvDriverHeader.setText("Driver: Failed to load");
                }
            }

            @Override
            public void onFailure(Call<GetDriverDTO> call, Throwable t) {
                if (!isAdded) return;
                tvDriverHeader.setText("Driver: Error");
            }
        });
    }

    public static void loadRatings(Context context, View view, Long rideId, boolean isAdded) {
        TextView tvAvgDriverRating = view.findViewById(R.id.tvAvgDriverRating);
        TextView tvAvgVehicleRating = view.findViewById(R.id.tvAvgVehicleRating);
        LinearLayout ratingsContainer = view.findViewById(R.id.ratingsContainer);

        if (tvAvgDriverRating == null || tvAvgVehicleRating == null || ratingsContainer == null) {
            Log.w("RideDetailHelper", "Ratings views missing in layout");
            return;
        }

        tvAvgDriverRating.setText("Driver rating: Loading...");
        tvAvgVehicleRating.setText("Vehicle rating: Loading...");
        ratingsContainer.removeAllViews();

        RatingApiService ratingService = ApiClient.getClient().create(RatingApiService.class);
        ratingService.getRatings(rideId).enqueue(new Callback<List<GetRatingDTO>>() {
            @Override
            public void onResponse(Call<List<GetRatingDTO>> call, Response<List<GetRatingDTO>> response) {
                if (!isAdded) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GetRatingDTO> ratings = response.body();

                    if (ratings.isEmpty()) {
                        tvAvgDriverRating.setText("Driver rating: No ratings yet");
                        tvAvgVehicleRating.setText("Vehicle rating: No ratings yet");
                        TextView tvEmpty = new TextView(context);
                        tvEmpty.setText("No ratings for this ride.");
                        tvEmpty.setTextColor(context.getResources().getColor(android.R.color.white, null));
                        tvEmpty.setPadding(16, 8, 16, 8);
                        ratingsContainer.addView(tvEmpty);
                        return;
                    }

                    double sumDriver = 0;
                    double sumVehicle = 0;

                    for (GetRatingDTO rating : ratings) {
                        sumDriver += rating.getDriverRating() != null ? rating.getDriverRating() : 0;
                        sumVehicle += rating.getVehicleRating() != null ? rating.getVehicleRating() : 0;

                        TextView tvRating = new TextView(context);
                        tvRating.setPadding(16, 8, 16, 8);
                        tvRating.setTextColor(context.getResources().getColor(android.R.color.white, null));

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
                } else {
                    tvAvgDriverRating.setText("Driver rating: Failed to load");
                    tvAvgVehicleRating.setText("Vehicle rating: Failed to load");
                }
            }

            @Override
            public void onFailure(Call<List<GetRatingDTO>> call, Throwable t) {
                if (!isAdded) return;
                tvAvgDriverRating.setText("Driver rating: Error");
                tvAvgVehicleRating.setText("Vehicle rating: Error");
            }
        });
    }

    public static void loadInconsistencyReports(Context context, LayoutInflater inflater, View view, Long rideId, boolean isAdded) {
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
        rideService.getInconsistencyReports(rideId).enqueue(new Callback<List<GetInconsistencyReportDTO>>() {
            @Override
            public void onResponse(Call<List<GetInconsistencyReportDTO>> call, Response<List<GetInconsistencyReportDTO>> response) {
                if (!isAdded) return;

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
                } else {
                    if (tvNoReports != null) {
                        tvNoReports.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetInconsistencyReportDTO>> call, Throwable t) {
                if (!isAdded) return;
                if (tvReportsLoading != null) {
                    tvReportsLoading.setVisibility(View.GONE);
                }
                if (tvNoReports != null) {
                    tvNoReports.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}

