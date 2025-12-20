package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.getgo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.getgo.adapters.RideHistoryAdapter;
import com.example.getgo.model.Ride;



public class RideHistoryFragment extends Fragment {

    private ListView rideHistoryLV;
    private RideHistoryAdapter adapter;
    private ArrayList<Ride> historyList;

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

        rideHistoryLV = view.findViewById(R.id.rideHistoryListView);
        historyList = new ArrayList<>();

        historyList.add(new Ride(
                1,
                "2025-12-20",
                "14:30",
                "15:10",
                "Belgrade",
                "Novi Sad",
                25.5,
                101,
                false,
                null,
                "FINISHED",
                Arrays.asList("Marko", "Nikola")
        ));

        historyList.add(new Ride(
                2,
                "2025-12-18",
                "09:00",
                null,
                "Novi Sad",
                "Subotica",
                0,
                102,
                false,
                "PASSENGER",
                "CANCELED",
                Arrays.asList("Ana")
        ));

        adapter = new RideHistoryAdapter(requireContext(), historyList);
        rideHistoryLV.setAdapter(adapter);
        return view;

//        return inflater.inflate(R.layout.fragment_ride_history, container, false);
    }
}