package com.example.getgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.core.view.WindowCompat;

import com.example.getgo.R;
import com.example.getgo.fragments.passengers.PassengerRateDriverVehicleFragment;
import com.example.getgo.fragments.admins.AdminRideHistoryFragment;
import com.example.getgo.fragments.passengers.PassengerRideHistoryFragment;
import com.example.getgo.utils.NavigationHelper;
import com.example.getgo.model.UserRole;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private UserRole currentUserRole;
    private DrawerLayout drawer;
    private NavigationHelper navigationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        initUserRole();
        setupGuestRestrictions();
        setupToolbarAndNavigation();

        Intent intent = getIntent();
        boolean openRate = intent.getBooleanExtra("OPEN_RATE_FRAGMENT", false);

        if (!openRate) {
            openFragment(navigationHelper.getStartFragment());
        }

        handleNotificationIntent(intent);

    }
    private void initUserRole() {
        String roleString = getIntent().getStringExtra("USER_ROLE");
        currentUserRole = roleString != null
                ? UserRole.valueOf(roleString)
                : UserRole.GUEST;

        navigationHelper = new NavigationHelper(currentUserRole);
    }

    private UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    private void setupGuestRestrictions() {
        if (currentUserRole != UserRole.GUEST) return;

        View bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) bottom.setVisibility(View.GONE);

        drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void setupToolbarAndNavigation() {
        FrameLayout toolbarContainer = findViewById(R.id.toolbar_container);
        toolbarContainer.removeAllViews();

        if (currentUserRole == UserRole.GUEST) {

            View toolbarView = getLayoutInflater()
                    .inflate(R.layout.guest_toolbar, toolbarContainer, true);

            Button btnLogin = toolbarView.findViewById(R.id.btnLoginToolbar);
            Button btnRegister = toolbarView.findViewById(R.id.btnRegisterToolbar);

            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));

            btnRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));

        } else {

            View toolbarView = getLayoutInflater()
                    .inflate(R.layout.standard_toolbar, toolbarContainer, true);

            Toolbar toolbar = toolbarView.findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            drawer = findViewById(R.id.drawer_layout);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar,
                    R.string.drawer_open, R.string.drawer_close
            );

            drawer.addDrawerListener(toggle);
            toggle.syncState();
            toggle.getDrawerArrowDrawable()
                    .setColor(getColor(android.R.color.white));

            setupBottomNavigation();
            setupDrawerNavigation();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.getMenu().clear();
        int menuRes = navigationHelper.getBottomNavMenu();
        if (menuRes != 0) {
            bottomNav.inflateMenu(menuRes);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle "My Rides" for both passenger and admin
            if (id == R.id.nav_bottom_my_rides) {
                if (currentUserRole == UserRole.PASSENGER) {
                    PassengerRideHistoryFragment.navigateTo(this);
                } else if (currentUserRole == UserRole.ADMIN) {
                    AdminRideHistoryFragment.navigateTo(this);
                } else {
                    Toast.makeText(this, "My Rides not available for your role", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            // Try NavigationHelper for other menu items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(id);
            if (fragment != null) {
                openFragment(fragment);
                return true;
            }

            // Fragment not implemented yet - show toast
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void setupDrawerNavigation() {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();
        navView.inflateMenu(navigationHelper.getDrawerNavMenu());

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handle logout separately
            if (id == R.id.nav_drawer_logout) {
                handleLogout();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Handle "My Rides" from drawer too
            if (id == R.id.nav_bottom_my_rides) {
                if (currentUserRole == UserRole.PASSENGER) {
                    PassengerRideHistoryFragment.navigateTo(this);
                } else if (currentUserRole == UserRole.ADMIN) {
                    AdminRideHistoryFragment.navigateTo(this);
                } else {
                    Toast.makeText(this, "My Rides not available for your role", Toast.LENGTH_SHORT).show();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Try NavigationHelper for other items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(id);
            if (fragment != null) {
                openFragment(fragment);
            } else {
                // Fragment not implemented yet - show toast
                Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void openFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    private void handleLogout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) {
            Log.d("NOTIF_TEST", "Intent je null");
            return;
        }

        Log.d("NOTIF_TEST", "Intent action = " + intent.getAction());
        Log.d("NOTIF_TEST", "Intent extras = " + intent.getExtras());

        boolean openRate = intent.getBooleanExtra("OPEN_RATE_FRAGMENT", false);
        long rideId = intent.getLongExtra("RIDE_ID", -1);
        long driverId = intent.getLongExtra("driverId", -1);

        Log.d("NOTIF_TEST", "handleNotificationIntent called");
        Log.d("NOTIF_TEST", "OPEN_RATE_FRAGMENT = " + openRate);
        Log.d("NOTIF_TEST", "RIDE_ID = " + rideId);

        if (openRate && rideId != -1 && !(getSupportFragmentManager().findFragmentById(R.id.fragmentContainer)
                instanceof PassengerRateDriverVehicleFragment)) {
            Log.d("NOTIF_TEST", "Opening PassengerRateDriverVehicleFragment for rideId: " + rideId);

            Bundle bundle = new Bundle();
            bundle.putLong("rideId", rideId);
            bundle.putLong("driverId", driverId);

            Fragment fragment = new PassengerRateDriverVehicleFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("NOTIF_TEST", "onNewIntent called");
        handleNotificationIntent(intent);
    }



}