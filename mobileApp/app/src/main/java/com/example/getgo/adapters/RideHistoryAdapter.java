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
import java.util.Locale;
import java.time.format.DateTimeFormatter;

import com.example.getgo.R;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.callbacks.OnRideClickListener;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ride_card, parent, false);
        }

        GetRideDTO ride = rides.get(position);

        TextView tvRoute = convertView.findViewById(R.id.tvRoute);
        TextView tvStartTime = convertView.findViewById(R.id.tvStartTime);
        TextView tvEndTime = convertView.findViewById(R.id.tvEndTime);
        TextView tvPrice = convertView.findViewById(R.id.tvPrice);
        TextView tvDuration = convertView.findViewById(R.id.tvDuration);
        TextView tvDistance = convertView.findViewById(R.id.tvDistance);
        TextView tvCancelled = convertView.findViewById(R.id.tvCancelled);
        TextView tvPanic = convertView.findViewById(R.id.tvPanic);
        LinearLayout layoutStatusIndicators = convertView.findViewById(R.id.layoutStatusIndicators);

        String startPoint = shortenAddress(ride.getStartPoint());
        String endPoint = shortenAddress(ride.getEndPoint());
        tvRoute.setText(startPoint + " → " + endPoint);

        // Start Time
        if (ride.getStartingTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            tvStartTime.setText(ride.getStartingTime().format(formatter));
        } else {
            tvStartTime.setText("N/A");
        }

        // End Time
        if (ride.getFinishedTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            tvEndTime.setText(ride.getFinishedTime().format(formatter));
        } else {
            tvEndTime.setText("N/A");
        }

        // Price
        if (ride.getPrice() != null) {
            tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.getPrice()));
        } else {
            tvPrice.setText("N/A");
        }

        if (ride.getDuration() != null && ride.getDuration() > 0) {
            tvDuration.setText(String.format(Locale.getDefault(), "⏱ %d min", ride.getDuration()));
        } else if (ride.getEstTime() != null && ride.getEstTime() > 0) {
            int minutes = (int) Math.round(ride.getEstTime());
            tvDuration.setText(String.format(Locale.getDefault(), "⏱ %d min", minutes));
        } else {
            tvDuration.setText("⏱ N/A");
        }

        // Distance
        if (ride.getEstDistance() != null && ride.getEstDistance() > 0) {
            tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", ride.getEstDistance()));
        } else {
            tvDistance.setText("N/A");
        }

        // Status indicators
        boolean hasStatus = false;

        if (ride.getCancelled() != null && ride.getCancelled()) {
            tvCancelled.setVisibility(View.VISIBLE);

            String cancelledByText = "Cancelled";
            if (ride.getCancelledBy() != null && !ride.getCancelledBy().isEmpty()) {
                cancelledByText += " by " + ride.getCancelledBy();
            }
            if (ride.getCancelledReason() != null && !ride.getCancelledReason().isEmpty()) {
                cancelledByText += " (" + ride.getCancelledReason() + ")";
            }

            tvCancelled.setText(cancelledByText);
            hasStatus = true;
        } else {
            tvCancelled.setVisibility(View.GONE);
        }

        // Panic status
        if (ride.getPanicActivated() != null && ride.getPanicActivated()) {
            tvPanic.setVisibility(View.VISIBLE);
            hasStatus = true;
        } else {
            tvPanic.setVisibility(View.GONE);
        }

        layoutStatusIndicators.setVisibility(hasStatus ? View.VISIBLE : View.GONE);

        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRideClick(ride);
            }
        });

        return convertView;
    }

    private String shortenAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "Unknown";
        }

        // Split by comma and take first 2-3 parts
        String[] parts = address.split(",");
        if (parts.length == 0) return address;

        StringBuilder shortened = new StringBuilder();
        int maxParts = Math.min(3, parts.length);

        for (int i = 0; i < maxParts; i++) {
            String part = parts[i].trim();

            if (part.startsWith("Град ")) {
                part = part.replace("Град ", "");
            } else if (part.startsWith("Општина ")) {
                part = part.replace("Општина ", "");
            }

            if (i > 0) shortened.append(", ");
            shortened.append(part);
        }

        return shortened.toString();
    }


}
