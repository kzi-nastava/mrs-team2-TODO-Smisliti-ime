package com.example.getgo.api.services;

import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.passenger.UpdatePassengerDTO;
import com.example.getgo.dtos.passenger.UpdatedPassengerDTO;
import com.example.getgo.dtos.user.UpdatedProfilePictureDTO;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface PassengerApiService {

    @GET("api/passenger/profile")
    Call<GetPassengerDTO> getProfile();

    @PUT("api/passenger/profile")
    Call<UpdatedPassengerDTO> updateProfile(@Body UpdatePassengerDTO updatePassengerDTO);

    @PUT("api/passenger/profile/password")
    Call<UpdatedPasswordDTO> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @Multipart
    @POST("api/passenger/profile/picture")
    Call<UpdatedProfilePictureDTO> uploadProfilePicture(@Part MultipartBody.Part file);
}