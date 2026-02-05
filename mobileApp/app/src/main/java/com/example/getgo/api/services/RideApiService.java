package com.example.getgo.api.services;

import com.example.getgo.dtos.inconsistencyReport.CreateInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.CreatedInconsistencyReportDTO;
import com.example.getgo.dtos.inconsistencyReport.GetInconsistencyReportDTO;
import com.example.getgo.dtos.ride.CancelRideRequestDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetRideTrackingDTO;
import com.example.getgo.dtos.ride.UpdateRideDTO;
import com.example.getgo.dtos.ride.UpdatedRideDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RideApiService {
    @POST("api/rides/order")
    Call<CreatedRideResponseDTO> orderRide(@Body CreateRideRequestDTO request);

    @GET("api/rides/driver/active")
    Call<GetDriverActiveRideDTO> getDriverActiveRide();

    @PUT("api/rides/{rideId}/accept")
    Call<UpdatedRideDTO> acceptRide(@Path("rideId") Long rideId);

    @PUT("api/rides/{rideId}/start")
    Call<UpdatedRideDTO> startRide(@Path("rideId") Long rideId);

    @PUT("api/rides/{rideId}/finish")
    Call<UpdatedRideDTO> finishRide(@Path("rideId") Long rideId, @Body UpdateRideDTO request);

    @POST("api/rides/{rideId}/cancel/driver")
    Call<Void> cancelRideByDriver(@Path("rideId") Long rideId, @Body CancelRideRequestDTO request);

    @GET("api/rides/{id}/tracking")
    Call<GetRideTrackingDTO> trackRide(@Path("id") Long id);

    @POST("api/rides/{rideId}/inconsistencies")
    Call<CreatedInconsistencyReportDTO> createInconsistencyReport(@Path("rideId") Long rideId, @Body CreateInconsistencyReportDTO report);

    @GET("api/completed-rides/{rideId}/inconsistencies")
    Call<List<GetInconsistencyReportDTO>> getInconsistencyReports(@Path("rideId") Long rideId);

}
