package com.example.getgo.api.services;

import com.example.getgo.dtos.activeRide.GetActiveRideAdminDTO;
import com.example.getgo.dtos.activeRide.GetActiveRideAdminDetailsDTO;
import com.example.getgo.dtos.admin.GetAdminDTO;
import com.example.getgo.dtos.admin.UpdateAdminDTO;
import com.example.getgo.dtos.admin.UpdatedAdminDTO;
import com.example.getgo.dtos.authentication.UpdatePasswordDTO;
import com.example.getgo.dtos.authentication.UpdatedPasswordDTO;
import com.example.getgo.dtos.driver.CreateDriverDTO;
import com.example.getgo.dtos.driver.CreatedDriverDTO;
import com.example.getgo.dtos.general.Page;
import com.example.getgo.dtos.request.AcceptDriverChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverAvatarChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverVehicleChangeRequestDTO;
import com.example.getgo.dtos.request.GetPersonalDriverChangeRequestDTO;
import com.example.getgo.dtos.request.RejectDriverChangeRequestDTO;
import com.example.getgo.dtos.user.BlockUserRequestDTO;
import com.example.getgo.dtos.user.BlockUserResponseDTO;
import com.example.getgo.dtos.user.CreatedUserDTO;
import com.example.getgo.dtos.ride.GetRideDTO;
import com.example.getgo.dtos.ride.PageResponse;
import com.example.getgo.dtos.user.UserEmailDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApiService {

    @PUT("api/admin/users/{id}/block")
    Call<BlockUserResponseDTO> blockUser(@Path("id") Long id, @Body BlockUserRequestDTO dto);

    @PUT("api/admin/users/{id}/unblock")
    Call<BlockUserResponseDTO> unblockUser(@Path("id") Long id);

    @GET("api/admin/users/unblocked")
    Call<Page<UserEmailDTO>> getUnblockedUsers(
            @Query("search") String search,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/admin/users/blocked")
    Call<Page<UserEmailDTO>> getBlockedUsers(
            @Query("search") String search,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/admin/rides/passenger")
    Call<PageResponse<GetRideDTO>> getPassengerRides(
            @Query("email") String email,
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate,
            @Query("sort") String sort,
            @Query("direction") String direction
    );

    @GET("api/admin/rides/driver")
    Call<PageResponse<GetRideDTO>> getDriverRides(
            @Query("email") String email,
            @Query("page") int page,
            @Query("size") int size,
            @Query("startDate") String startDate,
            @Query("sort") String sort,
            @Query("direction") String direction
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

    @GET("api/admin/profile")
    Call<GetAdminDTO> getProfile();

    @PUT("api/admin/profile")
    Call<UpdatedAdminDTO> updateProfile(@Body UpdateAdminDTO updateAdminDTO);

    @PUT("api/admin/profile/password")
    Call<UpdatedPasswordDTO> updatePassword(@Body UpdatePasswordDTO updatePasswordDTO);

    @POST("api/admin/drivers/register")
    Call<CreatedDriverDTO> registerDriver(@Body CreateDriverDTO createDriverDTO);

    @GET("api/admin/driver-change-requests/personal")
    Call<Page<GetPersonalDriverChangeRequestDTO>> getPendingPersonalChangeRequests(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/admin/driver-change-requests/vehicle")
    Call<Page<GetDriverVehicleChangeRequestDTO>> getPendingVehicleChangeRequests(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/admin/driver-change-requests/picture")
    Call<Page<GetDriverAvatarChangeRequestDTO>> getPendingPictureChangeRequests(
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("api/admin/driver-change-requests/personal/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approvePersonalChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/vehicle/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approveVehicleChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/picture/{requestId}/approve")
    Call<AcceptDriverChangeRequestDTO> approvePictureChangeRequest(@Path("requestId") Long requestId);

    @PUT("api/admin/driver-change-requests/personal/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectPersonalChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );

    @PUT("api/admin/driver-change-requests/vehicle/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectVehicleChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );

    @PUT("api/admin/driver-change-requests/picture/{requestId}/reject")
    Call<AcceptDriverChangeRequestDTO> rejectPictureChangeRequest(
            @Path("requestId") Long requestId,
            @Body RejectDriverChangeRequestDTO rejectDriverChangeRequestDTO
    );

    @GET("api/admin/active-rides")
    Call<List<GetActiveRideAdminDTO>> getActiveRides();

    @GET("api/admin/active-rides/{id}")
    Call<GetActiveRideAdminDetailsDTO> getActiveRideDetails(@Path("id") int id);
}