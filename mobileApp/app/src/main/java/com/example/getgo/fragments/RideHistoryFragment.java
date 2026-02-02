package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.getgo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.getgo.adapters.RideHistoryAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;
import com.example.getgo.interfaces.DriverApi;
import com.example.getgo.model.Ride;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RideHistoryFragment extends Fragment {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private TextView tvFilterDate;
    private ArrayList<GetRideDTO> fullHistoryList;
    private MaterialButton btnReset, btnApply;
    private MaterialButton btnNext, btnPrev;

    private int currentPage = 0;
    private int pageSize = 5;
    private int totalElements = 0;


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
        btnNext = view.findViewById(R.id.btnNextPage);
        btnPrev = view.findViewById(R.id.btnPrevPage);

        tvFilterDate = view.findViewById(R.id.tvFilterDate);

        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);

        Spinner spinnerPageSize = view.findViewById(R.id.spinnerPageSize);
        Integer[] pageSizes = {5, 10, 20};
        ArrayAdapter<Integer> adapterSpinner = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, pageSizes);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapterSpinner);

        spinnerPageSize.setSelection(0);

        // when passenger change number of cards per page
        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pageSize = pageSizes[position];
                currentPage = 0; // reset to first page
                loadRideHistoryFromServer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        fullHistoryList = new ArrayList<>();

        adapter = new RideHistoryAdapter(requireContext());
        rideHistoryLV.setAdapter(adapter);

        tvFilterDate.setOnClickListener(v -> showDatePicker());

        btnReset.setOnClickListener(v -> {
            tvFilterDate.setText("Filter by date");
            currentPage = 0;
            loadRideHistoryFromServer();
        });

        btnApply.setOnClickListener(v -> {
            currentPage = 0;
            loadRideHistoryFromServer();
        });

        btnNext.setOnClickListener(v -> {
            if ((currentPage + 1) * pageSize < totalElements) {
                currentPage++;
                loadRideHistoryFromServer();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadRideHistoryFromServer();
            }
        });

        loadRideHistoryFromServer();

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

    private void loadRideHistoryFromServer() {
        DriverApi driverApi = ApiClient.getClient().create(DriverApi.class);
        String startDate = "";
//        String startDate = "01-02-2026";

        if (!tvFilterDate.getText().toString().equals("Filter by date")) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = inputFormat.parse(tvFilterDate.getText().toString());
                startDate = outputFormat.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        driverApi.getDriverRides(currentPage, pageSize, startDate).enqueue(new Callback<PageResponse<GetRideDTO>>() {
            @Override
            public void onResponse(Call<PageResponse<GetRideDTO>> call, Response<PageResponse<GetRideDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<GetRideDTO> pageResponse = response.body();

                    List<GetRideDTO> ridesFromServer = pageResponse.getContent();
                    totalElements = pageResponse.getTotalElements();

                    Log.d("RideHistory", "Broj vožnji preuzetih sa servera: " + ridesFromServer.size());

                    for (GetRideDTO ride : ridesFromServer) {
                        Log.d("RideHistory", "Ride: " + ride.getStartPoint() + " -> " + ride.getEndPoint());
                    }
                    fullHistoryList = new ArrayList<>(ridesFromServer);
                    adapter.setRides(ridesFromServer);
                } else {
                    Log.d("RideHistory", "Response nije uspešan: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PageResponse<GetRideDTO>> call, Throwable t) {
                t.printStackTrace();
                Log.d("RideHistory", "Greška pri preuzimanju vožnji: " + t.getMessage());

            }
        });
    }



}