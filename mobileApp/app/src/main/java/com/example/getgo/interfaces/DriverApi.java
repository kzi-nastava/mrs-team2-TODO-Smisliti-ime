package com.example.getgo.interfaces;

import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.dtos.ride.GetRideDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DriverApi {
    @GET("/api/drivers/{id}/rides")
    Call<List<GetRideDTO>> getDriverRides(@Path("id") Long driverId);
}
