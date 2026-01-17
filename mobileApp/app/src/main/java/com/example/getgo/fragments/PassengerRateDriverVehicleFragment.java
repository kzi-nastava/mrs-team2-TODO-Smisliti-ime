package com.example.getgo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.getgo.R;
import com.example.getgo.adapters.RatingAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.interfaces.RatingApi;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRateDriverVehicleFragment extends Fragment {

    private RatingApi api;
    private Long rideId = 1L; // temporary hardcoded ride ID
//    rideId = getArguments().getLong("rideId");

    private RecyclerView rvRatings;
    private RatingAdapter ratingAdapter;


    public PassengerRateDriverVehicleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = ApiClient.getClient().create(RatingApi.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_passenger_rate_driver_vehicle,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRatings = view.findViewById(R.id.rvRatings);
        ratingAdapter = new RatingAdapter();

        rvRatings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRatings.setAdapter(ratingAdapter);

        if (rideId != null) {
            loadRatings();
        }
    }


    private void loadRatings() {
//        String token = "Bearer " + getTokenFromStorage();

//        String token = getTokenFromStorage();
//        Log.d("TOKEN", "JWT = " + token);
        Log.d("RATINGS", "Pozivam API za rideId = " + rideId);

        api.getRatings(rideId).enqueue(new Callback<List<GetRatingDTO>>() {
            @Override
            public void onResponse(Call<List<GetRatingDTO>> call,
                                   Response<List<GetRatingDTO>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<GetRatingDTO> ratings = response.body();

                    try {
                        Log.d("RATINGS_JSON", new Gson().toJson(ratings));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ratingAdapter.setRatings(ratings);
                    for (GetRatingDTO r : ratings) {
                        Log.d("RATINGS", r.getComment());
                    }

                    Log.d("RATINGS", response.body().toString());

                    Log.d("RATINGS", "Loaded ratings: " + ratings.size());
                } else {
                    Log.e("RATINGS", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GetRatingDTO>> call, Throwable t) {
                Log.e("RATINGS", "API call failed", t);
                t.printStackTrace();
            }
        });
    }
}