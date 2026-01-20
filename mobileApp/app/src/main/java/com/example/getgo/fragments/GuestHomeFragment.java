package com.example.getgo.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.getgo.R;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.vehicle.GetVehicleDTO;
import com.example.getgo.interfaces.VehicleApi;

import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;

import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer;

import static com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.BOTTOM;

import com.mapbox.maps.extension.style.expressions.generated.Expression;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.IconRotationAlignment;

import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.extension.style.expressions.generated.Expression;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;


public class GuestHomeFragment extends Fragment {

    private MapView mapView;
    private VehicleApi vehicleApi;
    private PointAnnotationManager pointAnnotationManager;


    public GuestHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_guest_home, container, false);

        mapView = root.findViewById(R.id.map_view);

        Button btnZoomIn = root.findViewById(R.id.btn_zoom_in);
        Button btnZoomOut = root.findViewById(R.id.btn_zoom_out);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(Point.fromLngLat(20.4569, 44.8176))
                            .zoom(12.0)
                            .build()
            );


            loadVehicles();
        });

        btnZoomIn.setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(mapView.getMapboxMap().getCameraState().getCenter())
                            .zoom(currentZoom + 1)
                            .build()
            );
        });

        btnZoomOut.setOnClickListener(v -> {
            double currentZoom = mapView.getMapboxMap().getCameraState().getZoom();
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(mapView.getMapboxMap().getCameraState().getCenter())
                            .zoom(currentZoom - 1)
                            .build()
            );
        });

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicleApi = ApiClient.getClient().create(VehicleApi.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void showVehiclesOnMap(List<GetVehicleDTO> vehicles) {
        if (mapView == null) return;

        // Dobijanje AnnotationPlugin
        AnnotationPlugin annotationPlugin = mapView.getPlugin(AnnotationPlugin.class);
        if (annotationPlugin == null) return;

        // Kreiranje PointAnnotationManager-a
        pointAnnotationManager = new PointAnnotationManager(annotationPlugin, "vehicles-manager");

        pointAnnotationManager.deleteAll(); // ukloni stare markere

        for (GetVehicleDTO v : vehicles) {
            if (v.getLatitude() == null || v.getLongitude() == null) continue;

            Point point = Point.fromLngLat(v.getLongitude(), v.getLatitude());
            com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions options =
                    new com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(v.getAvailable() ? "ic_car_green" : "ic_car_red"); // drawable

            pointAnnotationManager.create(options);
        }
    }


//    private void showVehiclesOnMap(List<GetVehicleDTO> vehicles) {
//        if (mapView == null) return;
//
//        PointAnnotationManager pointAnnotationManager = com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager.create(mapView);
//
//        pointAnnotationManager.deleteAll(); // ukloni stare markere
//
//        for (GetVehicleDTO v : vehicles) {
//            if (v.getLatitude() == null || v.getLongitude() == null) continue;
//
//            Point point = Point.fromLngLat(v.getLongitude(), v.getLatitude());
//            com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions options =
//                    new com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions()
//                            .withPoint(point)
//                            .withIconImage(v.getAvailable() ? "ic_car_green" : "ic_car_red"); // drawable
//
//            pointAnnotationManager.create(options);
//        }
//    }

    private void loadVehicles() {
        vehicleApi.getVehicles().enqueue(new Callback<List<GetVehicleDTO>>() {
            @Override
            public void onResponse(Call<List<GetVehicleDTO>> call,
                                   Response<List<GetVehicleDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showVehiclesOnMap(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<GetVehicleDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


}
