package com.example.getgo.repositories;

import android.content.Context;
import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;

import retrofit2.Response;

public class RideRepository {
    private static final String TAG = "RideRepository";
    private static RideRepository instance;

    private RideRepository() {}

    public static synchronized RideRepository getInstance() {
        if (instance == null) {
            instance = new RideRepository();
        }
        return instance;
    }

    public CreatedRideResponseDTO orderRide(CreateRideRequestDTO request) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<CreatedRideResponseDTO> response = service.orderRide(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Ride ordered successfully: " + response.body().getRideId());
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Order ride failed: " + response.code() + " - " + errBody);
            throw new Exception("Failed to order ride: " + errBody);
        }
    }
}