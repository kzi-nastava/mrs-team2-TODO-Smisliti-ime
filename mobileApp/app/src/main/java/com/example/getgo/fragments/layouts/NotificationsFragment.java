package com.example.getgo.fragments.layouts;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.NotificationApiService;
import com.example.getgo.dtos.notification.NotificationDTO;
import com.example.getgo.utils.ToastHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    private RecyclerView rvNotifications;
    private NotificationsAdapter adapter;
    private NotificationApiService service;

    public NotificationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        rvNotifications = root.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationsAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);

        service = ApiClient.getNotificationApiService();
        loadNotifications();

        // diagnostic: attempt to detect WebSocketManager presence on parent activity (optional)
        checkWebSocketManagerOnActivity();

        return root;
    }

    private void loadNotifications() {
        service.getNotifications().enqueue(new Callback<List<NotificationDTO>>() {
            @Override
            public void onResponse(Call<List<NotificationDTO>> call, Response<List<NotificationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDTO>> call, Throwable t) {
                Log.e(TAG, "Failed to load notifications", t);
                ToastHelper.showError(requireContext(), "Failed to load notifications", t.getMessage());
            }
        });
    }

    // Public method to allow external callers (e.g. MainActivity / WebSocket) to refresh the list
    public void refreshNotifications() {
        loadNotifications();
    }

    // Simple adapter
    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {
        private List<NotificationDTO> items;

        public NotificationsAdapter(List<NotificationDTO> items) { this.items = items; }

        public void setItems(List<NotificationDTO> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            NotificationDTO n = items.get(position);
            holder.title.setText(n.getTitle() != null ? n.getTitle() : "(no title)");

            // If message/reason missing and notification is a cancel type, show "/"
            String messageText = n.getMessage();
            String typeStr = null;
            try {
                Object t = n.getType(); // defensive: type might be enum or string
                if (t != null) typeStr = t.toString();
            } catch (Exception ignored) {}

            boolean isCancelType = false;
            if (typeStr != null) {
                isCancelType = typeStr.toUpperCase().contains("CANCEL");
            } else {
                // fallback: also check title for cancel keyword
                String title = n.getTitle();
                if (title != null && title.toUpperCase().contains("CANCEL")) isCancelType = true;
            }

            if ((messageText == null || messageText.trim().isEmpty()) && isCancelType) {
                messageText = "/"; // user requested placeholder
            } else if (messageText == null) {
                messageText = "";
            }

            holder.message.setText(messageText);
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        public void addNotification(NotificationDTO notification) {
            if (items == null) items = new ArrayList<>();
            items.add(0, notification); // to the top
            notifyItemInserted(0);
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, message;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvNotifTitle);
                message = itemView.findViewById(R.id.tvNotifMessage);
            }
        }
    }

    private void checkWebSocketManagerOnActivity() {
        try {
            if (getActivity() == null) {
                Log.d("NOTIF_WS", "No activity attached to fragment");
                return;
            }
            // Reflectively inspect 'webSocketManager' field on activity (if present)
            Field f = null;
            Class<?> cls = getActivity().getClass();
            while (cls != null) {
                try {
                    f = cls.getDeclaredField("webSocketManager");
                    break;
                } catch (NoSuchFieldException nsfe) {
                    cls = cls.getSuperclass();
                }
            }
            if (f == null) {
                Log.d("NOTIF_WS", "Activity does not expose webSocketManager field.");
                return;
            }
            f.setAccessible(true);
            Object wsObj = f.get(getActivity());
            if (wsObj != null) {
                Log.d("NOTIF_WS", "Detected activity.webSocketManager != null (websocket should be active).");
            } else {
                Log.w("NOTIF_WS", "activity.webSocketManager is null — WebSocket likely not connected.");
            }
        } catch (Exception ex) {
            Log.e("NOTIF_WS", "Failed to inspect WebSocketManager on activity", ex);
        }
    }

    public void addNotification(NotificationDTO notification) {
        if (adapter != null) {
            adapter.addNotification(notification);

            // Scroll to top to show new notification immediately
            if (rvNotifications != null) rvNotifications.scrollToPosition(0);
        }
    }
}
