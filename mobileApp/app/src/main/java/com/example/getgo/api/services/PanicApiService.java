package com.example.getgo.api.services;

import com.example.getgo.dtos.panic.PanicAlertDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PanicApiService {

    @GET("api/panic/admin/unread")
    Call<List<PanicAlertDTO>> getUnreadPanics();

    @PUT("api/panic/admin/read/{panicId}")
    Call<Void> markPanicAsRead(@Path("panicId") Long panicId);

    @PUT("api/panic/admin/read-all")
    Call<Void> markAllPanicsAsRead();
}

