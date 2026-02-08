package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;
import com.example.getgo.dtos.driver.GetDriverDTO;
import com.example.getgo.dtos.request.CreatedDriverChangeRequestDTO;
import com.example.getgo.dtos.request.UpdateDriverPersonalDTO;
import com.example.getgo.dtos.request.UpdateDriverVehicleDTO;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class DriverRepository {
    private static final String TAG = "DriverRepository";
    private static DriverRepository instance;
    private final DriverApiService apiService;

    private DriverRepository() {
        apiService = ApiClient.getClient().create(DriverApiService.class);
    }

    public static synchronized DriverRepository getInstance() {
        if (instance == null) {
            instance = new DriverRepository();
        }
        return instance;
    }

    public List<GetActiveDriverLocationDTO> getActiveDriverLocations() throws Exception {
        Response<List<GetActiveDriverLocationDTO>> response = apiService.getActiveDriverLocations().execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Fetched " + response.body().size() + " active drivers");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch active drivers: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch active drivers");
        }
    }

    public GetDriverDTO getProfile() throws Exception {
        Response<GetDriverDTO> response = apiService.getProfile().execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Driver profile fetched successfully");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch driver profile: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch driver profile");
        }
    }

    public CreatedDriverChangeRequestDTO requestPersonalInfoChange(UpdateDriverPersonalDTO updateDTO) throws Exception {
        Response<CreatedDriverChangeRequestDTO> response = apiService.requestPersonalInfoChange(updateDTO).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Personal info change request submitted: " + response.body().getRequestId());
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to submit personal info change request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to submit personal info change request");
        }
    }

    public CreatedDriverChangeRequestDTO requestVehicleInfoChange(UpdateDriverVehicleDTO updateDTO) throws Exception {
        Response<CreatedDriverChangeRequestDTO> response = apiService.requestVehicleInfoChange(updateDTO).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Vehicle info change request submitted: " + response.body().getRequestId());
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to submit vehicle info change request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to submit vehicle info change request");
        }
    }

    public CreatedDriverChangeRequestDTO requestProfilePictureChange(File file) throws Exception {
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Response<CreatedDriverChangeRequestDTO> response = apiService.requestProfilePictureChange(body).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Profile picture change request submitted: " + response.body().getRequestId());
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to submit profile picture change request: " + response.code() + " - " + errBody);
            throw new Exception("Failed to submit profile picture change request");
        }
    }
}