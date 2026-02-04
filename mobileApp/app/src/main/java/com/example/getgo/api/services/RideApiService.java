package com.example.getgo.api.services;

import com.example.getgo.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetRideTrackingDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RideApiService {
    @POST("api/rides/order")
    Call<CreatedRideResponseDTO> orderRide(@Body CreateRideRequestDTO request);

    @GET("api/rides/{id}/tracking")
    Call<GetRideTrackingDTO> trackRide(@Path("id") Long id);

    @POST("api/rides/{rideId}/inconsistencies")
    Call<CreatedInconsistencyReportDTO> createInconsistencyReport(@Path("rideId") Long rideId, @Body CreateInconsistencyReportDTO report);

    @GET("api/completed-rides/{rideId}/inconsistencies")
    Call<List<GetInconsistencyReportDTO>> getInconsistencyReports(@Path("rideId") Long rideId);

}
