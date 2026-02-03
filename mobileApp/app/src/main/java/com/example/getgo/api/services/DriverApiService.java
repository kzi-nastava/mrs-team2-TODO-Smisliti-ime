package com.example.getgo.api.services;

import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DriverApiService {
    @GET("/api/drivers/rides")
    Call<PageResponse<GetRideDTO>> getDriverRides(
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );
}
