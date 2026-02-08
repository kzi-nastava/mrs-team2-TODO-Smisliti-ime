package com.example.getgo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.getgo.R;
import com.example.getgo.api.ApiClient;
import com.example.getgo.dtos.request.GetDriverAvatarChangeRequestDTO;
import com.example.getgo.dtos.request.GetDriverVehicleChangeRequestDTO;
import com.example.getgo.dtos.request.GetPersonalDriverChangeRequestDTO;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    public interface OnRequestActionListener {
        void onApprove(Object request, Long requestId);
        void onReject(Object request, Long requestId);
    }

    private final Context context;
    private final OnRequestActionListener listener;
    private List<Object> requests = new ArrayList<>();
    private String type = "personal";

    public RequestsAdapter(Context context, OnRequestActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setRequests(List<Object> requests, String type) {
        this.requests = requests;
        this.type = type;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_item_driver_change_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object request = requests.get(position);

        if (type.equals("personal")) {
            bindPersonalRequest(holder, (GetPersonalDriverChangeRequestDTO) request);
        } else if (type.equals("vehicle")) {
            bindVehicleRequest(holder, (GetDriverVehicleChangeRequestDTO) request);
        } else {
            bindAvatarRequest(holder, (GetDriverAvatarChangeRequestDTO) request);
        }
    }

    private void bindPersonalRequest(ViewHolder holder, GetPersonalDriverChangeRequestDTO request) {
        holder.tvRequestType.setText("Personal Info");
        holder.tvDate.setText(formatDate(request.getCreatedAt()));
        holder.tvDriverName.setText(request.getDriverName());
        holder.tvDriverEmail.setText(request.getDriverEmail());

        holder.layoutChanges.setVisibility(View.VISIBLE);
        holder.layoutPictures.setVisibility(View.GONE);
        holder.layoutChanges.removeAllViews();

        addChange(holder, "Name", request.getCurrentName(), request.getRequestedName());
        addChange(holder, "Surname", request.getCurrentSurname(), request.getRequestedSurname());
        addChange(holder, "Phone", request.getCurrentPhone(), request.getRequestedPhone());
        addChange(holder, "Address", request.getCurrentAddress(), request.getRequestedAddress());

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(request, request.getRequestId()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request, request.getRequestId()));
    }

    private void bindVehicleRequest(ViewHolder holder, GetDriverVehicleChangeRequestDTO request) {
        holder.tvRequestType.setText("Vehicle Info");
        holder.tvDate.setText(formatDate(request.getCreatedAt()));
        holder.tvDriverName.setText(request.getDriverName());
        holder.tvDriverEmail.setText(request.getDriverEmail());

        holder.layoutChanges.setVisibility(View.VISIBLE);
        holder.layoutPictures.setVisibility(View.GONE);
        holder.layoutChanges.removeAllViews();

        addChange(holder, "Model", request.getCurrentVehicleModel(), request.getRequestedVehicleModel());
        addChange(holder, "Type", request.getCurrentVehicleType(), request.getRequestedVehicleType());
        addChange(holder, "License Plate", request.getCurrentVehicleLicensePlate(), request.getRequestedVehicleLicensePlate());
        addChange(holder, "Seats", String.valueOf(request.getCurrentVehicleSeats()), String.valueOf(request.getRequestedVehicleSeats()));
        addChange(holder, "Allows Babies", request.getCurrentVehicleHasBabySeats() ? "Yes" : "No", request.getRequestedVehicleHasBabySeats() ? "Yes" : "No");
        addChange(holder, "Allows Pets", request.getCurrentVehicleAllowsPets() ? "Yes" : "No", request.getRequestedVehicleAllowsPets() ? "Yes" : "No");

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(request, request.getRequestId()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request, request.getRequestId()));
    }

    private void bindAvatarRequest(ViewHolder holder, GetDriverAvatarChangeRequestDTO request) {
        holder.tvRequestType.setText("Profile Picture");
        holder.tvDate.setText(formatDate(request.getCreatedAt()));
        holder.tvDriverName.setText(request.getDriverName());
        holder.tvDriverEmail.setText(request.getDriverEmail());

        holder.layoutChanges.setVisibility(View.GONE);
        holder.layoutPictures.setVisibility(View.VISIBLE);

        String currentPicUrl = request.getCurrentProfilePictureUrl() != null
                ? ApiClient.SERVER_URL + request.getCurrentProfilePictureUrl()
                : null;
        String newPicUrl = request.getRequestedProfilePictureUrl() != null
                ? ApiClient.SERVER_URL + request.getRequestedProfilePictureUrl()
                : null;

        Glide.with(context)
                .load(currentPicUrl)
                .placeholder(R.drawable.unregistered_profile)
                .circleCrop()
                .into(holder.ivCurrentPicture);

        Glide.with(context)
                .load(newPicUrl)
                .placeholder(R.drawable.unregistered_profile)
                .circleCrop()
                .into(holder.ivNewPicture);

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(request, request.getRequestId()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request, request.getRequestId()));
    }

    private void addChange(ViewHolder holder, String label, String oldValue, String newValue) {
        if (!oldValue.equals(newValue)) {
            View changeView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.admin_item_change, holder.layoutChanges, false);

            TextView tvLabel = changeView.findViewById(R.id.tvChangeLabel);
            TextView tvOldValue = changeView.findViewById(R.id.tvOldValue);
            TextView tvNewValue = changeView.findViewById(R.id.tvNewValue);

            tvLabel.setText(label + ":");
            tvOldValue.setText(oldValue);
            tvNewValue.setText(newValue);

            holder.layoutChanges.addView(changeView);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequestType, tvDate, tvDriverName, tvDriverEmail;
        LinearLayout layoutChanges, layoutPictures;
        ImageView ivCurrentPicture, ivNewPicture;
        MaterialButton btnApprove, btnReject;

        ViewHolder(View view) {
            super(view);
            tvRequestType = view.findViewById(R.id.tvRequestType);
            tvDate = view.findViewById(R.id.tvDate);
            tvDriverName = view.findViewById(R.id.tvDriverName);
            tvDriverEmail = view.findViewById(R.id.tvDriverEmail);
            layoutChanges = view.findViewById(R.id.layoutChanges);
            layoutPictures = view.findViewById(R.id.layoutPictures);
            ivCurrentPicture = view.findViewById(R.id.ivCurrentPicture);
            ivNewPicture = view.findViewById(R.id.ivNewPicture);
            btnApprove = view.findViewById(R.id.btnApprove);
            btnReject = view.findViewById(R.id.btnReject);
        }
    }
}