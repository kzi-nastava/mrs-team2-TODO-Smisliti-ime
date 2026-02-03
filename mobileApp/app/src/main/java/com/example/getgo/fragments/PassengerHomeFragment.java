package com.example.getgo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.getgo.LoginActivity;
import com.example.getgo.R;
import com.example.getgo.RegisterActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.UiSettings;

public class PassengerHomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

    public PassengerHomeFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button zoomIn = view.findViewById(R.id.btn_zoom_in);
        Button zoomOut = view.findViewById(R.id.btn_zoom_out);

        if (zoomIn != null) {
            zoomIn.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }
        if (zoomOut != null) {
            zoomOut.setOnClickListener(v -> {
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut());
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        UiSettings s = googleMap.getUiSettings();
        s.setZoomControlsEnabled(false);
        LatLng defaultPos = new LatLng(45.248, 19.842);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 12f));
    }
}
