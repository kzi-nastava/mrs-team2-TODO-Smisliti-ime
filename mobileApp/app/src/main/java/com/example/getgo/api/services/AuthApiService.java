package com.example.getgo.api.services;

import com.example.getgo.dtos.user.CreateLoginDTO;
import com.example.getgo.dtos.user.ForgotPasswordDTO;
import com.example.getgo.dtos.user.ResetPasswordDTO;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Retrofit service interface for authentication endpoints.
 * All endpoints match backend AuthController structure.
 */
public interface AuthApiService {

    /**
     * POST /api/auth/login
     * Authenticates user with email and password.
     * Backend: AuthController.login(@Valid @RequestBody CreateLoginDTO)
     */
    @POST("api/auth/login")
    Call<ResponseBody> login(@Body CreateLoginDTO dto);

    /**
     * POST /api/auth/register (multipart/form-data)
     * Registers a new user with optional profile picture file.
     * Backend: AuthController.registerWithFile(@Valid @ModelAttribute CreateUserDTO, @RequestParam MultipartFile file)
     */
    @Multipart
    @POST("api/auth/register")
    Call<ResponseBody> register(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part file
    );

    /**
     * POST /api/auth/forgot-password
     * Sends password reset email to the provided address.
     * Backend: AuthController.forgotPassword(@Valid @RequestBody ForgotPasswordDTO)
     */
    @POST("api/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordDTO dto);

    /**
     * POST /api/auth/reset-password
     * Resets user password using token from email link.
     * Backend: AuthController.resetPassword(@Valid @RequestBody ResetPasswordDTO)
     */
    @POST("api/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordDTO dto);

    /**
     * GET /api/auth/activate-mobile?token=...
     * Activates user account via token (mobile JSON response).
     * Backend: AuthController.activateAccountMobileJson(@RequestParam("token") String)
     */
    @GET("api/auth/activate-mobile")
    Call<ResponseBody> activateMobile(@Query("token") String token);

    /**
     * POST /api/auth/logout
     * Checks if logout is allowed for the current user (e.g. driver not on active ride).
     * Backend: AuthController.logout()
     * Requires Authorization header (JWT token).
     */
    @POST("api/auth/logout")
    Call<ResponseBody> logout();
}

