package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.example.getgo.model.Ride;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;


public class RideHistoryFragment extends Fragment {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private ArrayList<Ride> historyList;
    private TextView tvFilterDate;
    private ArrayList<Ride> fullHistoryList;

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

        tvFilterDate = view.findViewById(R.id.tvFilterDate);

        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);
        historyList = new ArrayList<>();

        historyList.add(new Ride(
                1,
                "2025.12.10.",
                "08:00",
                "08:45",
                "Belgrade, Serbia",
                "Novi Sad, Serbia",
                25.50,
                101,
                false,
                null,
                "FINISHED",
                Arrays.asList("Ana Petrović", "Marko Jovanović")
        ));

        historyList.add(new Ride(
                2,
                "2025.12.11.",
                "09:30",
                "10:15",
                "Belgrade, Serbia",
                "Smederevo, Serbia",
                18.75,
                102,
                false,
                null,
                "FINISHED",
                Arrays.asList("Jelena Ilić")
        ));

        historyList.add(new Ride(
                3,
                "2025.12.11.",
                "11:00",
                "11:45",
                "Novi Sad, Serbia",
                "Subotica, Serbia",
                40.00,
                103,
                true,
                null,
                "FINISHED",
                Arrays.asList("Ivan Kovačević", "Sara Đorđević")
        ));

        historyList.add(new Ride(
                4,
                "2025.12.12.",
                "14:00",
                "16:00",
                "Belgrade, Serbia",
                "Zemun, Serbia",
                12.00,
                104,
                false,
                null,
                "IN_PROGRESS",
                Arrays.asList("Petar Lukić")
        ));

        historyList.add(new Ride(
                5,
                "2025.12.12.",
                "15:30",
                "16:15",
                "Novi Sad, Serbia",
                "Belgrade, Serbia",
                30.00,
                105,
                false,
                "PASSENGER",
                "CANCELED",
                Arrays.asList("Milan Stojanović")
        ));

        historyList.add(new Ride(
                6,
                "2025.12.13.",
                "08:15",
                "09:00",
                "Belgrade, Serbia",
                "Novi Sad, Serbia",
                26.50,
                106,
                false,
                null,
                "FINISHED",
                Arrays.asList("Ana Petrović")
        ));

        historyList.add(new Ride(
                7,
                "2025.12.13.",
                "10:30",
                "11:10",
                "Smederevo, Serbia",
                "Belgrade, Serbia",
                19.00,
                107,
                true,
                null,
                "FINISHED",
                Arrays.asList("Marko Jovanović", "Jelena Ilić")
        ));

        historyList.add(new Ride(
                8,
                "2025.12.14.",
                "12:00",
                "13:20",
                "Belgrade, Serbia",
                "Zemun, Serbia",
                12.00,
                108,
                false,
                null,
                "IN_PROGRESS",
                Arrays.asList("Ivan Kovačević")
        ));

        historyList.add(new Ride(
                9,
                "2025.12.14.",
                "13:15",
                "14:00",
                "Novi Sad, Serbia",
                "Subotica, Serbia",
                42.00,
                109,
                false,
                "DRIVER",
                "CANCELED",
                Arrays.asList("Sara Đorđević", "Petar Lukić")
        ));

        historyList.add(new Ride(
                10,
                "2025.12.15.",
                "09:00",
                "09:45",
                "Belgrade, Serbia",
                "Novi Sad, Serbia",
                27.50,
                110,
                false,
                null,
                "FINISHED",
                Arrays.asList("Milan Stojanović")
        ));

        historyList.add(new Ride(
                11,
                "2025.12.15.",
                "11:30",
                "12:15",
                "Belgrade, Serbia",
                "Smederevo, Serbia",
                20.00,
                111,
                true,
                null,
                "IN_PROGRESS",
                Arrays.asList("Ana Petrović", "Jelena Ilić")
        ));

        historyList.add(new Ride(
                12,
                "2025.12.16.",
                "08:45",
                "09:30",
                "Novi Sad, Serbia",
                "Belgrade, Serbia",
                28.00,
                112,
                false,
                null,
                "FINISHED",
                Arrays.asList("Marko Jovanović", "Ivan Kovačević")
        ));

        fullHistoryList = new ArrayList<Ride>(historyList);
        adapter = new RideHistoryAdapter(requireContext(), historyList);
        rideHistoryLV.setAdapter(adapter);

        tvFilterDate.setOnClickListener(v -> showDatePicker());

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

//        return inflater.inflate(R.layout.fragment_ride_history, container, false);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select date")
                        .build();

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault());
            String selectedDate = sdf.format(new Date(selection));

            tvFilterDate.setText(selectedDate);
            filterRidesByDate(selectedDate);
        });
    }

    private void filterRidesByDate(String date) {
        historyList.clear();

        for (Ride ride : fullHistoryList) {
            if (ride.getStartDate().equals(date)) {
                historyList.add(ride);
            }
        }

        adapter.notifyDataSetChanged();
    }


}