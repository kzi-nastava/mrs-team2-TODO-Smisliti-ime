package com.example.getgo.interfaces;

import com.example.getgo.dtos.vehicle.GetVehicleDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VehicleApi {
    @GET("/api/vehicles/active")
    Call<List<GetVehicleDTO>> getVehicles();
}
