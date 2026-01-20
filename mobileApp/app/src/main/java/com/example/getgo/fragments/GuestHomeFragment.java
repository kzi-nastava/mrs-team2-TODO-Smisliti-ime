package com.example.getgo.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.getgo.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

public class GuestHomeFragment extends Fragment {

    private MapView mapView;

    public GuestHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_guest_home, container, false);

        // MapView iz XML-a
        mapView = root.findViewById(R.id.map_view);

        // Dugmad za zumiranje
        Button btnZoomIn = root.findViewById(R.id.btn_zoom_in);
        Button btnZoomOut = root.findViewById(R.id.btn_zoom_out);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            // Postavi početnu kameru
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(Point.fromLngLat(20.4569, 44.8176))
                            .zoom(12.0)
                            .build()
            );
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
}
