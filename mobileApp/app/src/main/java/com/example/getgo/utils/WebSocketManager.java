package com.example.getgo.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.NotificationApiService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.getgo.R;
import com.example.getgo.activities.MainActivity;
import com.example.getgo.callbacks.SupportChatMessageListener;
import com.example.getgo.dtos.driver.GetDriverLocationDTO;
import com.example.getgo.dtos.notification.NotificationDTO;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetRideFinishedDTO;
import com.example.getgo.dtos.ride.GetRideStatusUpdateDTO;
import com.example.getgo.dtos.ride.GetRideStoppedEarlyDTO;
import com.example.getgo.dtos.ride.LinkedRideAcceptedDTO;
import com.example.getgo.dtos.supportChat.GetMessageDTO;
import com.example.getgo.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";

    public static final String WS_URL = "http://10.0.2.2:8080/socket/websocket";
    // public static final String WS_URL = "wss://nonpossibly-nonderivable-teddy.ngrok-free.dev/socket/websocket";


    private StompClient stompClient;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Gson gson;

    // Optional auth token to include in STOMP CONNECT headers
    private String authToken = null;

    // Connection listener called when socket is opened
    private Runnable connectionListener = null;

    // Disposable for unread-notifications polling (kept in compositeDisposable as well)
    private Disposable unreadPollingDisposable = null;

    public interface RideAssignedListener {
        void onRideAssigned(GetDriverActiveRideDTO ride);
    }

    public interface RideStatusUpdateListener {
        void onStatusUpdate(GetRideStatusUpdateDTO update);
    }

    public interface RideFinishedListener {
        void onRideFinished(GetRideFinishedDTO finished);
    }

    public interface RideAcceptedListener {
        void onRideAccepted(GetDriverActiveRideDTO ride);
    }

    public interface DriverLocationListener {
        void onLocationUpdate(GetDriverLocationDTO location);
    }

    public interface RideStoppedEarlyListener {
        void onRideStopped(GetRideStoppedEarlyDTO stopped);
    }

    // Listener for receiving a batch (list) of notifications (unread)
    public interface UserNotificationsListener {
        void onNotifications(List<com.example.getgo.dtos.notification.NotificationDTO> notifications);
    }

    // Listener for receiving a single notification
    public interface SingleUserNotificationListener {
        void onNotification(com.example.getgo.dtos.notification.NotificationDTO notification);
    }

    public interface NotificationListener {
        void onNotificationReceived(NotificationDTO notification, Long rideId);
    }

    private NotificationListener notificationListener;

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public interface LinkedRideAcceptedListener {
        void onLinkedRideAccepted(LinkedRideAcceptedDTO linkedRideAccepted);
    }

    private LinkedRideAcceptedListener linkedRideAcceptedListener;

    public void setLinkedRideAcceptedListener(LinkedRideAcceptedListener listener) {
        this.linkedRideAcceptedListener = listener;
    }

    public WebSocketManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .create();
    }

    /**
     * Set optional JWT token to include in CONNECT headers
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Set a callback that will be invoked on the UI thread when the socket connection opens
     */
    public void setConnectionListener(Runnable listener) {
        this.connectionListener = listener;
    }

    public void connect() {
        if (stompClient != null && stompClient.isConnected()) {
            Log.d(TAG, "Already connected");
            return;
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        stompClient.withClientHeartbeat(10000).withServerHeartbeat(10000);

        Disposable lifecycleDisposable = stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "WebSocket connected");
                            // notify listener if set
                            if (connectionListener != null) {
                                try {
                                    connectionListener.run();
                                } catch (Exception ex) {
                                    Log.e(TAG, "Connection listener failed", ex);
                                }
                            }
                            break;
                        case CLOSED:
                            Log.d(TAG, "WebSocket closed");
                            break;
                        case ERROR:
                            Log.e(TAG, "WebSocket error", lifecycleEvent.getException());
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.w(TAG, "Server heartbeat failed");
                            break;
                    }
                });

        compositeDisposable.add(lifecycleDisposable);

        Log.d(TAG, "Connecting to WebSocket at: " + WS_URL);
        stompClient.connect();
        Log.d(TAG, "WebSocketManager: user should now subscribe to topics for notifications");

        // Build connect headers if auth token present
        if (authToken != null && !authToken.isEmpty()) {
            List<StompHeader> headers = new ArrayList<>();
            headers.add(new StompHeader("Authorization", "Bearer " + authToken));
            stompClient.connect(headers);
        } else {
            stompClient.connect();
        }
    }

    public void subscribeToUserNotifications(Long userId, UserNotificationsListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/user/" + userId + "/notifications";
        Log.d(TAG, "Subscribing to user notifications (bulk): " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    try {
                        Log.d(TAG, "User notifications payload: " + topicMessage.getPayload());
                        com.example.getgo.dtos.notification.NotificationDTO[] arr = gson.fromJson(topicMessage.getPayload(), com.example.getgo.dtos.notification.NotificationDTO[].class);
                        List<com.example.getgo.dtos.notification.NotificationDTO> list = arr != null ? Arrays.asList(arr) : java.util.Collections.emptyList();
                        listener.onNotifications(list);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse user notifications payload", ex);
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on user notifications topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToUserNotification(Long userId, SingleUserNotificationListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/user/" + userId + "/notification";
        Log.d(TAG, "Subscribing to user notification (single): " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    try {
                        Log.d(TAG, "User notification payload: " + topicMessage.getPayload());
                        com.example.getgo.dtos.notification.NotificationDTO dto = gson.fromJson(topicMessage.getPayload(), com.example.getgo.dtos.notification.NotificationDTO.class);
                        listener.onNotification(dto);
                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse single user notification payload", ex);
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on user notification topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToRideAssigned(String driverEmail, RideAssignedListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/driver/" + driverEmail + "/ride-assigned";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride assigned: " + topicMessage.getPayload());
                    GetDriverActiveRideDTO ride = gson.fromJson(topicMessage.getPayload(), GetDriverActiveRideDTO.class);
                    listener.onRideAssigned(ride);
                }, throwable -> {
                    Log.e(TAG, "Error on ride assigned topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToRideStatusUpdates(String driverEmail, RideStatusUpdateListener listener) {
        if (stompClient == null) return;

        String topic = "/socket-publisher/driver/" + driverEmail + "/status-update";
        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Status update: " + topicMessage.getPayload());
                    GetRideStatusUpdateDTO update = gson.fromJson(topicMessage.getPayload(), GetRideStatusUpdateDTO.class);
                    listener.onStatusUpdate(update);
                }, throwable -> {
                    Log.e(TAG, "Error on status update topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToRideFinished(String driverEmail, RideFinishedListener listener) {
        if (stompClient == null) return;

        String topic = "/socket-publisher/driver/" + driverEmail + "/ride-finished";
        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride finished: " + topicMessage.getPayload());
                    GetRideFinishedDTO finished = gson.fromJson(topicMessage.getPayload(), GetRideFinishedDTO.class);
                    listener.onRideFinished(finished);
                }, throwable -> {
                    Log.e(TAG, "Error on ride finished topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToPassengerRideAccepted(Long rideId, RideAcceptedListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/ride-accepted";
        Log.d(TAG, "Subscribing to passenger ride accepted: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride accepted: " + topicMessage.getPayload());
                    GetDriverActiveRideDTO ride = gson.fromJson(topicMessage.getPayload(), GetDriverActiveRideDTO.class);
                    listener.onRideAccepted(ride);
                }, throwable -> {
                    Log.e(TAG, "Error on ride accepted topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToDriverLocation(String driverEmail, DriverLocationListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/driver/" + driverEmail + "/location";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Driver location: " + topicMessage.getPayload());
                    GetDriverLocationDTO location = gson.fromJson(topicMessage.getPayload(), GetDriverLocationDTO.class);
                    listener.onLocationUpdate(location);
                }, throwable -> {
                    Log.e(TAG, "Error on driver location topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToRideDriverLocation(Long rideId, DriverLocationListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/driver-location";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Driver location (for ride): " + topicMessage.getPayload());
                    GetDriverLocationDTO location = gson.fromJson(topicMessage.getPayload(), GetDriverLocationDTO.class);
                    listener.onLocationUpdate(location);
                }, throwable -> {
                    Log.e(TAG, "Error on driver location for ride topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToPassengerRideStatusUpdates(Long rideId, RideStatusUpdateListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/status-update";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride status update: " + topicMessage.getPayload());
                    GetRideStatusUpdateDTO update = gson.fromJson(topicMessage.getPayload(), GetRideStatusUpdateDTO.class);
                    listener.onStatusUpdate(update);
                }, throwable -> {
                    Log.e(TAG, "Error on ride status update topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToPassengerRideFinished(Long rideId, RideFinishedListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/ride-finished";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride finished: " + topicMessage.getPayload());
                    GetRideFinishedDTO finished = gson.fromJson(topicMessage.getPayload(), GetRideFinishedDTO.class);
                    listener.onRideFinished(finished);
                }, throwable -> {
                    Log.e(TAG, "Error on ride finished topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToPassengerRideStopped(Long rideId, RideStoppedEarlyListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/ride-stopped";
        Log.d(TAG, "Subscribing to: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride stopped early: " + topicMessage.getPayload());
                    GetRideStoppedEarlyDTO stopped = gson.fromJson(topicMessage.getPayload(), GetRideStoppedEarlyDTO.class);
                    listener.onRideStopped(stopped);
                }, throwable -> {
                    Log.e(TAG, "Error on ride stopped topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToRideCancelled(Long rideId, RideCancelledListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/ride/" + rideId + "/ride-cancelled";
        Log.d(TAG, "Subscribing to ride cancelled: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Ride cancelled: " + topicMessage.getPayload());
                    com.example.getgo.dtos.ride.GetRideCancelledDTO cancelled = gson.fromJson(topicMessage.getPayload(), com.example.getgo.dtos.ride.GetRideCancelledDTO.class);
                    listener.onRideCancelled(cancelled);
                }, throwable -> {
                    Log.e(TAG, "Error on ride cancelled topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void subscribeToDriverRideCancelled(String driverEmail, DriverRideCancelledListener listener) {
        if (stompClient == null) {
            Log.e(TAG, "Cannot subscribe - client is null");
            return;
        }

        String topic = "/socket-publisher/driver/" + driverEmail + "/ride-cancelled";
        Log.d(TAG, "Subscribing to driver ride cancelled: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Driver ride cancelled: " + topicMessage.getPayload());
                    com.example.getgo.dtos.ride.GetRideCancelledDTO cancelled = gson.fromJson(topicMessage.getPayload(), com.example.getgo.dtos.ride.GetRideCancelledDTO.class);
                    listener.onDriverRideCancelled(cancelled);
                }, throwable -> {
                    Log.e(TAG, "Error on driver ride cancelled topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public void requestUnreadNotifications() {
        NotificationApiService service = ApiClient.getNotificationApiService();

        service.requestUnreadNotifications().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Requested unread notifications via API");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to request unread notifications via API", t);
            }
        });
    }

    public void startUnreadNotificationsPolling(long intervalSeconds) {
        if (intervalSeconds <= 0) return;
        stopUnreadNotificationsPolling();

        Log.d(TAG, "Starting unread notifications polling interval=" + intervalSeconds + "s");

        // Start immediately, then every intervalSeconds
        unreadPollingDisposable = Observable.interval(0, intervalSeconds, TimeUnit.SECONDS)
                .subscribe(tick -> {
                    try {
                        if (stompClient != null && stompClient.isConnected()) {
                            requestUnreadNotifications();
                        } else {
                            Log.d(TAG, "Skipping requestUnreadNotifications - socket not connected yet");
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error during unread notifications polling", ex);
                    }
                }, throwable -> Log.e(TAG, "Unread notifications polling error", throwable));

        compositeDisposable.add(unreadPollingDisposable);
    }

    public void stopUnreadNotificationsPolling() {
        if (unreadPollingDisposable != null && !unreadPollingDisposable.isDisposed()) {
            try {
                unreadPollingDisposable.dispose();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to dispose unreadPollingDisposable", ex);
            }
            unreadPollingDisposable = null;
        }
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        compositeDisposable.clear();
        unreadPollingDisposable = null;
    }

    public void subscribeToChat(Long chatId, String currentUserType, SupportChatMessageListener listener) {
        if (stompClient == null) return;

        String topic = "/socket-publisher/chat/" + chatId;

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    GetMessageDTO dto = gson.fromJson(topicMessage.getPayload(), GetMessageDTO.class);

                    ChatMessage message = new ChatMessage(dto, currentUserType);

                    listener.onNewMessage(message);
                }, throwable -> {
                    Log.e(TAG, "Chat socket error", throwable);
                });

        compositeDisposable.add(disposable);
    }

    public interface RideCancelledListener {
        void onRideCancelled(com.example.getgo.dtos.ride.GetRideCancelledDTO dto);
    }

    public interface DriverRideCancelledListener {
        void onDriverRideCancelled(com.example.getgo.dtos.ride.GetRideCancelledDTO dto);
    }

    public void subscribeToLinkedRideAccepted(Long passengerId) {
        if (stompClient == null) return;

        String topic = "/socket-publisher/user/" + passengerId + "/linked-ride-accepted";
        Log.d(TAG, "Subscribing to linked ride accepted: " + topic);

        Disposable disposable = stompClient.topic(topic)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "WS payload received on topic: " + topic + " -> " + topicMessage.getPayload());
                    Log.d(TAG, "Linked ride accepted: " + topicMessage.getPayload());

                    try {
                        LinkedRideAcceptedDTO linkedRideAccepted = gson.fromJson(
                                topicMessage.getPayload(), LinkedRideAcceptedDTO.class);

                        if (linkedRideAcceptedListener != null) {
                            linkedRideAcceptedListener.onLinkedRideAccepted(linkedRideAccepted);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse linked ride accepted DTO", e);
                    }

                }, throwable -> {
                    Log.e(TAG, "Error on linked ride accepted topic", throwable);
                });

        compositeDisposable.add(disposable);
    }

}
