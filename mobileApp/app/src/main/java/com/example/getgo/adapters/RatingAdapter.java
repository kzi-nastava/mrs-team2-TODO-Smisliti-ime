package com.example.getgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.getgo.dtos.rating.GetRatingDTO;
import com.example.getgo.R;
import java.util.ArrayList;
import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {

    private List<GetRatingDTO> ratings = new ArrayList<>();

    public void setRatings(List<GetRatingDTO> ratings) {
        this.ratings = ratings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        GetRatingDTO rating = ratings.get(position);
        holder.tvVehicleRating.setText("Vehicle: " + getStars(rating.getVehicleRating()));
        holder.tvDriverRating.setText("Driver: " + getStars(rating.getDriverRating()));
        holder.tvComment.setText(rating.getComment());
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    private String getStars(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) sb.append("★");
        for (int i = rating; i < 5; i++) sb.append("☆");
        return sb.toString();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleRating, tvDriverRating, tvComment;
        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleRating = itemView.findViewById(R.id.tvVehicleRating);
            tvDriverRating = itemView.findViewById(R.id.tvDriverRating);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }
}

