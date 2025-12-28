package com.example.getgo;

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
    private UserRole currentUserRole = UserRole.PASSENGER; // TEST - Change to PASSENGER or ADMIN
    private DrawerLayout drawer;
    private NavigationHelper navigationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        navigationHelper = new NavigationHelper(currentUserRole);

        setupToolbar();
        setupBottomNavigation();
        setupDrawerNavigation();

        // Load default fragment for the user's role
        openFragment(navigationHelper.getDefaultFragment());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            // Show text if not implemented
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
                return true;
            }

            // Handle other menu items
            Fragment fragment = navigationHelper.getFragmentForMenuItem(item.getItemId());
            if (fragment != null) {
                openFragment(fragment);
            }
            // Show text if not implemented
            Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();

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

    }
}