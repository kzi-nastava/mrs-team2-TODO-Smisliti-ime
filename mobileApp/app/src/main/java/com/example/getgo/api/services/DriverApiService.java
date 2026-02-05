package com.example.getgo.api.services;

import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DriverApiService {
    @GET("/api/drivers/rides")
    Call<PageResponse<GetRideDTO>> getDriverRides(
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );

    @GET("/api/drivers/active-locations")
    Call<List<GetActiveDriverLocationDTO>> getActiveDriverLocations();

    // Get driver by ID (for admin/passenger viewing driver details)
    @GET("api/drivers/profile/{id}")
    Call<GetDriverDTO> getDriverProfileById(@Path("id") Long driverId);
}
