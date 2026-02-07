package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.PassengerApiService;
import com.example.getgo.dtos.passenger.GetPassengerDTO;
import com.example.getgo.dtos.passenger.UpdatePassengerDTO;
import com.example.getgo.dtos.passenger.UpdatedPassengerDTO;
import com.example.getgo.dtos.user.UpdatedProfilePictureDTO;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class PassengerRepository {
    private static final String TAG = "PassengerRepository";
    private static PassengerRepository instance;
    private final PassengerApiService apiService;

    private PassengerRepository() {
        apiService = ApiClient.getClient().create(PassengerApiService.class);
    }

    public static synchronized PassengerRepository getInstance() {
        if (instance == null) {
            instance = new PassengerRepository();
        }
        return instance;
    }

    public GetPassengerDTO getProfile() throws Exception {
        Response<GetPassengerDTO> response = apiService.getProfile().execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch profile: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch profile");
        }
    }

    public UpdatedPassengerDTO updateProfile(UpdatePassengerDTO updateDTO) throws Exception {
        Response<UpdatedPassengerDTO> response = apiService.updateProfile(updateDTO).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Profile updated successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to update profile: " + response.code() + " - " + errBody);
            throw new Exception("Failed to update profile");
        }
    }

    public UpdatedProfilePictureDTO uploadProfilePicture(File file) throws Exception {
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Response<UpdatedProfilePictureDTO> response = apiService.uploadProfilePicture(body).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Profile picture uploaded successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to upload profile picture: " + response.code() + " - " + errBody);
            throw new Exception("Failed to upload profile picture");
        }
    }
}