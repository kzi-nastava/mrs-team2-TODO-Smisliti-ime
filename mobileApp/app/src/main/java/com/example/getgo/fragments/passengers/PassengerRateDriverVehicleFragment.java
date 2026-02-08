package com.example.getgo.fragments.passengers;

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
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.adapters.RatingAdapter;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.rating.CreateRatingDTO;
import com.example.getgo.dtos.rating.CreatedRatingDTO;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.api.services.RatingApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerRateDriverVehicleFragment extends Fragment {

    private RatingApiService api;
    private Long rideId;
    private Long driverId;

    private RecyclerView rvRatings;
    private RatingAdapter ratingAdapter;

    private RatingBar ratingVehicle, ratingDriver;
    private EditText commentInput;
    private Button submitBtn;

    private RatingBar ratingBarAvgVehicle;
    private RatingBar ratingBarAvgDriver;




    public PassengerRateDriverVehicleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        api = ApiClient.getClient().create(RatingApiService.class);

        if (getArguments() != null) {
            rideId = getArguments().getLong("rideId");
            driverId = getArguments().getLong("driverId");
        }
        Log.d("RATE_DEBUG", "rideId = " + rideId);
        Log.d("RATE_DEBUG", "driverId = " + driverId);
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

        ratingVehicle = view.findViewById(R.id.ratingVehicle);
        ratingDriver = view.findViewById(R.id.ratingDriver);
        commentInput = view.findViewById(R.id.commentInput);
        submitBtn = view.findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> submitRating());

        ratingBarAvgVehicle = view.findViewById(R.id.ratingBarAvgVehicle);
        ratingBarAvgDriver = view.findViewById(R.id.ratingBarAvgDriver);


        if (driverId != null) {
            loadRatings();
        } else {
            Log.e("RATINGS", "driverId is null – cannot load ratings by driver");
        }

    }


    private void loadRatings() {
        Log.d("RATINGS", "Pozvao sam loadRatings() sa driverId = " + driverId);
        if (driverId == null) {
            Log.e("RATINGS", "driverId is null, cannot load ratings");
            return;
        }
//        String token = "Bearer " + getTokenFromStorage();

//        String token = getTokenFromStorage();
//        Log.d("TOKEN", "JWT = " + token);
        api.getRatingsByDriver(driverId).enqueue(new Callback<List<GetRatingDTO>>() {
            @Override
            public void onResponse(Call<List<GetRatingDTO>> call,
                                   Response<List<GetRatingDTO>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<GetRatingDTO> ratings = response.body();

                    ratingAdapter.setRatings(ratings);
                    if (ratings != null && !ratings.isEmpty()) {
                        float sumVehicle = 0f;
                        float sumDriver = 0f;

                        for (GetRatingDTO r : ratings) {
                            sumVehicle += r.getVehicleRating();
                            sumDriver += r.getDriverRating();
                        }

                        float avgVehicle = sumVehicle / ratings.size();
                        float avgDriver = sumDriver / ratings.size();

                        ratingBarAvgVehicle.setRating(avgVehicle);
                        ratingBarAvgDriver.setRating(avgDriver);

                        TextView tvAvgVehicleNumber = getView().findViewById(R.id.tvAvgVehicleNumber);
                        TextView tvAvgDriverNumber = getView().findViewById(R.id.tvAvgDriverNumber);

                        tvAvgVehicleNumber.setText(String.format("%.1f", avgVehicle));
                        tvAvgDriverNumber.setText(String.format("%.1f", avgDriver));
                    }
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

    private String getStars(double rating) {
        StringBuilder sb = new StringBuilder();
        int fullStars = (int) rating;
        for (int i = 0; i < fullStars; i++) sb.append("★");
        for (int i = fullStars; i < 5; i++) sb.append("☆");
        return sb.toString();
    }

    private void submitRating() {
        Log.d("RATING_POST", "Usao sam u submitRating()");

        int vehicleRating = (int) ratingVehicle.getRating();
        int driverRating = (int) ratingDriver.getRating();
        String comment = commentInput.getText().toString().trim();

        if (vehicleRating == 0 || driverRating == 0) {
            Log.e("RATING_POST", "Rating cannot be 0");
            return;
        }

        CreateRatingDTO dto = new CreateRatingDTO(
                driverRating,
                vehicleRating,
                comment
        );

        api.createRating(rideId, dto).enqueue(new Callback<CreatedRatingDTO>() {


            @Override
            public void onResponse(Call<CreatedRatingDTO> call,
                                   Response<CreatedRatingDTO> response) {

                if (response.isSuccessful() && response.body() != null) {
                    CreatedRatingDTO created = response.body();

                    Log.d("RATING_POST", "Created rating id = " + created.getId());

                    // reset polja
                    ratingVehicle.setRating(0);
                    ratingDriver.setRating(0);
                    commentInput.setText("");

                    Log.d("RATING_POST", "Refreshovao sam polja nakon postovanja");
                    // refresh liste
                    loadRatings();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("ALREADY_RATED")) {
                            Toast.makeText(getContext(), "You have already rated this ride!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to rate ride: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to rate ride: " + response.code(), Toast.LENGTH_SHORT).show();
                    }

                    Log.e("RATING_POST", "POST error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CreatedRatingDTO> call, Throwable t) {
                Log.e("RATING_POST", "POST failed", t);
            }
        });
    }

}