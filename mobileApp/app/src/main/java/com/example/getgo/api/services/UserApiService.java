package com.example.getgo.api.services;

import com.example.getgo.model.UserProfile;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApiService {
    @GET("api/users/me")
    Call<UserProfile> getUserProfile();
}

