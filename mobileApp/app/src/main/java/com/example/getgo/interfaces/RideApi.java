package com.example.getgo.interfaces;

import com.example.getgo.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import com.example.getgo.dtos.rating.CreateRatingDTO;
import com.example.getgo.dtos.rating.CreatedRatingDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.dtos.ride.GetRideTrackingDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RideApi {
    @GET("api/rides/{id}/tracking")
    Call<GetRideTrackingDTO> trackRide(@Path("id") Long id);

    @POST("api/rides/{rideId}/inconsistencies")
    Call<CreatedInconsistencyReportDTO> createInconsistencyReport(@Path("rideId") Long rideId, @Body CreateInconsistencyReportDTO report);

}
