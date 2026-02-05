package com.example.getgo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.dtos.ride.GetRideDTO;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminRideHistoryAdapter extends BaseAdapter {
    private Context context;
    private List<GetRideDTO> rides;
    private LayoutInflater inflater;

    public AdminRideHistoryAdapter(Context context) {
        this.context = context;
        this.rides = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void setRides(List<GetRideDTO> rides) {
        this.rides = rides != null ? rides : new ArrayList<>();
        System.out.println("Adapter prima " + this.rides.size() + " vožnji.");
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rides.size();
    }

    @Override
    public GetRideDTO getItem(int position) {
        if (position < 0 || position >= rides.size()) {
            return null;
        }
        return rides.get(position);
    }

    @Override
    public long getItemId(int position) {
        GetRideDTO ride = getItem(position);
        return ride != null ? ride.getId() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // Koristi postojeći ride_card.xml sa pravim ID-jevima
            convertView = inflater.inflate(R.layout.ride_card, parent, false);
            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvStartLocation = convertView.findViewById(R.id.tvStartLocation);
            holder.tvEndLocation = convertView.findViewById(R.id.tvEndLocation);
            holder.tvStartTime = convertView.findViewById(R.id.tvStartTime);
            holder.tvEndTime = convertView.findViewById(R.id.tvEndTime);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GetRideDTO ride = getItem(position);
        if (ride == null) {
            return convertView;
        }

        // Postavi podatke
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        if (holder.tvDate != null) {
            if (ride.getStartingTime() != null) {
                holder.tvDate.setText(ride.getStartingTime().format(dateFormat));
            } else {
                holder.tvDate.setText("N/A");
            }
        }

        if (holder.tvStartLocation != null) {
            holder.tvStartLocation.setText(shortenAddress(ride.getStartPoint()));
        }

        if (holder.tvEndLocation != null) {
            holder.tvEndLocation.setText(shortenAddress(ride.getEndPoint()));
        }

        if (holder.tvStartTime != null) {
            if (ride.getStartingTime() != null) {
                holder.tvStartTime.setText("Start: " + ride.getStartingTime().format(timeFormat));
            } else {
                holder.tvStartTime.setText("Start: N/A");
            }
        }

        if (holder.tvEndTime != null) {
            if (ride.getFinishedTime() != null) {
                holder.tvEndTime.setText("End: " + ride.getFinishedTime().format(timeFormat));
            } else {
                holder.tvEndTime.setText("End: N/A");
            }
        }

        if (holder.tvPrice != null) {
            holder.tvPrice.setText("$" + String.format("%.2f", ride.getPrice()));
        }

        return convertView;
    }

    private String shortenAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "N/A";
        }
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[0].trim() + ", " + parts[1].trim();
        }
        return parts.length > 0 ? parts[0].trim() : address;
    }

    static class ViewHolder {
        TextView tvDate;
        TextView tvStartLocation;
        TextView tvEndLocation;
        TextView tvStartTime;
        TextView tvEndTime;
        TextView tvPrice;
    }
}

