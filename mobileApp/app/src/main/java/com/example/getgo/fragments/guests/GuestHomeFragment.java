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
import android.widget.Button;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.vehicle.GetVehicleDTO;
import com.example.getgo.api.services.VehicleApiService;
import com.example.getgo.repositories.DriverRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private VehicleApiService vehicleApiService;
    private Button btnZoomIn, btnZoomOut;

    public GuestHomeFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicleApiService = ApiClient.getClient().create(VehicleApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_guest_home, container, false);

        btnZoomIn = root.findViewById(R.id.btn_zoom_in);
        btnZoomOut = root.findViewById(R.id.btn_zoom_out);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnZoomIn.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng noviSad = new LatLng(45.2519, 19.8370);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(noviSad, 12f));

        //
        mMap.addMarker(new MarkerOptions()
                .position(noviSad)
                .title("Starting point"));

        loadActiveDrivers();
    }

//    private void loadVehicles() {
//        vehicleApiService.getVehicles().enqueue(new Callback<List<GetVehicleDTO>>() {
//            @Override
//            public void onResponse(Call<List<GetVehicleDTO>> call,
//                                   Response<List<GetVehicleDTO>> response) {
//                if (response.isSuccessful() && response.body() != null) {
////                    showVehiclesOnMap(response.body());
//                    List<GetVehicleDTO> vehicles = response.body();
//                    Log.d("GuestHome", "Vehicles received: " + (vehicles != null ? vehicles.size() : "null"));
//
//                    if (vehicles != null) {
//                        for (GetVehicleDTO v : vehicles) {
//                            Log.d("GuestHome", "Vehicle: " + v.getModel()
//                                    + ", lat: " + v.getLatitude()
//                                    + ", lng: " + v.getLongitude()
//                                    + ", available: " + v.getAvailable());
//                        }
//                        showVehiclesOnMap(vehicles);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<GetVehicleDTO>> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//    }

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
            String title = d.getVehicleType();
            String snippet = "Status: " + (d.getIsAvailable() ? "Free" : "Occupied");

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(bitmapDescriptorFromVector(
                            getContext(),
                            d.getIsAvailable() ? R.drawable.ic_car_green : R.drawable.ic_car_red,
                            120, 120
                    )));
        }
    }



    private void showVehiclesOnMap(List<GetVehicleDTO> vehicles) {
        if (mMap == null) return;

        for (GetVehicleDTO v : vehicles) {
            if (v.getLatitude() == null || v.getLongitude() == null) continue;

            LatLng position = new LatLng(v.getLatitude(), v.getLongitude());
            Log.d("GuestHome", "Adding marker for " + v.getModel() + " at " + position);

            String title = v.getModel();
            String snippet = v.getAvailable() ? "Status: Free" : "Status: Occupied";

            int iconWidth = 120;
            int iconHeight = 120;
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(bitmapDescriptorFromVector(getContext(),
                            v.getAvailable() ? R.drawable.ic_car_green : R.drawable.ic_car_red,
                            120, 120))
                    .title(title)
                    .snippet(snippet));
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(@NonNull Context context, int vectorResId, int width, int height) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, width, height); // Ovde menjaš veličinu
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
