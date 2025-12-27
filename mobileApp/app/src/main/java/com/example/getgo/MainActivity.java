package com.example.getgo;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getgo.fragments.AdminProfileInfoFragment;
import com.example.getgo.fragments.DriverHomeFragment;
import com.example.getgo.fragments.DriverProfileInfoFragment;
import com.example.getgo.fragments.PassengerProfileInfoFragment;
import com.example.getgo.model.UserRole;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;



import com.example.getgo.fragments.RideHistoryFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private UserRole currentUserRole = UserRole.ADMIN; // TEST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("GetGo");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_hamburger);
            actionBar.setHomeButtonEnabled(true);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getColor(android.R.color.white));

        setupBottomNavigation(currentUserRole);
        setupDrawerMenu(currentUserRole);
        setupBottomNavListener();

        // default fragment
//        openFragment(new RideHistoryFragment());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_toolbar_menu, menu);
        return true; // vraćamo true da se meni prikaže
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_settings) {
            Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_profile) {
            Toast.makeText(MainActivity.this, "Language", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
//        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);
//        //...
//        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation(UserRole role) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.getMenu().clear();

        switch (role) {
            case DRIVER:
                bottomNav.inflateMenu(R.menu.driver_bottom_nav_menu);
                break;

            case PASSENGER:
                bottomNav.inflateMenu(R.menu.passenger_bottom_nav_menu);
                break;

            case ADMIN:
                bottomNav.inflateMenu(R.menu.admin_bottom_nav_menu);
                break;
        }
    }

    private void setupDrawerMenu(UserRole role) {
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().clear();

        switch (role) {
            case DRIVER:
                navView.inflateMenu(R.menu.driver_nav_menu);
                break;

            case PASSENGER:
                navView.inflateMenu(R.menu.passenger_nav_menu);
                break;

            case ADMIN:
                navView.inflateMenu(R.menu.admin_nav_menu);
                break;
        }
    }

    private void setupBottomNavListener() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            if (item.getItemId() == R.id.item_1) {
                fragment = getHomeFragmentByRole();
            } else if (item.getItemId() == R.id.item_2) {
                fragment = getSecondFragmentByRole();
            } else if (item.getItemId() == R.id.item_3) {
                fragment = getThirdFragmentByRole(); // or profile/settings
            }

            if (fragment != null) {
                openFragment(fragment);
            }

            return true;
        });
    }

    private Fragment getHomeFragmentByRole() {
        switch (currentUserRole) {
            case DRIVER:
                return new DriverHomeFragment();
            case PASSENGER:
//                return new PassengerHomeFragment();
            case ADMIN:
//                return new AdminHomeFragment();
            default:
                return null;
        }
    }

    private Fragment getSecondFragmentByRole() {
        switch (currentUserRole) {
            case DRIVER:
                return new RideHistoryFragment();
            case PASSENGER:
//                return new PassengerSecondFragment();
            case ADMIN:
//                return new AdminSecondFragment();
                return AdminProfileInfoFragment.newInstance();
            default:
                return null;
        }
    }

    private Fragment getThirdFragmentByRole() {
        switch (currentUserRole) {
            case DRIVER:
                return new DriverHomeFragment(); // currently
            case PASSENGER:
//                return new PassengerThirdFragment();
            case ADMIN:
//                return new AdminThirdFragment();
                return DriverProfileInfoFragment.newInstance();
            default:
                return null;
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragmentContainer, fragment)
                .commit();
    }


}