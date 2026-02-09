package com.example.getgo.fragments.admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.getgo.R;


public class AdminActiveRidesFragment extends Fragment {





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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_active_rides, container, false);
    }
}