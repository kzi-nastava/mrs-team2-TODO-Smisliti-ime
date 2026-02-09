package com.example.getgo.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.fragments.admins.AdminChatListFragment;
import com.example.getgo.fragments.admins.AdminProfileInfoFragment;
import com.example.getgo.fragments.admins.AdminReviewDriverRequestsFragment;
import com.example.getgo.fragments.drivers.DriverHomeFragment;
import com.example.getgo.fragments.drivers.DriverProfileInfoFragment;
import com.example.getgo.fragments.guests.GuestHomeFragment;
import com.example.getgo.fragments.layouts.SupportChatFragment;
import com.example.getgo.fragments.passengers.PassengerProfileInfoFragment;
import com.example.getgo.fragments.passengers.PassengerRateDriverVehicleFragment;
import com.example.getgo.fragments.drivers.DriverRideHistoryFragment;
import com.example.getgo.fragments.passengers.PassengerHomeFragment;
import com.example.getgo.model.UserRole;
import com.example.getgo.fragments.passengers.PassengerRideTrackingFragment;

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
            return new DriverRideHistoryFragment();
        } else if (itemId == R.id.nav_bottom_profile) {
            return DriverProfileInfoFragment.newInstance();
        } else if (itemId == R.id.nav_drawer_support) {
            return new SupportChatFragment();
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
            return new PassengerRideTrackingFragment();
        } else if (itemId == R.id.nav_drawer_support) {
            return new SupportChatFragment();
        }
        return null;
    }

    private Fragment getAdminFragment(int itemId) {
        if (itemId == R.id.nav_bottom_dashboard) {
            return new DriverHomeFragment();
        } else if (itemId == R.id.nav_bottom_profile) {
            return new AdminProfileInfoFragment();
        } else if (itemId == R.id.nav_review_requests) {
            return AdminReviewDriverRequestsFragment.newInstance();
        } else if (itemId == R.id.nav_drawer_support_chats) {
            return new AdminChatListFragment();
        }

        return null;
    }
}