package com.example.getgo.api.services;

import com.example.getgo.dtos.notification.CreateNotificationRequestDTO;
import com.example.getgo.dtos.notification.NotificationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotificationApiService {

    @GET("api/notifications/me")
    Call<List<NotificationDTO>> getNotifications();
}
