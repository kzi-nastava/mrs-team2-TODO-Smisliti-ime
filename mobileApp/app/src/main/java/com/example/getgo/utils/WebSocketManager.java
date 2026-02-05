package com.example.getgo.utils;

import android.content.Context;
import android.util.Log;

import com.example.getgo.dtos.driver.GetDriverLocationDTO;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetRideFinishedDTO;
import com.example.getgo.dtos.ride.GetRideStatusUpdateDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "http://10.0.2.2:8080/socket/websocket";

    private StompClient stompClient;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Gson gson;

    public interface RideAssignedListener {
        void onRideAssigned(GetDriverActiveRideDTO ride);
    }

    public interface RideStatusUpdateListener {
        void onStatusUpdate(GetRideStatusUpdateDTO update);
    }

    public interface RideFinishedListener {
        void onRideFinished(GetRideFinishedDTO finished);
    }

    public interface DriverLocationListener {
        void onLocationUpdate(GetDriverLocationDTO location);
    }

    public WebSocketManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .create();
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

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
        compositeDisposable.clear();
    }
}