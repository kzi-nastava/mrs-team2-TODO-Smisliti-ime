package com.example.getgo.callbacks;

import com.example.getgo.dtos.notification.NotificationDTO;

public interface NotificationListener {
    void onNotificationReceived(NotificationDTO notif, long rideId);
}
