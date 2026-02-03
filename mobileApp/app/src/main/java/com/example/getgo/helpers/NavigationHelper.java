package com.example.getgo.helpers;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.fragments.AdminProfileInfoFragment;
import com.example.getgo.fragments.DriverHomeFragment;
import com.example.getgo.fragments.DriverProfileInfoFragment;
import com.example.getgo.fragments.GuestHomeFragment;
import com.example.getgo.fragments.PassengerProfileInfoFragment;
import com.example.getgo.fragments.PassengerRateDriverVehicleFragment;
import com.example.getgo.fragments.RideHistoryFragment;
import com.example.getgo.fragments.passengers.PassengerHomeFragment;
import com.example.getgo.model.UserRole;
import com.example.getgo.fragments.PassengerRideTrackingFragment;


public class NavigationHelper {
    private final UserRole userRole;

    public NavigationHelper(UserRole userRole) {
        this.userRole = userRole;
    }

    // Get the appropriate bottom navigation menu resource for the current user role
    public int getBottomNavMenu() {
        switch (userRole) {
            case GUEST:
                return 0;
            case PASSENGER:
                return R.menu.passenger_bottom_nav_menu;
            case ADMIN:
                return R.menu.admin_bottom_nav_menu;
            default:
                return R.menu.driver_bottom_nav_menu;
        }
    }

    // Get the appropriate drawer navigation menu resource for the current user role
    public int getDrawerNavMenu() {
        switch (userRole) {
            case PASSENGER:
                return R.menu.passenger_drawer_nav_menu;
            case ADMIN:
                return R.menu.admin_drawer_nav_menu;
            default:
                return R.menu.driver_drawer_nav_menu;
        }
    }

    // Get the appropriate fragment for a given menu item ID
    public Fragment getFragmentForMenuItem(int itemId) {
        switch (userRole) {
            case DRIVER:
                return getDriverFragment(itemId);
            case PASSENGER:
                return getPassengerFragment(itemId);
            case ADMIN:
                return getAdminFragment(itemId);
            default:
                return null;
        }
    }

    public Fragment getStartFragment() {
        switch (userRole) {
            case GUEST:
                return new GuestHomeFragment();
            case PASSENGER:
                return new PassengerHomeFragment();
            case ADMIN:
                return new DriverHomeFragment(); // TODO: Create AdminDashboardFragment
            default:
                return new DriverHomeFragment();
        }
    }

    private Fragment getDriverFragment(int itemId) {
        if (itemId == R.id.nav_bottom_home) {
            return new DriverHomeFragment();
        } else if (itemId == R.id.nav_bottom_history) {
            return new RideHistoryFragment();
        } else if (itemId == R.id.nav_bottom_profile) {
            return DriverProfileInfoFragment.newInstance();
        }

        return null;
    }

    private Fragment getPassengerFragment(int itemId) {
        if (itemId == R.id.nav_bottom_home) {
            return PassengerHomeFragment.newInstance();
        } else if (itemId == R.id.nav_bottom_profile) {
            return PassengerProfileInfoFragment.newInstance();
        } else if (itemId == R.id.nav_bottom_rate_ride) {
            return new PassengerRateDriverVehicleFragment();
        } else if (itemId == R.id.nav_bottom_ride_tracking) {
            Log.d("NavigationHelper", "Navigating to PassengerRideTrackingFragment");
            return new PassengerRideTrackingFragment();
        }
        return null;
    }

    private Fragment getAdminFragment(int itemId) {
        if (itemId == R.id.nav_bottom_dashboard) {
            return new DriverHomeFragment();
        } else if (itemId == R.id.nav_bottom_profile) {
            return new AdminProfileInfoFragment();
        }

        return null;
    }
}