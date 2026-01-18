package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.dtos.passenger.GetRidePassengerDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.model.Ride;
import android.text.Html;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.time.format.DateTimeFormatter;


public class RideDetailFragment extends Fragment {

    private GetRideDTO ride;
    private static final String ARG_RIDE = "arg_ride";

    public RideDetailFragment() {
        // Required empty public constructor
    }


    public static RideDetailFragment newInstance(GetRideDTO ride) {
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
            ride = (GetRideDTO) getArguments().getSerializable(ARG_RIDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ride_detail, container, false);

        TextView date = view.findViewById(R.id.tvDate);
        TextView start = view.findViewById(R.id.tvStartLocation);
        TextView end = view.findViewById(R.id.tvEndLocation);
        TextView startTime = view.findViewById(R.id.tvStartTime);
        TextView endTime = view.findViewById(R.id.tvEndTime);
        TextView tvPanicActivated = view.findViewById(R.id.tvPanicActivated);
        TextView price = view.findViewById(R.id.tvPrice);
        TextView tvPassengers = view.findViewById(R.id.tvPassengers);


        if (ride != null) {

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

            if (ride.getStartingTime() != null) {
                setStyledText(date, "Date:", ride.getStartingTime().format(dateFormat));
                setStyledText(startTime, "Start time:", ride.getStartingTime().format(timeFormat));
            }

            if (ride.getFinishedTime() != null) {
                setStyledText(endTime, "End time:", ride.getFinishedTime().format(timeFormat));
            }

            setStyledText(start, "Start location:", ride.getStartPoint());
            setStyledText(end, "End location:", ride.getEndPoint());

            setStyledText(price, "Price:", "$" + ride.getPrice());
            setStyledText(tvPanicActivated, "Panic Activated:",
                    ride.getPanicActivated() != null && ride.getPanicActivated() ? "Yes" : "No");

            if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (GetRidePassengerDTO p : ride.getPassengers()) {
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;• ").append(p.getUsername()).append("<br>");
                }
                setStyledText(tvPassengers, "Passengers: <br>", sb.toString());
            } else {
                setStyledText(tvPassengers, "Passengers:", "None");
            }

        }
            return view;
    }
    private void setStyledText(TextView tv, String label, String value) {
        String html =
                "<b><font color='#133E87'>" + label + "</font></b> " +
                        "<font color='#757474'>" + value + "</font>";

        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
    }


}