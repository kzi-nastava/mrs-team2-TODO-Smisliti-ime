package com.example.getgo.fragments.admins;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getgo.R;
import com.example.getgo.dtos.ridePrice.GetRidePriceDTO;
import com.example.getgo.model.RidePrice;
import com.example.getgo.model.VehicleType;
import com.example.getgo.repositories.RidePriceRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminRidePricingFragment extends Fragment {

    private TableLayout priceTable;
    RidePriceRepository repository = RidePriceRepository.getInstance();


    public AdminRidePricingFragment() {
        // Required empty public constructor
    }


    public static AdminRidePricingFragment newInstance(String param1, String param2) {
        AdminRidePricingFragment fragment = new AdminRidePricingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_ride_pricing, container, false);
        priceTable = view.findViewById(R.id.price_table);

        loadPrices();

        return view;
    }

    private void loadPrices() {
        priceTable.removeAllViews();
        TableRow header = new TableRow(getContext());
        String[] headers = {"Vehicle Type", "Start Price", "Price per Km", "Action"};
        for (String h : headers) {
            TextView tv = new TextView(getContext());
            tv.setText(h);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(10,10,10,10);
            header.addView(tv);
        }
        priceTable.addView(header);

        for (VehicleType type : VehicleType.values()) {
            TableRow row = new TableRow(getContext());
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView vehicleType = new TextView(getContext());
            vehicleType.setText(type.name());
            vehicleType.setPadding(10,10,10,10);
            vehicleType.setGravity(Gravity.CENTER);
            row.addView(vehicleType);

            EditText startPriceInput = new EditText(getContext());
            EditText pricePerKmInput = new EditText(getContext());
            Button updateBtn = new Button(getContext());
            updateBtn.setText("Update");
            updateBtn.setTextColor(Color.WHITE);
            updateBtn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.button_update_color));

            row.addView(startPriceInput);
            row.addView(pricePerKmInput);
            row.addView(updateBtn);

            priceTable.addView(row);

            repository.getPrice(type.name(), new RidePriceRepository.PriceCallback() {
                @Override
                public void onSuccess(GetRidePriceDTO priceDTO) {
                    startPriceInput.setText(String.valueOf(priceDTO.getStartPrice()));
                    pricePerKmInput.setText(String.valueOf(priceDTO.getPricePerKm()));
                }

                @Override
                public void onError(Throwable t) {
                    Toast.makeText(getContext(), "Error loading prices", Toast.LENGTH_SHORT).show();
                }
            });

            updateBtn.setOnClickListener(v -> {
                try {
                    double start = Double.parseDouble(startPriceInput.getText().toString());
                    double perKm = Double.parseDouble(pricePerKmInput.getText().toString());

                    if (start <= 0 || perKm <= 0) {
                        Toast.makeText(getContext(), "Prices must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    repository.updatePrice(type.name(),
                            new GetRidePriceDTO(perKm, start), new RidePriceRepository.UpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getContext(), "Price updated", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(Throwable t) {
                                    Toast.makeText(getContext(), "Error updating price", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}