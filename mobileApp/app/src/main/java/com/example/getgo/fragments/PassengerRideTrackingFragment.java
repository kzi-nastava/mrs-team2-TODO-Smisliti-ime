package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassengerRideTrackingFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker driverMarker;


    public PassengerRideTrackingFragment() {
        // Required empty public constructor
    }

    public static PassengerRideTrackingFragment newInstance(String param1, String param2) {
        PassengerRideTrackingFragment fragment = new PassengerRideTrackingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_passenger_ride_tracking,
                container,
                false
        );

//        SupportMapFragment mapFragment =
//                (SupportMapFragment) getChildFragmentManager()
//                        .findFragmentById(R.id.mapContainer);
//
//        if (mapFragment == null) {
//            mapFragment = SupportMapFragment.newInstance();
//            getChildFragmentManager().beginTransaction()
//                    .replace(R.id.mapContainer, mapFragment)
//                    .commit();
//        }
//        mapFragment.getMapAsync(this);

        TextView placeholder = new TextView(getContext());
        placeholder.setText("Ride Tracking Map Placeholder (API key missing)");
        placeholder.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        FrameLayout mapContainer = view.findViewById(R.id.mapContainer);
        mapContainer.addView(placeholder);

        Button btnReport = view.findViewById(R.id.btnReport);
        LinearLayout reportForm = view.findViewById(R.id.reportForm);
        Button btnSubmit = view.findViewById(R.id.btnSubmitReport);
        Button btnCancel = view.findViewById(R.id.btnCancelReport);
        EditText editReport = view.findViewById(R.id.editReport);

        btnReport.setOnClickListener(v -> {
            reportForm.setVisibility(View.VISIBLE);
        });

        btnCancel.setOnClickListener(v -> {
            reportForm.setVisibility(View.GONE);
        });


        btnSubmit.setOnClickListener(v -> {
            String reportText = editReport.getText().toString();
            if(!reportText.isEmpty()) {
                Log.d("PassengerRideTracking", "Report submitted: " + reportText);
                reportForm.setVisibility(View.GONE); // sakrij formu
                editReport.setText("");               // očisti polje
            } else {
                Toast.makeText(getContext(), "Please enter a note", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        LatLng start = new LatLng(44.8176, 20.4633);
        driverMarker = mMap.addMarker(
                new MarkerOptions().position(start).title("Driver")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f));
    }

    private void updateDriverPosition(double lat, double lng) {
        if (driverMarker == null) return;

        LatLng newPosition = new LatLng(lat, lng);
        driverMarker.setPosition(newPosition);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(newPosition));
    }

}