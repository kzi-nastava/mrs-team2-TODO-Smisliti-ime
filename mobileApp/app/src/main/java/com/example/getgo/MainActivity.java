package com.example.getgo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.fragments.DriverHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;


import com.example.getgo.fragments.RideHistoryFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new RideHistoryFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.item_1) {
                selectedFragment = new DriverHomeFragment();
            } else if (item.getItemId() == R.id.item_2) {
                selectedFragment = new RideHistoryFragment();
            } else if (item.getItemId() == R.id.item_3) {
                selectedFragment = new DriverHomeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            return true;
        });

    }
}