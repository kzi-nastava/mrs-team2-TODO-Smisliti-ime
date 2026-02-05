package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.DriverApiService;
import com.example.getgo.dtos.driver.GetActiveDriverLocationDTO;

import java.util.List;

import retrofit2.Response;

public class DriverRepository {
    private static final String TAG = "DriverRepository";
    private static DriverRepository instance;

    private DriverRepository() {}

    public static synchronized DriverRepository getInstance() {
        if (instance == null) {
            instance = new DriverRepository();
        }
        return instance;
    }

    public List<GetActiveDriverLocationDTO> getActiveDriverLocations() throws Exception {
        DriverApiService service = ApiClient.getClient().create(DriverApiService.class);
        Response<List<GetActiveDriverLocationDTO>> response = service.getActiveDriverLocations().execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Fetched " + response.body().size() + " active drivers");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch active drivers: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch active drivers");
        }
    }
}