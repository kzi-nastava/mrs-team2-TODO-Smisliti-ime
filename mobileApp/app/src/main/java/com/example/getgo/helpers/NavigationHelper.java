package com.example.getgo.helpers;

import androidx.fragment.app.Fragment;

import com.example.getgo.R;
import com.example.getgo.fragments.AdminProfileInfoFragment;
import com.example.getgo.fragments.DriverHomeFragment;
import com.example.getgo.fragments.DriverProfileInfoFragment;
import com.example.getgo.fragments.PassengerProfileInfoFragment;
import com.example.getgo.fragments.RideHistoryFragment;
import com.example.getgo.model.UserRole;

public class NavigationHelper {
    private final UserRole userRole;

    public NavigationHelper(UserRole userRole) {
        this.userRole = userRole;
    }

    // Get the appropriate bottom navigation menu resource for the current user role
    public int getBottomNavMenu() {
        switch (userRole) {
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
        // Bottom Navigation
        if (itemId == R.id.nav_bottom_home) {
            return getHomeFragment();
        } else if (itemId == R.id.nav_bottom_my_rides) {
            return null; // TODO: Create UserRidesFragment
        } else if (itemId == R.id.nav_bottom_history) {
            return new RideHistoryFragment();
        } else if (itemId == R.id.nav_bottom_profile) {
            return getProfileFragment();
        } else if (itemId == R.id.nav_bottom_dashboard) {
            return null; // TODO: Create DashboardFragment
        } else if (itemId == R.id.nav_bottom_users) {
            return null; // TODO: Create ManageUsersFragment
        } else if (itemId == R.id.nav_bottom_reports) {
            return null; // TODO: Create ReportsFragment (Admin)
        }

        // Drawer Navigation (Admin)
        else if (itemId == R.id.nav_drawer_panic) {
            return null; // TODO: Create PanicNotificationsFragment
        } else if (itemId == R.id.nav_drawer_pricing) {
            return null; // TODO: Create ManagePricingFragment
        } else if (itemId == R.id.nav_drawer_search) {
            return null; // TODO: Create SearchUsersFragment
        } else if (itemId == R.id.nav_drawer_support_chats) {
            return null; // TODO: Create SupportChatsFragment
        }

        // Drawer Navigation (Driver)
        else if (itemId == R.id.nav_drawer_active_hours) {
            // Active hours are shown in driver profile
            return getProfileFragment();
        }

        // Drawer Navigation (Passenger)
        else if (itemId == R.id.nav_drawer_favorites) {
            return null; // TODO: Create FavoriteRoutesFragment
        }

        // Drawer Navigation (Common)
        else if (itemId == R.id.nav_drawer_logout) {
            // Logout is handled separately in MainActivity?
            return null;
        }

        return null;
    }

    // Get the home fragment based on user role
    private Fragment getHomeFragment() {
        switch (userRole) {
            case PASSENGER:
                return new DriverHomeFragment(); // TODO: Create PassengerHomeFragment
            case ADMIN:
                return new DriverHomeFragment(); // TODO: Create AdminHomeFragment
            default:
                return new DriverHomeFragment();
        }
    }

    // Get profile fragment based on user role
    private Fragment getProfileFragment() {
        switch (userRole) {
            case ADMIN:
                return AdminProfileInfoFragment.newInstance();
            case DRIVER:
                return DriverProfileInfoFragment.newInstance();
            case PASSENGER:
                return PassengerProfileInfoFragment.newInstance();
            default:
                return null;
        }
    }

    // Get default/starting fragment for the current user role
    public Fragment getDefaultFragment() {
        return getHomeFragment();
    }
}