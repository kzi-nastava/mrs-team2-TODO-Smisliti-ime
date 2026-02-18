package com.example.getgo.api.services;

import com.example.getgo.dtos.vehicle.GetVehicleDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VehicleApiService {
    @GET("/api/vehicles/active")
    Call<List<GetVehicleDTO>> getVehicles();

    @GET("/api/vehicles/types")
    Call<List<String>> getVehicleTypes();
}
