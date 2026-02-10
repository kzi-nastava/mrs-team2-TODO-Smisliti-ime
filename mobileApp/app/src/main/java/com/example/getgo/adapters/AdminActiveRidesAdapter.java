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
        holder.bind(ride);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvStatus, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRideClick(filtered.get(getAdapterPosition()));
                }
            });
        }

        void bind(GetActiveRideAdminDTO ride) {
            tvDriverName.setText(ride.getDriverName());
            tvStatus.setText(ride.getStatus());
            tvTime.setText(ride.getScheduledTime());
        }
    }
}

