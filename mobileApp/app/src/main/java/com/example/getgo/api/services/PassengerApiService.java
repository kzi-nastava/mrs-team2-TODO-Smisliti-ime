package com.example.getgo.api.services;

import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.ride.GetReorderRideDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PassengerApiService {

    // Get passenger by ID (for admin viewing passenger details)
    @GET("api/passenger/profile/{id}")
    Call<GetPassengerDTO> getPassengerProfileById(@Path("id") Long passengerId);

    // Get passenger ride history with pagination and date filter
    @GET("api/passenger/rides")
    Call<PageResponse<GetRideDTO>> getPassengerRides(
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );

    // Fetch single ride for re-order (returns GetReorderRideDTO)
    @GET("api/passenger/rides/{id}")
    Call<GetReorderRideDTO> getRideForReorder(@Path("id") Long rideId);
}
