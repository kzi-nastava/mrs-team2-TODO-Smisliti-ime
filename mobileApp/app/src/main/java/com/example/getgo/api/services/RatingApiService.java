package com.example.getgo.api.services;

import com.example.getgo.dtos.rating.CreateRatingDTO;
import com.example.getgo.dtos.rating.CreatedRatingDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RatingApiService {
    @GET("/api/ratings/ride/{rideId}")
    Call<List<GetRatingDTO>> getRatings(@Path("rideId") Long rideId);

    @GET("/api/ratings/driver/{driverId}")
    Call<List<GetRatingDTO>> getRatingsByDriver(@Path("driverId") Long driverId);

    @POST("/api/ratings")
    Call<CreatedRatingDTO> createRating(@Query("rideId") Long rideId, @Body CreateRatingDTO rating);
}

