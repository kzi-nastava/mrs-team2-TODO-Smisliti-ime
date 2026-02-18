package com.example.getgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.dtos.activeRide.GetActiveRideAdminDTO;

import java.util.ArrayList;
import java.util.List;

public class AdminActiveRidesAdapter
        extends RecyclerView.Adapter<AdminActiveRidesAdapter.ViewHolder> {

    private List<GetActiveRideAdminDTO> all = new ArrayList<>();
    private List<GetActiveRideAdminDTO> filtered = new ArrayList<>();
    private final OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(GetActiveRideAdminDTO ride);
    }

    public AdminActiveRidesAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }


    public void setData(List<GetActiveRideAdminDTO> data) {
        all = data;
        filtered = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        filtered.clear();
        for (GetActiveRideAdminDTO r : all) {
            if (r.getDriverName().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(r);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_ride, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GetActiveRideAdminDTO ride = filtered.get(position);

        holder.tvStatus.setText("Status: " + ride.getStatus());
        holder.tvDriverName.setText("🚗 " + ride.getDriverName());
        holder.tvActualStartTime.setText("Start: " + formatTime(ride.getActualStartTime()));
        holder.tvScheduledTime.setText("Scheduled: " + formatTime(ride.getScheduledTime()));
        holder.tvVehicleType.setText("Vehicle: " + ride.getVehicleType());
        holder.tvEstimatedPrice.setText(String.format("%.2f RSD", ride.getEstimatedPrice()));

        holder.itemView.setOnClickListener(v -> listener.onRideClick(ride));
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvStatus, tvActualStartTime, tvScheduledTime, tvVehicleType, tvEstimatedPrice;

        ViewHolder(View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvActualStartTime = itemView.findViewById(R.id.tvActualStartTime);
            tvScheduledTime = itemView.findViewById(R.id.tvScheduledTime);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvEstimatedPrice = itemView.findViewById(R.id.tvEstimatedPrice);
        }
    }


    private String formatTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(dateTimeStr);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            return ldt.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}

