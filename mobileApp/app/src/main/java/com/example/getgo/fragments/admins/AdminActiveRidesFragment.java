package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.getgo.R;
import com.example.getgo.adapters.AdminActiveRidesAdapter;
import com.example.getgo.dtos.activeRide.GetActiveRideAdminDTO;
import com.example.getgo.repositories.AdminRepository;

import java.util.List;


public class AdminActiveRidesFragment extends Fragment {

    private RecyclerView rvActiveRides;
    private EditText etSearch;
    private AdminActiveRidesAdapter adapter;

    public AdminActiveRidesFragment() {
        // Required empty public constructor
    }

    public static AdminActiveRidesFragment newInstance(String param1, String param2) {
        AdminActiveRidesFragment fragment = new AdminActiveRidesFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_active_rides, container, false);

        rvActiveRides = view.findViewById(R.id.rvActiveRides);
        etSearch = view.findViewById(R.id.etSearchDriver);

        adapter = new AdminActiveRidesAdapter(ride -> {
            AdminActiveRideDetailsFragment detailsFragment = new AdminActiveRideDetailsFragment();
            Bundle b = new Bundle();
            b.putLong("rideId", ride.getId());
            detailsFragment.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvActiveRides.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActiveRides.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        loadActiveRides();

        return view;
    }

    private void loadActiveRides() {
        new Thread(() -> {
            try {
                List<GetActiveRideAdminDTO> rides = AdminRepository.getInstance().getActiveRides();
                requireActivity().runOnUiThread(() -> adapter.setData(rides));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}