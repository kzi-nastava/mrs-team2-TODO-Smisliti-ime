package com.example.getgo.api.services;

import com.example.getgo.dtos.ridePrice.GetRidePriceDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RidePriceApiService {
    @GET("api/ride-price/prices/{vehicleType}")
    Call<GetRidePriceDTO> getPrice(@Path("vehicleType") String vehicleType);

    @PUT("api/ride-price/prices/{vehicleType}")
    Call<Void> updatePrice(@Path("vehicleType") String vehicleType, @Body GetRidePriceDTO data);
}
