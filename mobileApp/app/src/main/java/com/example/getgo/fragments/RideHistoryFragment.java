package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.getgo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.getgo.adapters.RideHistoryAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.interfaces.DriverApi;
import com.example.getgo.model.Ride;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.time.format.DateTimeFormatter;



public class RideHistoryFragment extends Fragment {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private TextView tvFilterDate;
    private ArrayList<GetRideDTO> fullHistoryList;
    private MaterialButton btnReset, btnApply;


    public RideHistoryFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static RideHistoryFragment newInstance(String param1, String param2) {
        RideHistoryFragment fragment = new RideHistoryFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ride_history, container, false);

        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);

        tvFilterDate = view.findViewById(R.id.tvFilterDate);

        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);

        fullHistoryList = new ArrayList<>();

        adapter = new RideHistoryAdapter(requireContext());
        rideHistoryLV.setAdapter(adapter);

        tvFilterDate.setOnClickListener(v -> showDatePicker());

        btnReset.setOnClickListener(v -> {
            tvFilterDate.setText("Filter by date");
            adapter.setRides(new ArrayList<>(fullHistoryList));
        });

        btnApply.setOnClickListener(v -> {
            String selectedDate = tvFilterDate.getText().toString();
            if (!selectedDate.equals("Filter by date")) {
                filterRidesByDate(selectedDate);
            }
        });

        Long driverId = 11L;
        loadRideHistoryFromServer(driverId);

        adapter.setOnRideClickListener(ride -> {
            RideDetailFragment fragment =
                    RideDetailFragment.newInstance(ride);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String selectedDate = sdf.format(new Date(selection));

            tvFilterDate.setText(selectedDate);
            filterRidesByDate(selectedDate);
        });
    }

    private void filterRidesByDate(String date) {
        List<GetRideDTO> filteredList = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (GetRideDTO ride : fullHistoryList) {
            if (ride.getStartingTime() != null) {
                String rideDateStr = ride.getStartingTime().format(dateFormatter);

                if (rideDateStr.equals(date)) {
                    filteredList.add(ride);
                }
            }
        }

        adapter.setRides(filteredList);
    }

    private void loadRideHistoryFromServer(Long driverId) {
        DriverApi driverApi = ApiClient.getClient().create(DriverApi.class);

        driverApi.getDriverRides(driverId).enqueue(new retrofit2.Callback<List<GetRideDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<List<GetRideDTO>> call, retrofit2.Response<List<GetRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetRideDTO> ridesFromServer = response.body();

                    Log.d("RideHistory", "Broj vožnji preuzetih sa servera: " + ridesFromServer.size());

                    for (GetRideDTO ride : ridesFromServer) {
                        Log.d("RideHistory", "Ride: " + ride.getStartPoint() + " -> " + ride.getEndPoint());
                    }

                    adapter.setRides(ridesFromServer);


                    fullHistoryList = new ArrayList<>(ridesFromServer);
                } else {
                    Log.d("RideHistory", "Response nije uspešan: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<GetRideDTO>> call, Throwable t) {
                t.printStackTrace();
                Log.d("RideHistory", "Greška pri preuzimanju vožnji: " + t.getMessage());

            }
        });
    }



}