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
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import com.example.getgo.dtos.ride.GetRideTrackingDTO;
import com.example.getgo.interfaces.RideApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRideTrackingFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker driverMarker;

    private RideApi rideApi;
    private Long rideId = 1L; // temporary hardcoded ride ID;

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

        Log.d("LIFECYCLE", "onCreateView called");

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
            if (reportText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a note", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateInconsistencyReportDTO dto = new CreateInconsistencyReportDTO(reportText);

            rideApi.createInconsistencyReport(rideId, dto)
                    .enqueue(new Callback<CreatedInconsistencyReportDTO>() {
                        @Override
                        public void onResponse(Call<CreatedInconsistencyReportDTO> call,
                                               Response<CreatedInconsistencyReportDTO> response) {

                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(),
                                        "Report sent successfully",
                                        Toast.LENGTH_SHORT).show();

                                reportForm.setVisibility(View.GONE);
                                editReport.setText("");
                            } else {
                                Log.e("REPORT", "Error: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<CreatedInconsistencyReportDTO> call, Throwable t) {
                            Log.e("REPORT", "POST failed", t);
                        }
                    });
        });

        Log.d("TRACKING", "Does rideId exist = " + rideId);
        if (rideId != null) {
            Log.d("TRACKING", "Calling API for rideId = " + rideId);
            loadRideTracking(view);
        }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideApi = ApiClient.getClient().create(RideApi.class);

        if (getArguments() != null) {
            rideId = getArguments().getLong("rideId");
        }
    }

    private void loadRideTracking(View view) {
        Log.d("TRACKING", "Calling API for rideId = " + rideId);
        rideApi.trackRide(rideId).enqueue(new Callback<GetRideTrackingDTO>() {
            @Override
            public void onResponse(Call<GetRideTrackingDTO> call, Response<GetRideTrackingDTO> response) {
                Log.d("TRACKING", "Response code = " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    GetRideTrackingDTO dto = response.body();

                    Log.d("TRACKING", "DTO = " + new Gson().toJson(dto));

                    TextView tvStart = view.findViewById(R.id.tvStartAddress);
                    TextView tvDestination = view.findViewById(R.id.tvDestination);
                    TextView tvEta = view.findViewById(R.id.tvTimeRemaining);

                    tvStart.setText(dto.getStartAddress());
                    tvDestination.setText(dto.getDestinationAddress());
                    tvEta.setText(dto.getEstimatedTime() + " min");

                    updateDriverPosition(dto.getVehicleLatitude(), dto.getVehicleLongitude());
                } else {
                    Log.e("TRACKING", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetRideTrackingDTO> call, Throwable t) {
                Log.e("TRACKING", "API failed", t);
            }
        });
    }


}