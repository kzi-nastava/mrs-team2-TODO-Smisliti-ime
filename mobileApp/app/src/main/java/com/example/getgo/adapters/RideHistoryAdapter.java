package com.example.getgo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import com.example.getgo.R;
import com.example.getgo.model.Ride;

public class RideHistoryAdapter extends ArrayAdapter<Ride> {

    private ArrayList<Ride> aRides;

    public RideHistoryAdapter(Context context, ArrayList<Ride> rides) {
        super(context, R.layout.ride_card, rides);
        this.aRides = rides;
    }

    @Override
    public int getCount() {
        return aRides.size();
    }

    @Nullable
    @Override
    public Ride getItem(int position) {
        return aRides.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ride ride = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ride_card, parent, false);
        }


        TextView rideDate = convertView.findViewById(R.id.tvDate);
        TextView rideStartLocation = convertView.findViewById(R.id.tvStartLocation);
        TextView rideEndLocation = convertView.findViewById(R.id.tvEndLocation);
        TextView rideStartTime = convertView.findViewById(R.id.tvStartTime);
        TextView rideEndTime = convertView.findViewById(R.id.tvEndTime);
        TextView ridePrice = convertView.findViewById(R.id.tvPrice);


        if (ride != null){
            rideDate.setText(ride.getStartDate());
            rideStartLocation.setText(ride.getStartLocation());
            rideEndLocation.setText(ride.getEndLocation());
            rideStartTime.setText("Start time: " + ride.getStartTime());
            rideEndTime.setText("End time: " + ride.getEndTime());
            ridePrice.setText(ride.getPrice() + " USD");


        }
        return convertView;
    }


}
