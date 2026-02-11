package com.example.getgo.repositories;

import android.util.Log;

import com.example.getgo.api.ApiClient;
import com.example.getgo.api.services.RidePriceApiService;
import com.example.getgo.dtos.ridePrice.GetRidePriceDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RidePriceRepository {
    private static final String TAG = "RidePriceRepository";
    private static RidePriceRepository instance;

    private RidePriceApiService service;

    private final Map<String, GetRidePriceDTO> defaultPrices = new HashMap<>();

    private RidePriceRepository() {
        service = ApiClient.getClient().create(RidePriceApiService.class);

        // Default cene
        defaultPrices.put("STANDARD", new GetRidePriceDTO(120.0, 300.0));
        defaultPrices.put("VAN", new GetRidePriceDTO(150.0, 500.0));
        defaultPrices.put("LUXURY", new GetRidePriceDTO(200.0, 800.0));
    }

    public static synchronized RidePriceRepository getInstance() {
        if (instance == null) {
            instance = new RidePriceRepository();
        }
        return instance;
    }

    public void getPrice(String vehicleType, PriceCallback callback) {
        service.getPrice(vehicleType).enqueue(new Callback<GetRidePriceDTO>() {
            @Override
            public void onResponse(Call<GetRidePriceDTO> call, Response<GetRidePriceDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    // If backend doesn't have the price, return default price
                    GetRidePriceDTO defaultPrice = defaultPrices.getOrDefault(vehicleType.toUpperCase(),
                            new GetRidePriceDTO(0.0, 0.0));
                    callback.onSuccess(defaultPrice);
                    Log.e(TAG, "API error while fetching price for " + vehicleType);
                }
            }

            @Override
            public void onFailure(Call<GetRidePriceDTO> call, Throwable t) {
                // If network error, also return default price
                GetRidePriceDTO defaultPrice = defaultPrices.getOrDefault(vehicleType.toUpperCase(),
                        new GetRidePriceDTO(0.0, 0.0));
                callback.onSuccess(defaultPrice);
                Log.e(TAG, "Error while fetching price: " + t.getMessage());
            }
        });
    }

    public interface PriceCallback {
        void onSuccess(GetRidePriceDTO priceDTO);
        void onError(Throwable t);
    }


    public void updatePrice(String vehicleType, GetRidePriceDTO data, UpdateCallback callback) {
        service.updatePrice(vehicleType.toUpperCase(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Price successfully updated for " + vehicleType);
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Updating price failed: " + response.code());
                    callback.onError(new Exception("Update failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Erorr while updating: " + t.getMessage());
                callback.onError(t);
            }
        });
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Throwable t);
    }

}
