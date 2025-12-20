package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.model.Ride;


public class RideDetailFragment extends Fragment {

    private Ride ride;
    private static final String ARG_RIDE = "arg_ride";

    public RideDetailFragment() {
        // Required empty public constructor
    }


    public static RideDetailFragment newInstance(Ride ride) {
        RideDetailFragment fragment = new RideDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RIDE, ride);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ride = (Ride) getArguments().getSerializable(ARG_RIDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ride_detail, container, false);

        TextView start = view.findViewById(R.id.tvStartLocation);
        TextView end = view.findViewById(R.id.tvEndLocation);
        TextView startTime = view.findViewById(R.id.tvStartTime);
        TextView endTime = view.findViewById(R.id.tvEndTime);
        TextView price = view.findViewById(R.id.tvPrice);
        TextView date = view.findViewById(R.id.tvDate);

        if (ride != null) {
            start.setText(ride.getStartLocation());
            end.setText(ride.getEndLocation());
            startTime.setText(ride.getStartTime());
            endTime.setText(ride.getEndTime());
            price.setText("$" + ride.getPrice());
            date.setText(ride.getStartDate());
        }
        return view;
//        return inflater.inflate(R.layout.fragment_ride_detail, container, false);
    }
}