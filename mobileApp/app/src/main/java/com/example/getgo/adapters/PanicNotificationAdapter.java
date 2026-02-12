package com.example.getgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.dtos.panic.PanicAlertDTO;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PanicNotificationAdapter extends RecyclerView.Adapter<PanicNotificationAdapter.ViewHolder> {

    private List<PanicAlertDTO> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(PanicAlertDTO notification);
    }

    public PanicNotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<PanicAlertDTO> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void removeNotification(Long panicId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getPanicId().equals(panicId)) {
                notifications.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void clearAll() {
        notifications.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panic_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PanicAlertDTO notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPanicTime, tvPanicRideId, tvPanicUserId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPanicTime = itemView.findViewById(R.id.tvPanicTime);
            tvPanicRideId = itemView.findViewById(R.id.tvPanicRideId);
            tvPanicUserId = itemView.findViewById(R.id.tvPanicUserId);
        }

        void bind(PanicAlertDTO notification, OnNotificationClickListener listener) {
            tvPanicRideId.setText("Ride #" + notification.getRideId());
            tvPanicUserId.setText("Triggered by User #" + notification.getTriggeredByUserId());

            if (notification.getTriggeredAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                tvPanicTime.setText(notification.getTriggeredAt().format(formatter));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}

