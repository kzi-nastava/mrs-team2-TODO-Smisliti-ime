package com.example.getgo.api.services;

import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApiService {

    @GET("api/admin/rides/passenger")
    Call<PageResponse<GetRideDTO>> getPassengerRides(
            @Query("email") String email,
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );

    @GET("api/admin/rides/driver")
    Call<PageResponse<GetRideDTO>> getDriverRides(
            @Query("email") String email,
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );

    @GET("api/admin/rides/passenger/{rideId}")
    Call<GetRideDTO> getPassengerRideById(
            @Path("rideId") Long rideId,
            @Query("email") String email
    );

    @GET("api/admin/rides/driver/{rideId}")
    Call<GetRideDTO> getDriverRideById(
            @Path("rideId") Long rideId,
            @Query("email") String email
    );
}
