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
import java.util.List;

import com.example.getgo.R;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.interfaces.OnRideClickListener;
import com.example.getgo.model.Ride;

public class RideHistoryAdapter extends ArrayAdapter<GetRideDTO> {

    private ArrayList<GetRideDTO> rides = new ArrayList<>();

    private OnRideClickListener listener;

    public RideHistoryAdapter(Context context) {
        super(context, R.layout.ride_card);
    }

    public void setRides(List<GetRideDTO> newRides) {
        rides.clear();
        if (newRides != null) {
            rides.addAll(newRides);
            System.out.println("Adapter prima " + newRides.size() + " vožnji.");
        }
        notifyDataSetChanged();
    }

    public void setOnRideClickListener(OnRideClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return rides.size();
    }

    @Nullable
    @Override
    public GetRideDTO getItem(int position) {
        return rides.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GetRideDTO ride = getItem(position);
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
//            rideDate.setText(ride.getStartDate());
//            rideStartLocation.setText(ride.getStartLocation());
//            rideEndLocation.setText(ride.getEndLocation());
//            rideStartTime.setText("Start time: " + ride.getStartTime());
//            rideEndTime.setText("End time: " + ride.getEndTime());
//            ridePrice.setText("$"+ride.getPrice());

            rideDate.setText(ride.getStartingTime() != null ? ride.getStartingTime().toLocalDate().toString() : "");
            rideStartLocation.setText(ride.getStartPoint());
            rideEndLocation.setText(ride.getEndPoint());
            rideStartTime.setText(ride.getStartingTime() != null ? "Start: " + ride.getStartingTime().toLocalTime().toString() : "");
            rideEndTime.setText(ride.getFinishedTime() != null ? "End: " + ride.getFinishedTime().toLocalTime().toString() : "");
            ridePrice.setText(ride.getPrice() != null ? "$" + ride.getPrice() : "$0");

            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRideClick(ride);
                }
            });

        }
        return convertView;
    }


}
