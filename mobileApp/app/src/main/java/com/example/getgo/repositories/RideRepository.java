package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.ride.CancelRideRequestDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedFavoriteRideDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetFavoriteRideDTO;
import com.example.getgo.dtos.ride.GetPassengerActiveRideDTO;
import com.example.getgo.dtos.ride.UpdateRideDTO;
import com.example.getgo.dtos.ride.UpdatedRideDTO;
import com.example.getgo.dtos.ride.RideCompletionDTO;

import java.util.List;

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

    public List<GetFavoriteRideDTO> getFavoriteRides() throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<List<GetFavoriteRideDTO>> response = service.getFavoriteRides().execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Fetched " + response.body().size() + " favorite rides");
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch favorites: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch favorite rides");
        }
    }

    public CreatedFavoriteRideDTO favoriteRide(Long completedRideId) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<CreatedFavoriteRideDTO> response = service.favoriteRide(completedRideId).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Ride favorited: " + completedRideId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to favorite ride: " + response.code() + " - " + errBody);
            throw new Exception(errBody);
        }
    }

    public void unfavoriteRide(Long completedRideId) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<Void> response = service.unfavoriteRide(completedRideId).execute();

        if (!response.isSuccessful()) {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to unfavorite ride: " + response.code() + " - " + errBody);
            throw new Exception(errBody);
        }
        Log.d(TAG, "Ride unfavorited: " + completedRideId);
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

    public GetPassengerActiveRideDTO getPassengerActiveRide() throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);

        try {
            Response<GetPassengerActiveRideDTO> response = service.getPassengerActiveRide().execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to fetch active ride: " + response.code() + " - " + errBody);
                throw new Exception("Failed to fetch active ride");
            }
        } catch (com.google.gson.JsonSyntaxException | java.io.EOFException e) {
            return null; // No active ride, response body empty
        }
    }

    public GetDriverActiveRideDTO getDriverActiveRide() throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);

        try {
            Response<GetDriverActiveRideDTO> response = service.getDriverActiveRide().execute();

            if (response.isSuccessful()) {
                return response.body();
            } else {
                String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Failed to fetch active ride: " + response.code() + " - " + errBody);
                throw new Exception("Failed to fetch active ride");
            }
        } catch (com.google.gson.JsonSyntaxException | java.io.EOFException e) {
            return null; // No active ride, response body empty
        }
    }

    public UpdatedRideDTO acceptRide(Long rideId) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<UpdatedRideDTO> response = service.acceptRide(rideId).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            throw new Exception("Failed to accept ride");
        }
    }

    public UpdatedRideDTO startRide(Long rideId) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<UpdatedRideDTO> response = service.startRide(rideId).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            throw new Exception("Failed to start ride");
        }
    }

    public RideCompletionDTO cancelRide(Long rideId, String reason) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        CancelRideRequestDTO request = new CancelRideRequestDTO(reason);
        Response<RideCompletionDTO> response = service.cancelRideByDriver(rideId, request).execute();

        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to cancel ride: " + response.code() + " - " + errBody);
            throw new Exception("Failed to cancel ride: " + errBody);
        }
    }

    public UpdatedRideDTO finishRide(Long rideId, String status) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        UpdateRideDTO request = new UpdateRideDTO(status);
        Response<UpdatedRideDTO> response = service.finishRide(rideId, request).execute();

        if (response.isSuccessful() && response.body() != null) {
            Log.d(TAG, "Ride finished successfully: " + rideId);
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to finish ride: " + response.code() + " - " + errBody);
            throw new Exception("Failed to finish ride");
        }
    }

}