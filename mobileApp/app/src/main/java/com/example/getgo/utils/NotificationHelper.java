package com.example.getgo.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.getgo.R;
import com.example.getgo.activities.MainActivity;
import com.example.getgo.dtos.notification.NotificationDTO;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID = "getgo_notifications";

    public static void showNotification(Context context, NotificationDTO notification, Long rideId) {
        if (context == null) {
            Log.w(TAG, "Context is null, cannot show notification");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted");
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OPEN_RATE_FRAGMENT", false);

        if (rideId != null) {
            intent.putExtra("RIDE_ID", rideId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) (notification.getId() != null ? notification.getId() : System.currentTimeMillis()),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(
                notification.getId() != null ? notification.getId().intValue() : (int) System.currentTimeMillis(),
                builder.build()
        );
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GetGo Notifications";
            String description = "Channel for GetGo app notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
