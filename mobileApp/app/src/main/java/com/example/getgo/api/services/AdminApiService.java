package com.example.getgo.api.services;

import com.example.getgo.dtos.admin.GetAdminDTO;
import com.example.getgo.dtos.admin.UpdateAdminDTO;
import com.example.getgo.dtos.admin.UpdatedAdminDTO;
import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.dtos.driver.CreateDriverDTO;
import com.example.getgo.dtos.driver.CreatedDriverDTO;
import com.example.getgo.dtos.request.AcceptDriverChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverAvatarChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverVehicleChangeRequestDTO;
import com.example.getgo.dtos.request.GetPersonalDriverChangeRequestDTO;
import com.example.getgo.dtos.request.RejectDriverChangeRequestDTO;
import com.example.getgo.dtos.user.CreatedUserDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminApiService {

    @PUT("api/admin/users/{id}/block")
    Call<CreatedUserDTO> blockUser(@Path("id") Long id);

    @PUT("api/admin/users/{id}/unblock")
    Call<CreatedUserDTO> unblockUser(@Path("id") Long id);

    @GET("api/admin/profile")
    Call<GetAdminDTO> getProfile();

    @PUT("api/admin/profile")
    Call<UpdatedAdminDTO> updateProfile(@Body UpdateAdminDTO updateAdminDTO);

    @PUT("api/admin/profile/password")
    Call<UpdatedPasswordDTO> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @POST("api/admin/drivers/register")
    Call<CreatedDriverDTO> registerDriver(@Body CreateDriverDTO createDriverDTO);

    @GET("api/admin/driver-change-requests/personal")
    Call<List<GetPersonalDriverChangeRequestDTO>> getPendingPersonalChangeRequests();

    @PUT("api/admin/driver-change-requests/personal/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approvePersonalChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/personal/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectPersonalChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );

    @GET("api/admin/driver-change-requests/vehicle")
    Call<List<GetDriverVehicleChangeRequestDTO>> getPendingVehicleChangeRequests();

    @PUT("api/admin/driver-change-requests/vehicle/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approveVehicleChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/vehicle/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectVehicleChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );

    @GET("api/admin/driver-change-requests/picture")
    Call<List<GetDriverAvatarChangeRequestDTO>> getPendingPictureChangeRequests();

    @PUT("api/admin/driver-change-requests/picture/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approvePictureChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/picture/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectPictureChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );
}