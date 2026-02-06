package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RideApiService;
import com.example.getgo.dtos.ride.CancelRideRequestDTO;
import com.example.getgo.dtos.ride.CreateRideRequestDTO;
import com.example.getgo.dtos.ride.CreatedRideResponseDTO;
import com.example.getgo.dtos.ride.GetDriverActiveRideDTO;
import com.example.getgo.dtos.ride.GetPassengerActiveRideDTO;
import com.example.getgo.dtos.ride.UpdateRideDTO;
import com.example.getgo.dtos.ride.UpdatedRideDTO;

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

    public GetPassengerActiveRideDTO getPassengerActiveRide() throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<GetPassengerActiveRideDTO> response = service.getPassengerActiveRide().execute();

        if (response.isSuccessful()) {
            if (response.body() == null) {
                Log.d(TAG, "No active ride for passenger");
                return null;
            }
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch active ride: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch active ride");
        }
    }

    public GetDriverActiveRideDTO getDriverActiveRide() throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        Response<GetDriverActiveRideDTO> response = service.getDriverActiveRide().execute();

        if (response.isSuccessful()) {
            if (response.body() == null) {
                Log.d(TAG, "No active ride for driver");
                return null;
            }
            return response.body();
        } else {
            String errBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "Failed to fetch active ride: " + response.code() + " - " + errBody);
            throw new Exception("Failed to fetch active ride");
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

    public void cancelRide(Long rideId, String reason) throws Exception {
        RideApiService service = ApiClient.getClient().create(RideApiService.class);
        CancelRideRequestDTO request = new CancelRideRequestDTO(reason);
        Response<Void> response = service.cancelRideByDriver(rideId, request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("Failed to cancel ride");
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