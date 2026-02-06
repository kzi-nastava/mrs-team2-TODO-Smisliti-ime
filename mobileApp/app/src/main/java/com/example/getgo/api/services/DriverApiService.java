package com.example.getgo.api.services;

import com.example.getgo.dtos.authentication.GetActivationTokenDTO;
import com.example.getgo.dtos.authentication.UpdateDriverPasswordDTO;
import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.request.CreatedDriverChangeRequestDTO;
import com.example.getgo.dtos.request.UpdateDriverPersonalDTO;
import com.example.getgo.dtos.request.UpdateDriverVehicleDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DriverApiService {

    @GET("api/drivers/activate/{token}")
    Call<GetActivationTokenDTO> validateActivationToken(@Path("token") String token);

    @POST("api/drivers/activate")
    Call<UpdatedPasswordDTO> setDriverPassword(@Body UpdateDriverPasswordDTO updateDriverPasswordDTO);

    @GET("api/drivers/profile")
    Call<GetDriverDTO> getProfile();

    @POST("api/drivers/profile/change-requests/personal")
    Call<CreatedDriverChangeRequestDTO> requestPersonalInfoChange(@Body UpdateDriverPersonalDTO updateDriverPersonalDTO);

    @POST("api/drivers/profile/change-requests/vehicle")
    Call<CreatedDriverChangeRequestDTO> requestVehicleInfoChange(@Body UpdateDriverVehicleDTO updateDriverVehicleDTO);

    @Multipart
    @POST("api/drivers/profile/change-requests/picture")
    Call<CreatedDriverChangeRequestDTO> requestProfilePictureChange(@Part MultipartBody.Part file);

    @PUT("api/drivers/profile/password")
    Call<UpdatedPasswordDTO> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @GET("/api/drivers/rides")
    Call<PageResponse<GetRideDTO>> getDriverRides(
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate
    );

    @GET("/api/drivers/active-locations")
    Call<List<GetActiveDriverLocationDTO>> getActiveDriverLocations();
}
