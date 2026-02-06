package com.example.getgo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.getgo.R;
import com.example.getgo.dtos.ride.GetRideDTO;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            convertView = inflater.inflate(R.layout.ride_card, parent, false);
            holder = new ViewHolder();
            holder.tvRoute = convertView.findViewById(R.id.tvRoute);
            holder.tvStartTime = convertView.findViewById(R.id.tvStartTime);
            holder.tvEndTime = convertView.findViewById(R.id.tvEndTime);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvDuration = convertView.findViewById(R.id.tvDuration);
            holder.tvDistance = convertView.findViewById(R.id.tvDistance);
            holder.tvCancelled = convertView.findViewById(R.id.tvCancelled);
            holder.tvPanic = convertView.findViewById(R.id.tvPanic);
            holder.layoutStatusIndicators = convertView.findViewById(R.id.layoutStatusIndicators);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GetRideDTO ride = getItem(position);
        if (ride == null) {
            return convertView;
        }

        // Route
        String startPoint = shortenAddress(ride.getStartPoint());
        String endPoint = shortenAddress(ride.getEndPoint());
        holder.tvRoute.setText(startPoint + " → " + endPoint);

        // Start Time
        if (ride.getStartingTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            holder.tvStartTime.setText(ride.getStartingTime().format(formatter));
        } else {
            holder.tvStartTime.setText("N/A");
        }

        // End Time
        if (ride.getFinishedTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            holder.tvEndTime.setText(ride.getFinishedTime().format(formatter));
        } else {
            holder.tvEndTime.setText("N/A");
        }

        // Price
        if (ride.getPrice() != null) {
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%.0f RSD", ride.getPrice()));
        } else {
            holder.tvPrice.setText("N/A");
        }

        if (ride.getDuration() != null && ride.getDuration() > 0) {
            holder.tvDuration.setText(String.format(Locale.getDefault(), "⏱ %d min", ride.getDuration()));
        } else if (ride.getEstTime() != null && ride.getEstTime() > 0) {
            int minutes = (int) Math.round(ride.getEstTime());
            holder.tvDuration.setText(String.format(Locale.getDefault(), "⏱ %d min", minutes));
        } else {
            holder.tvDuration.setText("⏱ N/A");
        }

        // Distance
        if (ride.getEstDistance() != null && ride.getEstDistance() > 0) {
            holder.tvDistance.setText(String.format(Locale.getDefault(), "📏 %.1f km", ride.getEstDistance()));
        } else {
            holder.tvDistance.setText("📏 N/A");
        }

        // Status indicators
        boolean hasStatus = false;

        if (ride.getCancelled() != null && ride.getCancelled()) {
            holder.tvCancelled.setVisibility(View.VISIBLE);

            String cancelledByText = "❌ Cancelled";
            if (ride.getCancelledBy() != null && !ride.getCancelledBy().isEmpty()) {
                cancelledByText += " by " + ride.getCancelledBy();
            }
            if (ride.getCancelledReason() != null && !ride.getCancelledReason().isEmpty()) {
                cancelledByText += " (" + ride.getCancelledReason() + ")";
            }

            holder.tvCancelled.setText(cancelledByText);
            hasStatus = true;
        } else {
            holder.tvCancelled.setVisibility(View.GONE);
        }

        // Panic status
        if (ride.getPanicActivated() != null && ride.getPanicActivated()) {
            holder.tvPanic.setVisibility(View.VISIBLE);
            hasStatus = true;
        } else {
            holder.tvPanic.setVisibility(View.GONE);
        }

        holder.layoutStatusIndicators.setVisibility(hasStatus ? View.VISIBLE : View.GONE);

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

    static class ViewHolder {
        TextView tvRoute;
        TextView tvStartTime;
        TextView tvEndTime;
        TextView tvPrice;
        TextView tvDuration;
        TextView tvDistance;
        TextView tvCancelled;
        TextView tvPanic;
        LinearLayout layoutStatusIndicators;
    }
}
