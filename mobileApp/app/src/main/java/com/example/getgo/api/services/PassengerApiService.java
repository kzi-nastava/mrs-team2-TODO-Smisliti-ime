package com.example.getgo.api.services;

import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.passenger.UpdatePassengerDTO;
import com.example.getgo.dtos.passenger.UpdatedPassengerDTO;
import com.example.getgo.dtos.user.UpdatedProfilePictureDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface PassengerApiService {

    @GET("api/passenger/profile")
    Call<GetPassengerDTO> getProfile();

    // Get passenger by ID (for admin viewing passenger details)
    @GET("api/passenger/profile/{id}")
    Call<GetPassengerDTO> getPassengerProfileById(@Path("id") Long passengerId);

    @PUT("api/passenger/profile")
    Call<UpdatedPassengerDTO> updateProfile(@Body UpdatePassengerDTO updatePassengerDTO);

    // Get passenger ride history with pagination and date filter
    @GET("api/passenger/rides")
    Call<PageResponse<GetRideDTO>> getPassengerRides(
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate,
            @Query("sort") String sort,
            @Query("direction") String direction
    );

    @PUT("api/passenger/profile/password")
    Call<UpdatedPasswordDTO> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @Multipart
    @POST("api/passenger/profile/picture")
    Call<UpdatedProfilePictureDTO> uploadProfilePicture(@Part MultipartBody.Part file);

    // Fetch single ride for re-order (returns GetReorderRideDTO)
    @GET("api/passenger/rides/{id}")
    Call<GetRideDTO> getRideForReorder(@Path("id") Long rideId);
}