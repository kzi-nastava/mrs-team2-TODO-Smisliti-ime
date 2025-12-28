package com.example.getgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.getgo.helpers.NavigationHelper;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Get user role from login
        String roleString = getIntent().getStringExtra("USER_ROLE");
        if (roleString != null) {
            currentUserRole = UserRole.valueOf(roleString);
        } else {
            // No role provided - redirect to login
            redirectToLogin();
            return;
        }

        // Initialize navigation helper
        navigationHelper = new NavigationHelper(currentUserRole);

        setupToolbar();
        setupBottomNavigation();
        setupDrawerNavigation();

        // Load default fragment for the user's role
        openFragment(navigationHelper.getStartFragment());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getColor(R.color.white));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_hamburger);
            actionBar.setHomeButtonEnabled(true);
        }

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getColor(android.R.color.white));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.getMenu().clear();
        bottomNav.inflateMenu(navigationHelper.getBottomNavMenu());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = navigationHelper.getFragmentForMenuItem(item.getItemId());
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
            // Handle logout separately
            if (item.getItemId() == R.id.nav_drawer_logout) {
                handleLogout();
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            // Handle other menu items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(item.getItemId());
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Don't allow back to login - just minimize app
            moveTaskToBack(true);
        }
    }
}