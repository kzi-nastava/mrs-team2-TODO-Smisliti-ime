package com.example.getgo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.getgo.R;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapManager {
    private static final String TAG = "MapManager";
    private static final String DIRECTIONS_API_KEY = "AIzaSyCBpfJRsfmQwttGjqAApSL8EjTaB4guK5c";

    private final Context context;
    private final GoogleMap map;
    private final Geocoder geocoder;
    private final OkHttpClient httpClient;

    private final List<Marker> waypointMarkers;
    private final List<Marker> driverMarkers = new ArrayList<>();

    private Polyline routePolyline;

    public interface AddressCallback {
        void onAddressFound(String address, LatLng latLng);
        void onError(String error);
    }

    public interface RouteCallback {
        void onRouteFound(int distanceMeters, int durationSeconds);
        void onError(String error);
    }

    public MapManager(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.httpClient = new OkHttpClient();
        this.waypointMarkers = new ArrayList<>();
    }

    public void updateDriverLocations(List<GetActiveDriverLocationDTO> drivers) {
        for (Marker marker : driverMarkers) {
            if (marker != null) {
                marker.remove();
            }
        }
        driverMarkers.clear();

        for (GetActiveDriverLocationDTO driver : drivers) {
            if (driver.getLatitude() != null && driver.getLongitude() != null) {
                LatLng position = new LatLng(driver.getLatitude(), driver.getLongitude());

                int iconRes = driver.getIsAvailable() ? R.drawable.ic_car_green : R.drawable.ic_car_red;
                BitmapDescriptor icon = ImageUtils.getBitmapDescriptorFromDrawable(context, iconRes);

                String title = driver.getVehicleType() + " - " + (driver.getIsAvailable() ? "Available" : "Busy");

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(title)
                        .icon(icon);

                Marker marker = map.addMarker(markerOptions);
                driverMarkers.add(marker);
            }
        }
    }

    public void getAddressFromLocation(LatLng latLng, AddressCallback callback) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                );

                if (addresses != null && !addresses.isEmpty()) {
                    String finalAddress = buildAddress(addresses);
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onAddressFound(finalAddress, latLng)
                    );
                } else {
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError("No address found")
                    );
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onError("Geocoding failed: " + e.getMessage())
                );
            }
        }).start();
    }

    private static String buildAddress(List<Address> addresses) {
        Address address = addresses.get(0);
        String streetName = address.getThoroughfare();
        String streetNumber = address.getSubThoroughfare();

        String fullAddress;
        if (streetName != null && streetNumber != null) {
            fullAddress = streetName + " " + streetNumber;
        } else if (streetName != null) {
            fullAddress = streetName;
        } else {
            fullAddress = address.getAddressLine(0);
        }

        return fullAddress;
    }

    public Marker addWaypointMarker(LatLng position, int index, String title) {
        // Remove existing marker at this index if any
        if (index < waypointMarkers.size() && waypointMarkers.get(index) != null) {
            waypointMarkers.get(index).remove();
        }

        // Determine marker color based on index
        int color = index == 0 ? Color.GREEN : Color.parseColor("#F97316"); // Green for start, orange for others

        BitmapDescriptor icon = getColoredMarkerIcon(color);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .icon(icon);

        Marker marker = map.addMarker(markerOptions);

        // Ensure list is large enough
        while (waypointMarkers.size() <= index) {
            waypointMarkers.add(null);
        }
        waypointMarkers.set(index, marker);

        return marker;
    }

    public void drawRoute(List<LatLng> waypoints, RouteCallback callback) {
        Log.d("MapManager", "drawRoute called with " + waypoints.size() + " waypoints");

        if (waypoints == null || waypoints.size() < 2) {
            if (callback != null) callback.onError("Need at least 2 waypoints");
            Log.d("MapManager", "Not enough waypoints, returning");
            return;
        }

        clearRoute();

        String url = buildDirectionsUrl(waypoints);
        Log.d("MapManager", "Directions API URL: " + url);

        Request request = new Request.Builder().url(url).build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleRouteError(callback, "Failed to fetch route: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleRouteResponse(response, callback);
            }
        });
    }

    private String buildDirectionsUrl(List<LatLng> waypoints) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");

        LatLng origin = waypoints.get(0);
        url.append("origin=").append(origin.latitude).append(",").append(origin.longitude);

        LatLng destination = waypoints.get(waypoints.size() - 1);
        url.append("&destination=").append(destination.latitude).append(",").append(destination.longitude);

        if (waypoints.size() > 2) {
            url.append("&waypoints=");
            for (int i = 1; i < waypoints.size() - 1; i++) {
                if (i > 1) url.append("|");
                LatLng wp = waypoints.get(i);
                url.append(wp.latitude).append(",").append(wp.longitude);
            }
        }

        url.append("&key=").append(DIRECTIONS_API_KEY);
        return url.toString();
    }

    private void handleRouteResponse(Response response, RouteCallback callback) throws IOException {
        if (!response.isSuccessful()) {
            handleRouteError(callback, "API error: " + response.code());
            return;
        }

        try {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (!json.getString("status").equals("OK")) {
                handleRouteError(callback, "Route not found: " + json.getString("status"));
                return;
            }

            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() == 0) {
                handleRouteError(callback, "No routes found");
                return;
            }

            parseAndDrawRoute(routes.getJSONObject(0), callback);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing directions response", e);
            handleRouteError(callback, "Failed to parse route: " + e.getMessage());
        }
    }

    private void parseAndDrawRoute(JSONObject route, RouteCallback callback) throws Exception {
        String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
        List<LatLng> decodedPath = decodePolyline(encodedPolyline);

        int[] totals = calculateTotals(route.getJSONArray("legs"));

        ((android.app.Activity) context).runOnUiThread(() -> {
            PolylineOptions options = new PolylineOptions()
                    .addAll(decodedPath)
                    .color(Color.parseColor("#3B82F6"))
                    .width(10f);

            routePolyline = map.addPolyline(options);

            if (callback != null) {
                callback.onRouteFound(totals[0], totals[1]);
            }
        });
    }

    private int[] calculateTotals(JSONArray legs) throws Exception {
        int totalDistance = 0;
        int totalDuration = 0;

        for (int i = 0; i < legs.length(); i++) {
            JSONObject leg = legs.getJSONObject(i);
            totalDistance += leg.getJSONObject("distance").getInt("value");
            totalDuration += leg.getJSONObject("duration").getInt("value");
        }

        return new int[]{totalDistance, totalDuration};
    }

    private void handleRouteError(RouteCallback callback, String error) {
        Log.e(TAG, "Route error: " + error);
        if (callback != null) {
            ((android.app.Activity) context).runOnUiThread(() -> callback.onError(error));
        }
    }

    public void clearWaypoints() {
        for (Marker marker : waypointMarkers) {
            if (marker != null) {
                marker.remove();
            }
        }
        waypointMarkers.clear();
    }

    public void clearRoute() {
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }
    }

    public void reset() {
        clearWaypoints();
        clearRoute();
        for (Marker marker : driverMarkers) {
            if (marker != null) {
                marker.remove();
            }
        }
        driverMarkers.clear();
    }

    private BitmapDescriptor getColoredMarkerIcon(int color) {
        int size = 40;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setColor(color);
        paint.setStyle(android.graphics.Paint.Style.FILL);
        paint.setAntiAlias(true);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint);

        paint.setColor(Color.WHITE);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}